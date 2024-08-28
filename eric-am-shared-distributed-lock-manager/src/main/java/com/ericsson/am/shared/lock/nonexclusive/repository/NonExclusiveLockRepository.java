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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ericsson.am.shared.lock.exceptions.RedisKeyDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyHasNoTtlException;
import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;

public interface NonExclusiveLockRepository {

    void put(String key, String holder, NonExclusiveLock lock);

    boolean putIfAbsent(String key, String holder, NonExclusiveLock lock);

    Optional<NonExclusiveLock> get(String key, String holder);

    Map<String, NonExclusiveLock> getAll(String key);

    Set<String> getHolders(String key);

    long delete(String key, String holder);

    boolean delete(String key);

    long getTtlMs(String key) throws RedisKeyDoesNotExistException, RedisKeyHasNoTtlException;

    void setTtlMs(String key, long timeToLive) throws RedisKeyDoesNotExistException;
}
