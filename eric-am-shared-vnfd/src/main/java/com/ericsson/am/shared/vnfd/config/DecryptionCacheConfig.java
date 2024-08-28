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
package com.ericsson.am.shared.vnfd.config;

import static com.ericsson.am.shared.vnfd.utils.Constants.CAFFEINE_DECRYPTION_CACHE_NAME;
import static com.ericsson.am.shared.vnfd.utils.Constants.DECRYPTION_CACHE_MANAGER_NAME;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ericsson.am.shared.vnfd.utils.Constants;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Below is a configuration class for caching calls to Crypto service. For enabling this functionality in your service, make sure you've added
 * spring's @EnableCaching annotation and define crypto.cache.enabled=true (or create an implementation of DecryptionCacheEnablingConfig,
 * and mark it as @Primary, see example in vnfm-orchestrator).
 */
@Configuration
public class DecryptionCacheConfig {
    @Bean(name = DECRYPTION_CACHE_MANAGER_NAME)
    @Primary
    public CacheManager decryptionCacheManager(@Qualifier(CAFFEINE_DECRYPTION_CACHE_NAME) Cache<Object, Object> cache) {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager(Constants.DECRYPTION_CACHE_NAME);
        cacheManager.registerCustomCache(Constants.DECRYPTION_CACHE_NAME, cache);
        return cacheManager;
    }

    @Bean(name = CAFFEINE_DECRYPTION_CACHE_NAME)
    public Cache<Object, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .softValues()
                .build();
    }
}
