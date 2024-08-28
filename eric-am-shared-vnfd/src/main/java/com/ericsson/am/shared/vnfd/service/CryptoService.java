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

/**
 * Service to encrypt and decrypt node sensitive data and save it as a key value in
 * column sensitive_info in VnfInstance table
 */
public interface CryptoService {

    /**
     * Encrypt the data provided
     *
     * @param data data to be encrypted
     * @return encrypted string
     */
    String encryptString(String data);

    /**
     * Decrypt the data provided
     *
     * @param data data to be decrypted
     * @return decrypted string
     */
    String decryptString(String data);

}
