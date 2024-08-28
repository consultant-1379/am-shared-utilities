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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class LockImplTest extends LocksTestBase {
    private static final String RESOURCE = "resource";
    private static final String OWNER = "exclusiveOwner";
    private static final String ALT_OWNER = "anotherOwner";

    @Test
    public void testAcquireFailedWhenOwnedExclusively() {
        initLockTest(RESOURCE, OWNER);
        lock = new LockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        redis.put(RESOURCE, ALT_OWNER);
        assertFalse(lock.lock());
        assertNull(ttlRefreshTasksQueue.poll());
        assertTrue(ownersHash.isEmpty());
        assertEquals(ALT_OWNER, redis.get(RESOURCE));
    }

    @Test
    public void testAcquireSucceedsWhenEmpty() {
        initLockTest(RESOURCE, OWNER);
        lock = new LockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        assertTrue(lock.lock());
        List<LockTtlRefreshTask> submittedTasks = drainRefreshTasks();
        assertEquals(0, submittedTasks.size()); // by default timeout == ttl, so no refresh installed
        assertEquals(1, redis.size());
        assertTrue(redis.containsKey(RESOURCE));
        assertEquals(OWNER, redis.get(RESOURCE));
    }

    @Test
    public void testAcquireSucceedsWithRefreshWhenEmpty() {
        initLockTest(RESOURCE, OWNER);
        lock = new LockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER).withAcquireRetries(3, 2000L);
        long now = System.currentTimeMillis();
        assertTrue(lock.lock(20, TimeUnit.SECONDS));
        List<LockTtlRefreshTask> submittedTasks = drainRefreshTasks();
        assertEquals(1, submittedTasks.size());
        LockTtlRefreshTask task = submittedTasks.get(0);
        long skew = Math.abs(task.getExpiration() - now - TimeUnit.SECONDS.toMillis(20));
        assertTrue(skew < 200L);
        assertEquals(AbstractLock.DEFAULT_TTL, task.getTtl());
        assertEquals(RESOURCE, task.getName());
        assertEquals(OWNER, task.getOwner());
        assertTrue(ownersHash.isEmpty());
        assertEquals(1, redis.size());
        assertTrue(redis.containsKey(RESOURCE));
        assertEquals(OWNER, redis.get(RESOURCE));
    }

    @Test
    void testUnlockSucceedsWhenOwns() {
        initLockTest(RESOURCE, OWNER);
        lock = new LockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        lock.lock();
        assertFalse(redis.isEmpty());
        lock.unlock();
        assertTrue(redis.isEmpty());
    }

    @Test
    void testUnlockSkipsWhenNotOwner() {
        initLockTest(RESOURCE, OWNER);
        lock = new LockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER, AbstractLock.DEFAULT_TTL, AbstractLock.DEFAULT_TTL);
        redis.put(RESOURCE, OWNER + "2");
        lock.unlock();
        assertFalse(redis.isEmpty());
    }
}
