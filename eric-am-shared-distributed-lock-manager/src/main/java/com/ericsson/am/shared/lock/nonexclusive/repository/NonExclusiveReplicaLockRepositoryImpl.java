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

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.ericsson.am.shared.lock.exceptions.RedisCommunicationException;

@Repository
public class NonExclusiveReplicaLockRepositoryImpl implements NonExclusiveReplicaLockRepository {
    @Autowired
    @Qualifier("stringRedisTemplate")
    private RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public Set<String> findAll(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    @Override
    public boolean add(String key, String resourceId) {
        return stringRedisTemplate.opsForSet().add(key, resourceId) != 0;
    }

    @Override
    public boolean remove(String key, String resourceId) {
        return stringRedisTemplate.opsForSet().remove(key, resourceId) != 0;
    }

    @Override
    public long getTimeMs() {
        return Optional.ofNullable(stringRedisTemplate.execute((RedisCallback<Long>) connection -> connection.serverCommands().time()))
                .orElseThrow(() -> new RedisCommunicationException("Failed to obtain server time from Redis."));
    }
}
