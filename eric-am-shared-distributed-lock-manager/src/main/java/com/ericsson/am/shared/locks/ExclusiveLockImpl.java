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
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExclusiveLockImpl extends LockImpl {

    public ExclusiveLockImpl(RedisTemplate<String, String> redisTemplate,
                             BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue, String name, String owner) {
        super(redisTemplate, ttlRefreshTasksQueue, name, owner);
    }

    public ExclusiveLockImpl(RedisTemplate<String, String> redisTemplate,
                             BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue, String name, String owner,
                             long ttl, long defaultTimeout) {
        super(redisTemplate, ttlRefreshTasksQueue, name, owner, ttl, defaultTimeout);
    }

    @Override
    public boolean lock() {
        return lockWithEviction(defaultTimeout);
    }

    @Override
    public boolean lock(long duration, TimeUnit timeUnit) {
        return lockWithEviction(timeUnit.toMillis(duration));
    }

    private boolean lockWithEviction(long timeout) {
        boolean acquired = lockWithRetries(ttl, EXCLUSIVE_RETRY_ATTEMPTS, SHARED_EXCLUSIVE_RETRY_INTERVAL); // TODO enclose with usual retry
        if (!acquired) {
            return false;
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(sharedHashKey))) { // Check if shared lock is owned
            evictExpired();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sharedHashKey))) { // Check again if eviction released all shared lock owners
                redisTemplate.delete(name);
                return false;
            }
        }
        installRefreshTask(timeout, getEffectiveTtl(timeout));
        return true;
    }

    @Override
    public void unlock() {
        String actualOwner = redisTemplate.opsForValue().get(name);
        if (owner.equals(actualOwner)) {
            redisTemplate.delete(name);
        }
    }
}
