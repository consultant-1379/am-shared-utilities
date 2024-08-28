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
package com.ericsson.am.shared.crypto;

import static com.ericsson.am.shared.crypto.config.CryptoRequestServiceBeanQualifiers.CRYPTO_REQUEST_SERVICE_REST_TEMPLATE;
import static com.ericsson.am.shared.crypto.config.CryptoRequestServiceBeanQualifiers.CRYPTO_REQUEST_SERVICE_RETRY_TEMPLATE;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import com.ericsson.am.shared.crypto.policy.CryptoRequestServiceExceptionClassifierRetryPolicy;
import com.ericsson.am.shared.crypto.config.CryptoRetryProperties;
import com.ericsson.am.shared.crypto.service.CryptoRequestServiceImpl;
import com.ericsson.am.shared.crypto.service.CryptoService;

@AutoConfiguration
public class CryptoRequestServiceRestAutoConfiguration {
    private CryptoRetryProperties retryProperties = new CryptoRetryProperties();


    @Bean(name = CRYPTO_REQUEST_SERVICE_REST_TEMPLATE)
    public RestTemplate cryptoRequestServiceRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.of(retryProperties.getConnectTimeout(), ChronoUnit.MILLIS))
                .setReadTimeout(Duration.of(retryProperties.getReadTimeout(), ChronoUnit.MILLIS))
                .build();
    }


    @Bean(name = CRYPTO_REQUEST_SERVICE_RETRY_TEMPLATE)
    public RetryTemplate cryptoRequestServiceRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProperties.getInitialBackoff());
        backOffPolicy.setMaxInterval(retryProperties.getMaxBackoff());
        backOffPolicy.setMultiplier(retryProperties.getMultiplier());

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(new CryptoRequestServiceExceptionClassifierRetryPolicy(
                retryProperties.getMaxAttempts()));
        return retryTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public CryptoService cryptoService() {
        return new CryptoRequestServiceImpl();
    }

}
