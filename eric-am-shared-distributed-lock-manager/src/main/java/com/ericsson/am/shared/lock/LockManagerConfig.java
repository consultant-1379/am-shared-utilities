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
package com.ericsson.am.shared.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class LockManagerConfig {

    @Bean
    public RedisTemplate<String, Long> lockPriorityRedisTemplate(final RedisConnectionFactory connectionFactory, final ObjectMapper objectMapper) {

        return buildRedisTemplate(connectionFactory, objectMapper, Long.class);
    }

    @Bean
    public RedisTemplate<String, NonExclusiveLock> nonExclusiveLockRedisTemplate(final RedisConnectionFactory connectionFactory,
                                                                      final ObjectMapper objectMapper) {
        return buildRedisTemplate(connectionFactory, objectMapper, NonExclusiveLock.class);
    }

    private static <T> RedisTemplate<String, T> buildRedisTemplate(final RedisConnectionFactory connectionFactory, final ObjectMapper objectMapper,
            final Class<T> valueClass) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, valueClass));
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }
}
