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
package com.ericsson.am.shared.lock.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LockPriorityRepositoryImpl implements LockPriorityRepository {

    @Autowired
    @Qualifier("lockPriorityRedisTemplate")
    private RedisTemplate<String, Long> redisTemplate;

    @Override
    public void set(String key, Integer priority, Long expirationTime) {
        redisTemplate.opsForHash().put(key, priority, expirationTime);
    }

    @Override
    public Map<Integer, Long> getAllByKey(final String key) {
        final BoundHashOperations<String, Integer, Long> hashOps = redisTemplate.boundHashOps(key);
        return hashOps.entries();
    }
}
