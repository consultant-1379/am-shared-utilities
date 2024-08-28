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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

// This is not a most stupid test class, I just hope it is stupid enough...
@ExtendWith(MockitoExtension.class)
public class LockManagerTest {
    private static final String KEYSPACE = "CVNFM::service:";
    private static final String REPLICA = "service-pod-for-the-sake-of-God";
    private static final String RESOURCE = "useless";
    private static final String OWNER = "Captain Jack Sparrow";
    private static final String GROUP = "bastards";

    private static final BlockingQueue<LockTtlRefreshTask> TTL_REFRESH_TASK_QUEUE = new LinkedBlockingQueue<>();

    @Mock
    private RedisConnectionFactory connectionFactory;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testGetSimpleLock() {
        LockManager lockManager = new LockManager(connectionFactory, objectMapper, KEYSPACE, REPLICA);
        Lock lock = lockManager.getLock(RESOURCE);
        assertTrue(lock instanceof LockImpl);
        lock = lockManager.getLock(RESOURCE, OWNER);
        assertTrue(lock instanceof LockImpl);
        lock = lockManager.getLock(RESOURCE, OWNER, 5L, TimeUnit.SECONDS, 4, TimeUnit.MINUTES);
        assertTrue(lock instanceof LockImpl);
    }

    @Test
    public void testGetSharedExclusiveLocks() {
        LockManager lockManager = new LockManager(connectionFactory, objectMapper, KEYSPACE, REPLICA);
        Lock lock = lockManager.getLock(LockMode.EXCLUSIVE, RESOURCE);
        assertTrue(lock instanceof ExclusiveLockImpl);
        lock = lockManager.getLock(LockMode.EXCLUSIVE, RESOURCE, OWNER);
        assertTrue(lock instanceof ExclusiveLockImpl);
        lock = lockManager.getLock(LockMode.EXCLUSIVE, RESOURCE, OWNER, 5L, TimeUnit.SECONDS, 4, TimeUnit.MINUTES);
        assertTrue(lock instanceof ExclusiveLockImpl);
         lock = lockManager.getLock(LockMode.SHARED, RESOURCE);
        assertTrue(lock instanceof SharedLockImpl);
        lock = lockManager.getLock(LockMode.SHARED, RESOURCE, OWNER);
        assertTrue(lock instanceof SharedLockImpl);
        lock = lockManager.getLock(LockMode.SHARED, RESOURCE, OWNER, 5L, TimeUnit.SECONDS, 4, TimeUnit.MINUTES);
        assertTrue(lock instanceof SharedLockImpl);
    }

    @Test
    public void testGetGroupLock() {
        LockManager lockManager = new LockManager(connectionFactory, objectMapper, KEYSPACE, REPLICA);
        Lock lock = lockManager.getLock(RESOURCE, OWNER, GROUP);
        assertTrue(lock instanceof GroupExclusiveLockImpl);
        lock = lockManager.getLock(RESOURCE, OWNER, GROUP, 5L, TimeUnit.SECONDS, 4, TimeUnit.MINUTES);
        assertTrue(lock instanceof GroupExclusiveLockImpl);
    }
}
