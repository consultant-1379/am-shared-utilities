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
package com.ericsson.am.shared.crypto.service;

import static com.ericsson.am.shared.crypto.config.CryptoRequestServiceBeanQualifiers.CRYPTO_REQUEST_SERVICE_REST_TEMPLATE;
import static com.ericsson.am.shared.crypto.config.CryptoRequestServiceBeanQualifiers.CRYPTO_REQUEST_SERVICE_RETRY_TEMPLATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

import com.ericsson.am.shared.crypto.exception.CryptoRequestException;
import com.ericsson.am.shared.crypto.exception.RequestServiceUnavailableException;
import com.ericsson.eo.evnfm.crypto.model.DecryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.google.common.base.Strings;


@Service
public class CryptoRequestServiceImpl implements CryptoService {
    public static final String SERVICE_NAME = "Crypto";
    private static final String DECRYPTION_URL = "%s/generic/v1/decryption";
    private static final String ENCRYPTION_URL = "%s/generic/v1/encryption";
    public static final String UNABLE_TO_DECRYPT_DATA_MESSAGE = "Unable to decrypt data";
    public static final String UNABLE_TO_ENCRYPT_DATA_MESSAGE = "Unable to encrypt data";
    public static final String CRYPTO_SERVICE_UNAVAILABLE_MESSAGE = "Crypto service unavailable";


    @Value("${crypto.host}")
    private String cryptoHost;

    @Autowired
    @Qualifier(CRYPTO_REQUEST_SERVICE_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier(CRYPTO_REQUEST_SERVICE_RETRY_TEMPLATE)
    private RetryTemplate retryTemplate;

    @Override
    public String decryptString(String data) {
        if (Strings.isNullOrEmpty(data)) {
            return data;
        }
        final HttpEntity<DecryptionPostRequest> request = getDecryptionRequestHttpEntity(data);
        try {
            return executeDecryptionRequest(request);
        } catch (final HttpServerErrorException | ResourceAccessException e) { // NOSONAR
            throw new RequestServiceUnavailableException(CRYPTO_SERVICE_UNAVAILABLE_MESSAGE, e);
        } catch (final Exception e) {
            throw new CryptoRequestException(UNABLE_TO_DECRYPT_DATA_MESSAGE, e);
        }
    }

    private String executeDecryptionRequest(final HttpEntity<DecryptionPostRequest> request) {
        final ResponseEntity<DecryptionResponse> response = retryTemplate
                .execute(context -> restTemplate.exchange(String.format(DECRYPTION_URL, cryptoHost), HttpMethod.POST, request,
                                                          DecryptionResponse.class));
        DecryptionResponse decryptionResponse = response.getBody();
        if (decryptionResponse == null) {
            throw new CryptoRequestException("Decrypt response body is null.");
        }
        return decryptionResponse.getPlaintext();
    }

    private static HttpEntity<DecryptionPostRequest> getDecryptionRequestHttpEntity(final String data) {
        DecryptionPostRequest decryptionRequest = new DecryptionPostRequest(data);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        return new HttpEntity<>(decryptionRequest, headers);
    }

    @Override
    public String encryptString(String data) {
        if (Strings.isNullOrEmpty(data)) {
            return data;
        }
        final HttpEntity<EncryptionPostRequest> request = getEncryptionRequestHttpEntity(data);
        try {
            return executeEncryptionRequest(request);
        } catch (final HttpServerErrorException | ResourceAccessException e) { // NOSONAR
            throw new RequestServiceUnavailableException(CRYPTO_SERVICE_UNAVAILABLE_MESSAGE, e);
        } catch (final Exception e) {
            throw new CryptoRequestException(UNABLE_TO_ENCRYPT_DATA_MESSAGE, e);
        }
    }

    private String executeEncryptionRequest(final HttpEntity<EncryptionPostRequest> request) {
        String requestUrl = String.format(ENCRYPTION_URL, cryptoHost);
        final ResponseEntity<EncryptionResponse> response = retryTemplate
                .execute(context -> restTemplate.exchange(
                        requestUrl, HttpMethod.POST, request, EncryptionResponse.class));
        EncryptionResponse encryptionResponse = response.getBody();
        if (encryptionResponse == null) {
            throw new CryptoRequestException("Encrypt response body is null");
        }
        return encryptionResponse.getCiphertext();
    }

    private static HttpEntity<EncryptionPostRequest> getEncryptionRequestHttpEntity(final String data) {
        EncryptionPostRequest encryptionRequest = new EncryptionPostRequest(data);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        return new HttpEntity<>(encryptionRequest, headers);
    }
}
