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
package com.ericsson.am.shared.vnfd.service;

import com.ericsson.am.shared.vnfd.config.DecryptionCacheConfig;
import com.ericsson.am.shared.vnfd.config.DecryptionCacheEnablingConfigImpl;
import com.ericsson.am.shared.vnfd.service.config.RestConfiguration;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@TestPropertySource(
        properties = {
            "crypto.host = test",
            "crypto.cache.enabled = true"
        })
@ActiveProfiles({ "prod" })
@ExtendWith(SpringExtension.class)
@EnableCaching
@ContextConfiguration(
        classes = { CryptoServiceImpl.class, RestConfiguration.class, DecryptionCacheConfig.class, DecryptionCacheEnablingConfigImpl.class })
class CryptoServiceWithCachingTest {

    private static final String PLAIN_TEXT = "test-input-string";
    private static final String CIPHER_TEXT = "tzC7SBTCBYo4QIvObbLWTHI=";

    @MockBean
    @Qualifier("cryptoServiceRestTemplate")
    private RestTemplate restTemplate;

    @SpyBean
    @Qualifier("cryptoServiceRetryTemplate")
    private RetryTemplate retryTemplate;

    @MockBean
    @Qualifier("toscaRestTemplate")
    private RestTemplate toscaRestTemplate;

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private CryptoService cryptoService;

    @BeforeEach
    public void setUp() {
        mockDecryption();
        mockEncryption();
    }

    private void mockEncryption() {
        EncryptionResponse encryptionResponse = new EncryptionResponse(CIPHER_TEXT);
        ResponseEntity<EncryptionResponse> response = ResponseEntity.of(Optional.of(encryptionResponse));
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class)))
                .thenReturn(response);
    }

    private void mockDecryption() {
        DecryptionResponse decryptionResponse = new DecryptionResponse(PLAIN_TEXT);
        ResponseEntity<DecryptionResponse> response = ResponseEntity.of(Optional.of(decryptionResponse));
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class)))
                .thenReturn(response);
    }

    @Test
    public void testCaching() {

        final String stringToCache = RandomStringUtils.random(100, true, true);
        final String stringToCache2 = RandomStringUtils.random(100, true, true);

        cryptoService.decryptString(stringToCache); // do call and cache the result
        cryptoService.decryptString(stringToCache); // should not perform the real logic, just fetch data from cache
        cryptoService.decryptString(stringToCache2); // do call with another input parameter, it's absent in cache, se call will be performed

        verify(restTemplate, times(2))
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class));
    }

    @Test
    public void testCachingWhenEncrypt() {
        final String stringToCache = RandomStringUtils.random(100, true, true);

        final String encryptedString = cryptoService.encryptString(stringToCache); // cached during this call
        cryptoService.decryptString(encryptedString); // should not do real call, since the result was cached during encryption

        verify(restTemplate, times(1))
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class));
        verifyNoMoreInteractions(restTemplate);
    }
}