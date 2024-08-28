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
package com.ericsson.am.shared.locks;

import lombok.Getter;
import lombok.Setter;

@Getter
class LockTtlRefreshTask {
    private final String name;
    private final String owner;
    private final LockType type;
    @Setter
    private long expiration;
    @Setter
    private long ttl;

    LockTtlRefreshTask(String name, String owner, LockType type) {
        this.name = name;
        this.owner = owner;
        this.type = type;
    }
}
