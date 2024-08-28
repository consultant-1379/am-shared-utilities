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

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
class GroupExclusiveLockImpl extends AbstractLock {
    static final String GROUP_KEY_SUFFIX = "/group";
    private final String group;
    private final String groupKey;

    GroupExclusiveLockImpl(RedisTemplate<String, String> redisTemplate,
                                  BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue,
                                  String name, String owner, String group) {
        super(redisTemplate, ttlRefreshTasksQueue, LockType.SHARED, name, owner);
        Objects.requireNonNull(group, "Group name for group exclusive lock can't be null");
        this.group = group;
        this.groupKey = name + GROUP_KEY_SUFFIX;
    }

    GroupExclusiveLockImpl(RedisTemplate<String, String> redisTemplate,
                                  BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue,
                                  String owner, String name, String group, long ttl, long defaultTimeout) {
        super(redisTemplate, ttlRefreshTasksQueue, LockType.SHARED, owner, name, ttl, defaultTimeout);
        this.group = group;
        this.groupKey = name + GROUP_KEY_SUFFIX;
    }

    @Override
    public boolean lock() {
        return lockInternal(this.defaultTimeout);
    }

    @Override
    public boolean lock(long duration, TimeUnit timeUnit) {
        return lockInternal(timeUnit.toMillis(duration));
    }

    private boolean lockInternal(long timeout) {
        boolean acquired = lockWithRetries(this.ttl, this.maxAttempts, this.retryInterval);
        if (!acquired) {
            return false;
        }
        evictExpired();
        boolean ownedByGroup = group.equals(redisTemplate.opsForValue().get(groupKey));
        boolean success = ownedByGroup || !Boolean.TRUE.equals(redisTemplate.hasKey(sharedHashKey));
        if (success) {
            long now = System.currentTimeMillis();
            long expiration = now + Long.min(this.ttl, timeout);
            if (!ownedByGroup) {
                redisTemplate.opsForValue().set(groupKey, group);
            }
            redisTemplate.opsForHash().put(sharedHashKey, owner, expiration);
            long effectiveTtl = getEffectiveTtl(timeout);
            if (effectiveTtl + LocksTtlRefresher.PROLONGATION_TRESHOLD < timeout) {
                installRefreshTask(timeout, effectiveTtl);
            }
        }
        redisTemplate.delete(name);
        return success;
    }

    @Override
    public void unlock() {
        HashOperations<String, String, Long> ownersTimeoutsOps = redisTemplate.opsForHash();
        ownersTimeoutsOps.delete(sharedHashKey, owner);
        evictExpired();
    }
}
