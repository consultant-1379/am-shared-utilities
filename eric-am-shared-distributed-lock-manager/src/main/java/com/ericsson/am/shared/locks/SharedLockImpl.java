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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
class SharedLockImpl extends AbstractLock {

    SharedLockImpl(RedisTemplate<String, String> redisTemplate,
                          BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue, String name, String owner) {
        super(redisTemplate, ttlRefreshTasksQueue, LockType.SHARED, name, owner);
    }

    SharedLockImpl(RedisTemplate<String, String> redisTemplate,
                          BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue, String name, String owner,
                          long ttl, long defaultTimeout) {
        super(redisTemplate, ttlRefreshTasksQueue, LockType.SHARED, name, owner, ttl, defaultTimeout);
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
        boolean exclusiveAcquired = lockWithRetries(SHARED_EXCLUSIVE_TTL,
                SHARED_EXCLUSIVE_RETRY_ATTEMPTS, SHARED_EXCLUSIVE_RETRY_INTERVAL); // TODO enclose with usual retry
        boolean result = exclusiveAcquired;
        if (exclusiveAcquired) {
            HashOperations<String, String, Long> ownersTimeoutsOps = redisTemplate.opsForHash();
            // Place a timeout record into owners hash
            long now = System.currentTimeMillis();
            ownersTimeoutsOps.putIfAbsent(sharedHashKey, owner, now + ttl + 100L); // Put a bit longer ttl to avoid timing issues
            if (!installRefreshTask(timeout, getEffectiveTtl(timeout))) {
                ownersTimeoutsOps.delete(sharedHashKey, owner); // Delete owners timeout record
                result = false;
            }
            redisTemplate.delete(name); // Release exclusive lock
        }
        evictExpired();
        return result;
    }

    @Override
    public void unlock() {
        HashOperations<String, String, Long> ownersTimeoutsOps = redisTemplate.opsForHash();
        ownersTimeoutsOps.delete(sharedHashKey, owner);
        evictExpired();
    }
}
