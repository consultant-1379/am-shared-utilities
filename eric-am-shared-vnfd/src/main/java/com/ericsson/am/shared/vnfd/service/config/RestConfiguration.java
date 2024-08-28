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
package com.ericsson.am.shared.vnfd.service.config;

import com.ericsson.am.shared.vnfd.service.policy.CryptoSeriveServiceExceptionClassifierRetryPolicy;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class RestConfiguration {
    private final RetryProperties retryProperties = new RetryProperties();

    @Bean("toscaRestTemplate")
    public RestTemplate toscaRestTemplate(RestTemplateBuilder builder) {
        return builder.requestFactory(settings -> new BufferingClientHttpRequestFactory(
                        ClientHttpRequestFactories.get(HttpComponentsClientHttpRequestFactory.class, settings)))
                .setConnectTimeout(Duration.ofSeconds(retryProperties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMinutes(retryProperties.getReadTimeout()))
                .build();
    }

    @Bean("cryptoServiceRestTemplate")
    public RestTemplate cryptoServiceRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.of(retryProperties.getConnectTimeout(), ChronoUnit.MILLIS))
                .setReadTimeout(Duration.of(retryProperties.getReadTimeout(), ChronoUnit.MILLIS))
                .build();
    }

    @Bean("retryToscaTemplate")
    public RetryTemplate retryToscaTemplate() {
        return RetryTemplate.builder()
                .exponentialBackoff(1000, 2.0, 64000)
                .maxAttempts(retryProperties.getMaxAttempts())
                .build();
    }

    @Bean("cryptoServiceRetryTemplate")
    public RetryTemplate cryptoServiceRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProperties.getInitialBackoff());
        backOffPolicy.setMaxInterval(retryProperties.getMaxBackoff());
        backOffPolicy.setMultiplier(retryProperties.getMultiplier());

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(new CryptoSeriveServiceExceptionClassifierRetryPolicy(
                retryProperties.getMaxAttempts()));
        return retryTemplate;
    }
}
