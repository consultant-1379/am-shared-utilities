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
package com.ericsson.am.shared.lock.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> redisContainer =
                new GenericContainer<>(DockerImageName.parse("armdocker.rnd.ericsson.se/dockerhub-ericsson-remote/redis:5.0.3-alpine")
                                               .asCompatibleSubstituteFor("redis"))
                        .withExposedPorts(6379)
                        .withReuse(true);
        redisContainer.start();
        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getFirstMappedPort().toString());
        System.setProperty("spring.data.redis.username", "");
        System.setProperty("spring.data.redis.password", "");
        System.setProperty("redis.acl.enabled", "false");
        return redisContainer;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(GenericContainer<?> redisContainer) {
        return new LettuceConnectionFactory(redisContainer.getHost(), redisContainer.getFirstMappedPort());
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        return new StringRedisTemplate(redisConnectionFactory);
    }

}
