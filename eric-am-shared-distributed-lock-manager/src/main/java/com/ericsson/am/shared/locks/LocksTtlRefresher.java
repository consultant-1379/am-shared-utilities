/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.am.shared.locks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LocksTtlRefresher implements Runnable {
    private static final long EMPTY_TASKS_SLEEP_TIME = 5000L;
    private static final long TASK_TIMEOUT_THRESHOLD = 500L;
    static final long PROLONGATION_TRESHOLD = 1000L;
    private final BlockingQueue<LockTtlRefreshTask> lockUpdatesQueue;
    private final RedisTemplate<String, String> redisTemplate;
    private final SortedMap<Long, LockTtlRefreshTask> tasks;

    public LocksTtlRefresher(BlockingQueue<LockTtlRefreshTask> lockUpdatesQueue,
                             RedisTemplate<String, String> redisTemplate) {
        this.lockUpdatesQueue = lockUpdatesQueue;
        this.redisTemplate = redisTemplate;
        this.tasks = new TreeMap<>();
    }

    @Override
    public void run() {
        boolean interrupted;
        long pollTimeout = EMPTY_TASKS_SLEEP_TIME;
        do {
            interrupted = pollTasks(pollTimeout);
            pollTimeout = prolongateExisting();
            log.debug("Will poll incoming tasks for up to {}ms", pollTimeout);
        } while (!interrupted);
    }

    private boolean pollTasks(long pollTimeout) {
        LockTtlRefreshTask task = null;
        try {
            task = lockUpdatesQueue.poll(pollTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
        while (task != null) {
            log.debug("New task to refresh {} lock on {} owned by {}, ttl = {}", task.getType().name().toLowerCase(),
                    task.getName(), task.getOwner(), task.getTtl());
            prolongate(task);
            task = lockUpdatesQueue.poll();
        }
        return false;
    }

    private void prolongate(LockTtlRefreshTask task) {
        if (task.getType() == LockType.EXCLUSIVE) {
            if (task.getOwner().equals(redisTemplate.opsForValue().get(task.getName()))) { // verify lock is still owned by task's owner
                prolongateExclusive(task);
            } else {
                log.debug("{} lock on {} no more owned by {}, stopped refreshes for it",
                        task.getType().name().toLowerCase(), task.getName(), task.getOwner());
            }
        } else {
            HashOperations<String, String, Long> ownerOps = redisTemplate.opsForHash();
            if (ownerOps.hasKey(task.getName(), task.getOwner())) { // verify lock is still owned by task's owner
                prolongateShared(task);
            } else {
                log.debug("{} lock on {} no more owned by {}, stopped refreshes for it",
                        task.getType().name().toLowerCase(), task.getName(), task.getOwner());
            }
        }
    }

    private void prolongateExclusive(LockTtlRefreshTask task) {
        long now = System.currentTimeMillis();
        long newExpiration = now + task.getTtl();
        if (newExpiration + PROLONGATION_TRESHOLD >= task.getExpiration()) { // The final cut, may be a bit longer than std ttl
            redisTemplate.expire(task.getName(), task.getExpiration() - now, TimeUnit.MILLISECONDS);
        } else { // This is not a last prolongation, refresh and reschedule
            redisTemplate.expire(task.getName(), task.getTtl(), TimeUnit.MILLISECONDS);
            tasks.put(newExpiration, task);
        }
        log.debug("{} lock on {} owned by {} refreshed", task.getType().name().toLowerCase(), task.getName(),
                task.getOwner());
    }

    private void prolongateShared(LockTtlRefreshTask task) {
        long now = System.currentTimeMillis();
        long newExpiration = now + task.getTtl();
        if (newExpiration + PROLONGATION_TRESHOLD >= task.getExpiration()) { // The final cut, may be a bit longer than std ttl
            redisTemplate.opsForHash().put(task.getName(), task.getOwner(), task.getExpiration());
        } else {
            redisTemplate.opsForHash().put(task.getName(), task.getOwner(), newExpiration);
            tasks.put(newExpiration, task);
        }
        log.debug("{} lock on {} owned by {} refreshed", task.getType().name().toLowerCase(), task.getName(),
                task.getOwner());
    }

    private long prolongateExisting() {
        if (tasks.isEmpty()) {
            return EMPTY_TASKS_SLEEP_TIME;
        }
        Long expiring = tasks.firstKey();
        while (expiring < System.currentTimeMillis() + TASK_TIMEOUT_THRESHOLD) {
            LockTtlRefreshTask task = tasks.remove(expiring);
            prolongate(task); // Update ttl/expiration in Redis and put task back under a new expiration time
            if (tasks.isEmpty()) { // If task wasn't prolonged the map may become empty
                return EMPTY_TASKS_SLEEP_TIME;
            }
            expiring = tasks.firstKey(); // This may throw NoSuchElementException if map is empty; lines above prevents it
        }
        return expiring - System.currentTimeMillis() - TASK_TIMEOUT_THRESHOLD;
    }
}
