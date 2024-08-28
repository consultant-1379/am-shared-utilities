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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.ericsson.am.shared.crypto.service.CryptoRequestServiceImpl.CRYPTO_SERVICE_UNAVAILABLE_MESSAGE;
import static com.ericsson.am.shared.crypto.service.CryptoRequestServiceImpl.UNABLE_TO_DECRYPT_DATA_MESSAGE;
import static com.ericsson.am.shared.crypto.service.CryptoRequestServiceImpl.UNABLE_TO_ENCRYPT_DATA_MESSAGE;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.am.shared.crypto.exception.CryptoRequestException;
import com.ericsson.am.shared.crypto.exception.RequestServiceUnavailableException;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.ericsson.am.shared.crypto.service.CryptoService;

@TestPropertySource(
        properties = {
            "crypto.host = test"
        })
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CryptoRequestServiceRestAutoConfiguration.class})
class CryptoServiceTest {

    private static final String PLAIN_TEXT = "test-input-string";
    private static final String CIPHER_TEXT = "tzC7SBTCBYo4QIvObbLWTHI=";

    @MockBean
    @Qualifier("cryptoServiceRestTemplate")
    private RestTemplate restTemplate;

    @SpyBean
    @Qualifier("cryptoServiceRetryTemplate")
    private RetryTemplate retryTemplate;

    @Autowired
    private CryptoService cryptoService;

    @Test
    public void testEncryptString() {
        EncryptionResponse encryptionResponse = new EncryptionResponse(CIPHER_TEXT);
        ResponseEntity<EncryptionResponse> response = ResponseEntity.of(Optional.of(encryptionResponse));
        when(restTemplate
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class)))
                .thenReturn(response);

        String actual = cryptoService.encryptString(PLAIN_TEXT);
        assertThat(actual).isEqualTo(CIPHER_TEXT);

        verify(restTemplate, atLeastOnce()).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class));
    }

    @Test
    public void testEncryptStringServerUnavailable() {
        when(restTemplate
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class)))
                .thenThrow(HttpServerErrorException.class);

        RequestServiceUnavailableException exception = assertThrows(
                RequestServiceUnavailableException.class, () -> cryptoService.encryptString(PLAIN_TEXT));

        assertThat(exception.getMessage()).isEqualTo(CRYPTO_SERVICE_UNAVAILABLE_MESSAGE);

        verify(restTemplate, atLeast(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class));
    }

    @Test
    public void testEncryptStringWithNullResponseBody() {
        when(restTemplate
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        CryptoRequestException exception = assertThrows(
                CryptoRequestException.class, () -> cryptoService.encryptString(PLAIN_TEXT));

        assertThat(exception.getMessage()).isEqualTo(UNABLE_TO_ENCRYPT_DATA_MESSAGE);

        verify(restTemplate, atLeast(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(EncryptionResponse.class));
    }

    @Test
    public void testDecryptString() {
        DecryptionResponse decryptionResponse = new DecryptionResponse(PLAIN_TEXT);
        ResponseEntity<DecryptionResponse> response = ResponseEntity.of(Optional.of(decryptionResponse));

        when(restTemplate
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class)))
                .thenReturn(response);

        String actual = cryptoService.decryptString(CIPHER_TEXT);

        assertThat(actual).isEqualTo(PLAIN_TEXT);

        verify(restTemplate, atLeast(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class));
    }

    @Test
    public void testDecryptStringServerUnavailable() {
        when(restTemplate
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class)))
                .thenThrow(HttpServerErrorException.class);

        RequestServiceUnavailableException exception = assertThrows(
                RequestServiceUnavailableException.class, () -> cryptoService.decryptString(CIPHER_TEXT));

        assertThat(exception.getMessage()).isEqualTo("Crypto service unavailable");

        verify(restTemplate, atLeast(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class));
    }

    @Test
    public void testDecryptStringWithNullResponseBody() {
        when(restTemplate
                .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class)))
                .thenReturn(ResponseEntity.of(Optional.empty()));

        CryptoRequestException exception = assertThrows(
                CryptoRequestException.class, () -> cryptoService.decryptString(CIPHER_TEXT));

        assertThat(exception.getMessage()).isEqualTo(UNABLE_TO_DECRYPT_DATA_MESSAGE);

        verify(restTemplate, atLeast(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(DecryptionResponse.class));
    }
}