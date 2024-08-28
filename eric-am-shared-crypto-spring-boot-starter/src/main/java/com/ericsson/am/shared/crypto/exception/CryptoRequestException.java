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
package com.ericsson.am.shared.crypto.exception;

public class CryptoRequestException extends RuntimeException {
    private static final long serialVersionUID = 4885185148095779199L;

    public CryptoRequestException(String message) {
        super(message);
    }

    public CryptoRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
