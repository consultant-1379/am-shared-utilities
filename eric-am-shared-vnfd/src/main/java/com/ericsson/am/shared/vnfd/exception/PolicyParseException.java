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
package com.ericsson.am.shared.vnfd.exception;

import java.io.Serial;

public class PolicyParseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1122795961656396970L;

    public PolicyParseException(String keyObject) {
        super(String.format("Unable to parse policy type %s from VNFD", keyObject));
    }

    public PolicyParseException(String keyObject, Exception exception) {
        super(String.format("Unable to parse policy type %s from VNFD with exception message %s", keyObject, exception.getMessage()), exception);
    }

    public PolicyParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyParseException(Throwable cause) {
        super(cause);
    }
}
