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

import com.ericsson.am.shared.lock.models.Lock;
import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;

public class ProcessingTooLongException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2205507355722466158L;

    public ProcessingTooLongException(String message, Lock lock) {
        super("%s. Lock: %s".formatted(message, lock));
    }
    public ProcessingTooLongException(String message, NonExclusiveLock lock) {
        super("%s. Lock: %s".formatted(message, lock));
    }


    public ProcessingTooLongException(String message, String resourceId, String holder) {
        super("%s. resourceId: %s, holder: %s".formatted(message, resourceId, holder));
    }
}
