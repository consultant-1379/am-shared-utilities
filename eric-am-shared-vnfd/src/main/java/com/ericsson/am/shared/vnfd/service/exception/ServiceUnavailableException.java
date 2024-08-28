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

public class ServiceUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6561990874772226506L;

    private final String service;

    public ServiceUnavailableException(final String service, final String message, final Throwable cause) {
        super(message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
