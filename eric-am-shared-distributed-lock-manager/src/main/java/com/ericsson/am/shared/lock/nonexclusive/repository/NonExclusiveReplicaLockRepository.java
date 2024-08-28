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
package com.ericsson.am.shared.lock.nonexclusive.repository;

import java.util.Set;

public interface NonExclusiveReplicaLockRepository {
    Set<String> findAll(String key);

    boolean add(String key, String resourceId);

    boolean remove(String key, String resourceId);

    long getTimeMs();
}
