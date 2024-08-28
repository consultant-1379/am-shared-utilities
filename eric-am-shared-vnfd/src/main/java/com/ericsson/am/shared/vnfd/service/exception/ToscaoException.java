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
package com.ericsson.am.shared.vnfd.service.exception;

import java.io.Serial;

public class ToscaoException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3034924109486878628L;

    public ToscaoException(final String message) {
        super(message);
    }

    public ToscaoException(final String message, Throwable cause) {
        super(message, cause);
    }
}
