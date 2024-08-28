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

import static com.ericsson.am.shared.vnfd.utils.Constants.DECRYPTION_CACHE_MANAGER_NAME;
import static com.ericsson.am.shared.vnfd.utils.Constants.DECRYPTION_CACHE_NAME;

import com.ericsson.eo.evnfm.crypto.model.DecryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.EncryptionPostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.am.shared.vnfd.config.DecryptionCacheEnablingConfig;
import com.ericsson.am.shared.vnfd.service.exception.CryptoException;
import com.ericsson.am.shared.vnfd.service.exception.ServiceUnavailableException;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.google.common.base.Strings;

@Service
@Profile({ "prod" })
public class CryptoServiceImpl implements CryptoService {
    public static final String SERVICE_NAME = "Crypto";
    public static final String CRYPTO_SERVICE_UNAVAILABLE_MESSAGE = "Crypto service unavailable";
    public static final String UNABLE_TO_ENCRYPT_DATA_MESSAGE = "Unable to encrypt data";
    public static final String UNABLE_TO_DECRYPT_DATA_MESSAGE = "Unable to decrypt data";

    private static final String ENCRYPTION_URL = "%s/generic/v1/encryption";
    private static final String DECRYPTION_URL = "%s/generic/v1/decryption";


    private final String cryptoHost;

    private final RestTemplate restTemplate;

    private final RetryTemplate retryTemplate;

    private final Cache decryptionCache;

    private final DecryptionCacheEnablingConfig decryptionCacheEnablingConfig;

    @Autowired
    public CryptoServiceImpl(@Value("${crypto.host}") final String cryptoHost,
                             @Qualifier("cryptoServiceRestTemplate") final RestTemplate restTemplate,
                             @Qualifier("cryptoServiceRetryTemplate") final RetryTemplate retryTemplate,
                             final DecryptionCacheEnablingConfig decryptionCacheEnablingConfig,
                             @Qualifier(DECRYPTION_CACHE_MANAGER_NAME) final CacheManager cacheManager) {
        this.cryptoHost = cryptoHost;
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.decryptionCacheEnablingConfig = decryptionCacheEnablingConfig;
        this.decryptionCache = cacheManager.getCache(DECRYPTION_CACHE_NAME);
    }

    @Override
    public String encryptString(String data) {
        if (Strings.isNullOrEmpty(data)) {
            return data;
        }
        final String encryptedString = doEncryptString(data);
        addToCache(data, encryptedString);
        return encryptedString;
    }

    private String doEncryptString(final String data) {
        EncryptionPostRequest encryptionRequest = new EncryptionPostRequest(data);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        final HttpEntity<EncryptionPostRequest> request = new HttpEntity<>(encryptionRequest, headers);
        try {
            String requestUrl = String.format(ENCRYPTION_URL, cryptoHost);
            final ResponseEntity<EncryptionResponse> response = retryTemplate
                    .execute(context -> restTemplate.exchange(
                            requestUrl, HttpMethod.POST, request, EncryptionResponse.class));
            EncryptionResponse encryptionResponse = response.getBody();
            if (encryptionResponse == null) {
                throw new CryptoException("Encrypt response body is null");
            }
            return encryptionResponse.getCiphertext();
        } catch (final HttpServerErrorException | ResourceAccessException e) { // NOSONAR
            throw new ServiceUnavailableException(SERVICE_NAME, CRYPTO_SERVICE_UNAVAILABLE_MESSAGE, e);
        } catch (final Exception e) {
            throw new CryptoException(UNABLE_TO_ENCRYPT_DATA_MESSAGE, e);
        }
    }

    @Cacheable(value = DECRYPTION_CACHE_NAME, cacheManager = DECRYPTION_CACHE_MANAGER_NAME, condition = "#root.target.isDecryptionCacheEnabled()")
    @Override
    public String decryptString(String data) {
        if (Strings.isNullOrEmpty(data)) {
            return data;
        }
        DecryptionPostRequest decryptionRequest = new DecryptionPostRequest(data);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        final HttpEntity<DecryptionPostRequest> request = new HttpEntity<>(decryptionRequest, headers);
        try {
            final ResponseEntity<DecryptionResponse> response = retryTemplate
                    .execute(context -> restTemplate.exchange(String.format(DECRYPTION_URL, cryptoHost), HttpMethod.POST, request,
                            DecryptionResponse.class));
            DecryptionResponse decryptionResponse = response.getBody();
            if (decryptionResponse == null) {
                throw new CryptoException("Decrypt response body is null.");
            }
            return decryptionResponse.getPlaintext();
        } catch (final HttpServerErrorException | ResourceAccessException e) { // NOSONAR
            throw new ServiceUnavailableException(SERVICE_NAME, CRYPTO_SERVICE_UNAVAILABLE_MESSAGE, e);
        } catch (final Exception e) {
            throw new CryptoException(UNABLE_TO_DECRYPT_DATA_MESSAGE, e);
        }
    }

    private void addToCache(final String data, final String encryptedString) {
        if (decryptionCache != null && encryptedString != null && isDecryptionCacheEnabled()) {
            decryptionCache.put(encryptedString, data);
        }
    }

    public boolean isDecryptionCacheEnabled() {
        return decryptionCacheEnablingConfig.isDecryptionCacheEnabled();
    }
}
