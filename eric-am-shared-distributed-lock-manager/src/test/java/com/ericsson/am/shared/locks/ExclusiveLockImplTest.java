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

import static com.ericsson.am.shared.locks.AbstractLock.DEFAULT_TTL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ExclusiveLockImplTest extends LocksTestBase {
    private static final String RESOURCE = "resource";
    private static final String OWNER = "exclusiveOwner";
    private static final String ALT_OWNER1 = "sharedOwner1";
    private static final String ALT_OWNER2 = "sharedOwner2";

    @Test
    public void testAcquireFailedWhenOwnedExclusively() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER, DEFAULT_TTL, DEFAULT_TTL);
        redis.put(RESOURCE, ALT_OWNER1);
        assertFalse(lock.lock());
        assertNull(ttlRefreshTasksQueue.poll());
        assertTrue(ownersHash.isEmpty());
        assertEquals(ALT_OWNER1, redis.get(RESOURCE));
    }

    @Test
    public void testAcquireFailedWhenOwnedShared() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        ownersHash.put(ALT_OWNER1, now + 2000L);
        ownersHash.put(ALT_OWNER2, now + 1000L);
        assertFalse(lock.lock());
        assertNull(ttlRefreshTasksQueue.poll());
        assertEquals(2, ownersHash.size());
        assertEquals(0, redis.size());
    }

    @Test
    public void testAcquireFailedWhenOwnedSharedButExpiredEvicted() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        ownersHash.put(ALT_OWNER1, now - 200L);
        ownersHash.put(ALT_OWNER2, now + 2000L);
        assertFalse(lock.lock());
        assertNull(ttlRefreshTasksQueue.poll());
        assertEquals(1, ownersHash.size());
        assertTrue(ownersHash.containsKey(ALT_OWNER2));
        assertEquals(0, redis.size());
    }

    @Test
    public void testAcquireSucceedsWhenEmpty() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        assertTrue(lock.lock());
        List<LockTtlRefreshTask> submittedTasks = drainRefreshTasks();
        assertEquals(0, submittedTasks.size()); // by default timeout == ttl, so no refresh installed
        assertTrue(ownersHash.isEmpty());
        assertEquals(1, redis.size());
        assertTrue(redis.containsKey(RESOURCE));
        assertEquals(OWNER, redis.get(RESOURCE));
    }

    @Test
    public void testAcquireSucceedsWithRefreshWhenEmpty() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        assertTrue(lock.lock(20, TimeUnit.SECONDS));
        List<LockTtlRefreshTask> submittedTasks = drainRefreshTasks();
        assertEquals(1, submittedTasks.size());
        LockTtlRefreshTask task = submittedTasks.get(0);
        long skew = Math.abs(task.getExpiration() - now - TimeUnit.SECONDS.toMillis(20));
        assertTrue(skew < 200L);
        assertEquals(5000L, task.getTtl());
        assertEquals(RESOURCE, task.getName());
        assertEquals(OWNER, task.getOwner());
        assertTrue(ownersHash.isEmpty());
        assertEquals(1, redis.size());
        assertTrue(redis.containsKey(RESOURCE));
        assertEquals(OWNER, redis.get(RESOURCE));
    }

    @Test
    public void testAcquireSucceedsWhenSharedExpired() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        ownersHash.put(ALT_OWNER1, now - 100L);
        ownersHash.put(ALT_OWNER2, now - 200L);
        assertTrue(lock.lock());
        List<LockTtlRefreshTask> submittedTasks = drainRefreshTasks();
        assertEquals(0, submittedTasks.size()); // by default timeout == ttl, so no refresh installed
        assertTrue(ownersHash.isEmpty());
        assertEquals(1, redis.size());
        assertTrue(redis.containsKey(RESOURCE));
        assertEquals(OWNER, redis.get(RESOURCE));
        assertTrue(ownersHash.isEmpty());
    }

    @Test
    void testUnlockSucceedsWhenOwns() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        lock.lock();
        assertFalse(redis.isEmpty());
        lock.unlock();
        assertTrue(redis.isEmpty());
    }

    @Test
    void testUnlockSkipsWhenNotOwner() {
        initLockTest(RESOURCE, OWNER);
        lock = new ExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        redis.put(RESOURCE, OWNER + "2");
        lock.unlock();
        assertFalse(redis.isEmpty());
    }
}
