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

public class LockTtlWithinRacingProtectionIntervalException extends Exception {

    private static final String MESSAGE_TEMPLATE = "Lock for resource %s has ttl %sms within "
            + "LOCK_RACING_PROTECTION_INTERVAL_MS (0..%sms).";

    @Serial
    private static final long serialVersionUID = -2352335146676596369L;

    public LockTtlWithinRacingProtectionIntervalException(String resourceId, long ttl, long lockRacingProtectionInterval) {
        super(MESSAGE_TEMPLATE.formatted(resourceId, ttl, lockRacingProtectionInterval));
    }
}