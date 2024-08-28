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
package com.ericsson.am.shared.lock.nonexclusive;

import java.util.Set;

import com.ericsson.am.shared.lock.exceptions.LockDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.LockHasNoTtlException;
import com.ericsson.am.shared.lock.exceptions.LockTtlWithinRacingProtectionIntervalException;
import com.ericsson.am.shared.lock.models.LockParameters;

public interface NonExclusiveDistributedLockManager {
    boolean acquire(String resourceId, String holder, int duration, String transferringHolder, int priority,
                    Set<String> sharingGroups, LockParameters lockParameters);

    void updateDuration(String resourceId, String holder, int duration, LockParameters lockParameters);

    void release(String resourceId, String holder, LockParameters lockParameters);

    void refreshAll(LockParameters lockParameters);

    void refresh(String resourceId, boolean mayNotExist, boolean isGuaranteedNotDeletedInParallel, LockParameters lockParameters);

    long getResidualLockDurationMs(String resourceId, String holder, LockParameters lockParameters);

    long getResidualLockTtlMs(String resourceId, boolean allowLockProtectionInterval, LockParameters lockParameters)
            throws LockTtlWithinRacingProtectionIntervalException, LockDoesNotExistException, LockHasNoTtlException;
}
