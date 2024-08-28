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
package com.ericsson.am.shared.lock;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class LockConstructor {

    private static final String TRANSACTION_LOCK_VALUE_FORMAT = "%s:%s:%s:%s";
    private static final String THREAD_LOCK_VALUE_FORMAT = "%s:%s";

    public String construct(String invoker, String replicaName) {
        String threadName = Thread.currentThread().getName();
        String operationId = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT).replace(":", "");

        return TRANSACTION_LOCK_VALUE_FORMAT.formatted(replicaName, threadName, invoker, operationId);
    }

    public String construct(String replicaName) {
        String threadName = Thread.currentThread().getName();
        return THREAD_LOCK_VALUE_FORMAT.formatted(replicaName, threadName);
    }
}
