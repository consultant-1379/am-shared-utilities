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

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

abstract class AbstractLock implements Lock {

    static final String SHARED_HASH_SUFFIX = "/owners";
    static final long SHARED_EXCLUSIVE_TTL = 500L;
    static final long SHARED_EXCLUSIVE_TIMEOUT = 500L;
    static final long SHARED_EXCLUSIVE_RETRY_INTERVAL = 300L;
    static final int SHARED_EXCLUSIVE_RETRY_ATTEMPTS = 3;
    static final int EXCLUSIVE_RETRY_ATTEMPTS = 5;
    static final long DEFAULT_TTL = 5000L;
    static final long LONG_RUNNING_TTL = 7000L;
    static final long LONG_RUNNING_TIMEOUT_THRESHOLD = 30000L;

    protected final RedisTemplate<String, String> redisTemplate;
    protected final BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue;
    protected final String owner;
    protected final String name;
    protected final String sharedHashKey;
    protected final LockTtlRefreshTask ttlRefreshTask;
    protected final long ttl;     // ttl in ms
    protected final long defaultTimeout; // timeout in ms
    protected int maxAttempts;
    protected long retryInterval;

    AbstractLock(RedisTemplate<String, String> redisTemplate, BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue,
                 LockType lockType, String name, String owner) {
        this.redisTemplate = redisTemplate;
        this.ttlRefreshTasksQueue = ttlRefreshTasksQueue;
        this.name = name;
        this.sharedHashKey = name + SHARED_HASH_SUFFIX;
        this.owner = owner;
        final String refreshKey = (lockType == LockType.SHARED) ? sharedHashKey : name;
        this.ttlRefreshTask = new LockTtlRefreshTask(refreshKey, owner, lockType);
        this.ttl = DEFAULT_TTL;
        this.defaultTimeout = DEFAULT_TTL;
        this.maxAttempts = 1;
        this.retryInterval = 300L;
    }

    AbstractLock(RedisTemplate<String, String> redisTemplate, BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue,
                 LockType lockType, String name, String owner, long ttl, long defaultTimeout) {
        this.redisTemplate = redisTemplate;
        this.ttlRefreshTasksQueue = ttlRefreshTasksQueue;
        this.name = name;
        this.owner = owner;
        this.sharedHashKey = name + SHARED_HASH_SUFFIX;
        final String refreshKey = (lockType == LockType.SHARED) ? sharedHashKey : name;
        this.ttlRefreshTask = new LockTtlRefreshTask(refreshKey, owner, lockType);
        this.ttl = ttl == 0L ? DEFAULT_TTL : ttl;
        this.defaultTimeout = defaultTimeout;
        this.maxAttempts = 1;
        this.retryInterval = 300L;
    }

    @Override
    public Lock withAcquireRetries(int attempts, long retryInterval) {
        this.maxAttempts = attempts;
        this.retryInterval = retryInterval;
        return this;
    }

    protected boolean lock(long ttl) { // NOSONAR
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(name, owner, ttl, TimeUnit.MILLISECONDS));
    }

    /**
     * Encapsulates a retry logic for acquiring a lock.
     * @param ttl initial ttl for lock
     * @param maxAttempts max number of acquire attempts
     * @param retryInterval interval between acquire attempts
     * @return true if lock has been acquired
     */
    protected boolean lockWithRetries(final long ttl, int maxAttempts, long retryInterval) {
        boolean result = lock(ttl);
        for (int attempts = maxAttempts; !result && attempts > 0; attempts--) {
            try {
                Thread.sleep(retryInterval);
                result = lock(ttl);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return result;
    }

    protected long getEffectiveTtl(long timeout) { // NOSONAR
        return (timeout < LONG_RUNNING_TIMEOUT_THRESHOLD) ? this.ttl : Long.max(this.ttl, LONG_RUNNING_TTL);
    }

    /**
     * Submits a TTL refresh task to the thread running a refresher.
     * @param timeout maximum time to hold a lock
     * @param ttl time-to-live to put on lock, refreshed automatically until released or timeout get reached
     * @return true on success, false if current thread was interrupted
     */
    protected boolean installRefreshTask(final long timeout, final long ttl) {
        if (timeout > ttl) {
            ttlRefreshTask.setExpiration(System.currentTimeMillis() + timeout);
            ttlRefreshTask.setTtl(ttl);
            try {
                ttlRefreshTasksQueue.put(ttlRefreshTask);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    /**
     * Locks TTL refresh process is lazy and doesn't do cleanup of expired shared or group lock's owners
     * So, it is up to lock implementation to clean them during acquire. This method just does that job.
     * Note that this method should be called when corresponding exclusive lock is held.
     */
    protected void evictExpired() {
        HashOperations<String, String, Long> ownersTimeoutsOps = redisTemplate.opsForHash();
        Map<String, Long> owners = ownersTimeoutsOps.entries(sharedHashKey);
        if (owners.isEmpty()) {
            return;
        }
        final long now = System.currentTimeMillis();
        final List<String> expired = new ArrayList<>();
        owners.forEach((k, v) -> {
            if (v < now) {
                expired.add(k);
            }
        });
        if (!expired.isEmpty()) {
            ownersTimeoutsOps.delete(sharedHashKey, (Object[]) expired.toArray(new String[expired.size()]));
        }
    }
}
