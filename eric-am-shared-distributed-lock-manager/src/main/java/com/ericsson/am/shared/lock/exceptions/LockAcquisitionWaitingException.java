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
package com.ericsson.am.shared.lock.exceptions;

import java.io.Serial;

public class LockAcquisitionWaitingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3273767554418976726L;

    public LockAcquisitionWaitingException(String message) {
        super(message);
    }
}