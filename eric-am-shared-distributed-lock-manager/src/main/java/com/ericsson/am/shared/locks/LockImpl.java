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
class LockImpl extends AbstractLock {

    LockImpl(RedisTemplate<String, String> redisTemplate, BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue,
             String name, String owner) {
        super(redisTemplate, ttlRefreshTasksQueue, LockType.EXCLUSIVE, name, owner);
    }

    LockImpl(RedisTemplate<String, String> redisTemplate, BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue,
             String name, String owner, long ttl, long defaultTimeout) {
        super(redisTemplate, ttlRefreshTasksQueue, LockType.EXCLUSIVE, name, owner, ttl, defaultTimeout);
    }

    @Override
    public boolean lock() {
        return lock(ttl, defaultTimeout);
    }

    @Override
    public boolean lock(long duration, TimeUnit timeUnit) {
        long timeout = timeUnit.toMillis(duration);
        return lock(getEffectiveTtl(timeout), timeout);
    }

    private boolean lock(final long ttl, final long timeout) {
        if (lockWithRetries(ttl, this.maxAttempts, this.retryInterval)) {
            if (installRefreshTask(timeout, ttl)) {
                return true;
            } else { // NOSONAR
                redisTemplate.delete(name);
                return false;
            }
        }
        return false;
    }

    @Override
    public void unlock() {
        String actualOwner = redisTemplate.opsForValue().get(name);
        if (owner.equals(actualOwner)) {
            redisTemplate.delete(name);
        }
    }
}
