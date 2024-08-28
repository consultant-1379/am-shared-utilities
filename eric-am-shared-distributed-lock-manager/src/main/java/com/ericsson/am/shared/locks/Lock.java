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

import java.util.concurrent.TimeUnit;

/**
 * Generic interface of distributed lock, offering quite simple methods for acquiring and releasing a lock
 * along with methods to configure some parameters. For now only one such method
 *
 * <p>Exclusive lock that has at most one owner at any given moment.
 * Shared/exclusive lock has two modes similar to {@link java.util.concurrent.locks.ReadWriteLock} read and write locks</p>
 * Implementation should be obtained from LockManager via getLock() call.
 * Lock have a timeout after which it is released automatically.
 * Provided implementation uses Redis key with a short ttl which is prolonged by a separate thread
 * until timeout is reached. Once application replica shuts down (even ungracefully) all locks it owns
 * will be released automatically on ttl expiry, which is typically much faster than timeout expiration.
 * Default value for both ttl and timeout is 3s.
 */
public interface Lock {

    /**
     * Enables retries for lock acquisition.
     * @param attempts max number of retries to acquire
     * @param retryInterval interval between acquire attempts in milliseconds
     * @return the same lock instance, thus allowing one line construction like<br/>
     * {@code Lock lock = lockManager.getLock("resource").withAcquireRetries(10, 500L);}
     */
    Lock withAcquireRetries(int attempts, long retryInterval);

    /**
     * Attempts to acquire a lock.
     * @return true if attempt was successful, false otherwise
     */
    boolean lock();

    /**
     * Attempts to acquire a lock with a given timeout.
     * Note that for timeout longer than 30s ttl is also adjusted to be 5s.
     *
     * @param duration lock timeout duration
     * @param timeUnit time unit in which timeout is specified
     * @return true if lock is acquired
     */
    boolean lock(long duration, TimeUnit timeUnit);

    /**
     * Releases the lock
     */
    void unlock();
}
