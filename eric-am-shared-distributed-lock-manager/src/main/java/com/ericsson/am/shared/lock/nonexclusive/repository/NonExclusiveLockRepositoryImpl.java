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
package com.ericsson.am.shared.lock.nonexclusive.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.ericsson.am.shared.lock.exceptions.RedisInternalException;
import com.ericsson.am.shared.lock.exceptions.RedisInvalidValueException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyHasNoTtlException;
import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;

/**
 * Lock repository contains a map of locks by holders.
 */
@Repository
public class NonExclusiveLockRepositoryImpl implements NonExclusiveLockRepository {

    @Autowired
    @Qualifier("nonExclusiveLockRedisTemplate")
    private RedisTemplate<String, NonExclusiveLock> lockRedisTemplate;

    @Override
    public void put(String key, String holder, NonExclusiveLock lock) {
        lockRedisTemplate.opsForHash().put(key, holder, lock);
    }

    @Override
    public boolean putIfAbsent(String key, String holder, NonExclusiveLock lock) {
        return lockRedisTemplate.opsForHash().putIfAbsent(key, holder, lock);
    }

    @Override
    public Optional<NonExclusiveLock> get(String key, String holder) {
        final BoundHashOperations<String, String, NonExclusiveLock> hashOps = lockRedisTemplate.boundHashOps(key);
        NonExclusiveLock lock = hashOps.get(holder);
        return Optional.ofNullable(lock);
    }

    @Override
    public Map<String, NonExclusiveLock> getAll(final String key) {
        final BoundHashOperations<String, String, NonExclusiveLock> hashOps = lockRedisTemplate.boundHashOps(key);
        return hashOps.entries();
    }

    @Override
    public Set<String> getHolders(final String key) {
        final BoundHashOperations<String, String, NonExclusiveLock> hashOps = lockRedisTemplate.boundHashOps(key);
        final Set<String> holders = hashOps.keys();
        if (CollectionUtils.isEmpty(holders)) {
            return new HashSet<>();
        }
        return new HashSet<>(holders);
    }

    @Override
    public long delete(String key, String holder) {
        return lockRedisTemplate.opsForHash().delete(key, holder);
    }

    @Override
    public boolean delete(String key) {
        return lockRedisTemplate.delete(key);
    }

    @Override
    public long getTtlMs(String key) throws RedisKeyDoesNotExistException, RedisKeyHasNoTtlException {
        long ttl = lockRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        if (ttl == -2) {
            throw new RedisKeyDoesNotExistException("Redis key %s not found: TTL returns -2".formatted(key));
        } else if (ttl == -1) {
            throw new RedisKeyHasNoTtlException("Redis key %s has no ttl: TTL returns -1".formatted(key));
        } else if (ttl < 0) {
            throw new RedisInternalException("Redis key %s has invalid ttl: TTL returns %s. This should never happen."
                                                     .formatted(key, ttl));
        }
        return ttl;
    }

    @Override
    public void setTtlMs(final String key, final long timeToLive) throws RedisKeyDoesNotExistException {
        if (timeToLive <= 0) {
            throw new RedisInvalidValueException("Redis key %s ttl should be positive number, but %s provided".formatted(key, timeToLive));
        }
        boolean isSet = lockRedisTemplate.expire(key, timeToLive, TimeUnit.MILLISECONDS);
        if (!isSet) {
            throw new RedisKeyDoesNotExistException("Redis key %s not found: EXPIRE returns 0".formatted(key));
        }
    }
}
