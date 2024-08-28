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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SharedLockImplTest extends LocksTestBase {
    private static final String RESOURCE = "resource";
    private static final String OWNER = "exclusiveOwner";
    private static final String ALT_OWNER = "anotherOwner";

    @Test
    public void testAcquireFailedWhenExclusivelyLocked() {
        initLockTest(RESOURCE, OWNER);
        lock = new SharedLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        redis.put(RESOURCE, ALT_OWNER);
        assertFalse(lock.lock());
        assertTrue(ownersHash.isEmpty());
        assertEquals(ALT_OWNER, redis.get(RESOURCE));
    }

    @Test
    public void testAcquireSucceedsWhenEmpty() {
        initLockTest(RESOURCE, OWNER);
        lock = new SharedLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        assertTrue(lock.lock());
        assertTrue(redis.isEmpty());
        assertEquals(1, ownersHash.size());
        assertTrue(ownersHash.containsKey(OWNER));
    }

    @Test
    public void testAcquireSucceedsWhenSharedLocked() {
        initLockTest(RESOURCE, OWNER);
        lock = new SharedLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        ownersHash.put(ALT_OWNER, System.currentTimeMillis() + 2000L);
        assertTrue(lock.lock());
        assertEquals(2, ownersHash.size());
        assertTrue(redis.isEmpty());
        assertTrue(ownersHash.containsKey(OWNER));
        assertTrue(ownersHash.containsKey(ALT_OWNER));
    }

    @Test
    public void testAcquireSucceededAndExpiredSharedEvicted() {
        initLockTest(RESOURCE, OWNER);
        lock = new SharedLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        ownersHash.put(ALT_OWNER, System.currentTimeMillis() - 200L);
        assertTrue(lock.lock());
        assertEquals(1, ownersHash.size());
        assertTrue(redis.isEmpty());
        assertTrue(ownersHash.containsKey(OWNER));
    }

    @Test
    public void testAcquireSucceedsWhenEmptyWithRefreshInstalled() {
        initLockTest(RESOURCE, OWNER);
        lock = new SharedLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER);
        long now = System.currentTimeMillis();
        assertTrue(lock.lock(20, TimeUnit.SECONDS));
        List<LockTtlRefreshTask> tasks = drainRefreshTasks();
        assertEquals(1, tasks.size());
        LockTtlRefreshTask task = tasks.get(0);
        long skew = Math.abs(task.getExpiration() - now - TimeUnit.SECONDS.toMillis(20));
        assertTrue(skew < 200L);
        assertEquals(AbstractLock.DEFAULT_TTL, task.getTtl());
        assertEquals(RESOURCE + AbstractLock.SHARED_HASH_SUFFIX, task.getName());
        assertEquals(OWNER, task.getOwner());
        assertTrue(redis.isEmpty());
        assertEquals(1, ownersHash.size());
        assertTrue(ownersHash.containsKey(OWNER));
    }

    @Test
    public void testUnlock() {
        initLockTest(RESOURCE, OWNER);
        lock = new SharedLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, OWNER, AbstractLock.DEFAULT_TTL, AbstractLock.DEFAULT_TTL);
        lock.lock();
        assertFalse(ownersHash.isEmpty());
        ownersHash.put(OWNER + "2", System.currentTimeMillis() - 200L);
        lock.unlock();
        assertTrue(ownersHash.isEmpty());
    }
}
