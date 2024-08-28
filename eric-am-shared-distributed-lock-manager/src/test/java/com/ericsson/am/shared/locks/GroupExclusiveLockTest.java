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
import static com.ericsson.am.shared.locks.AbstractLock.LONG_RUNNING_TTL;
import static com.ericsson.am.shared.locks.AbstractLock.SHARED_HASH_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class GroupExclusiveLockTest extends LocksTestBase {
    private static final String RESOURCE = "resource";
    private static final String OWNING_GROUP_KEY = RESOURCE + GroupExclusiveLockImpl.GROUP_KEY_SUFFIX;
    private static final String GROUP = "group01";
    private static final String GROUP_OWNER = "group01_Owner01";
    private static final String GROUP_OWNER2 = "group01_Owner02";
    private static final String ALT_GROUP = "group02";
    private static final String ALT_GROUP_OWNER = "group02_Owner01";
    private static final String ALT_GROUP_OWNER2 = "group02_Owner02";

    @Test
    public void testAcquireSucceedsWhenEmpty() {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        assertTrue(lock.lock());
        assertEquals(GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
    }

    @Test
    public void testAcquireSucceedsWhenOwnedByGroup() {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        redis.put(OWNING_GROUP_KEY, GROUP);
        ownersHash.put(GROUP_OWNER2, System.currentTimeMillis() + 2000L);
        assertTrue(lock.lock());
        assertEquals(GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
        assertEquals(2, ownersHash.size());
    }

    @Test
    public void testAcquireSucceedsWhenOwnedByGroupWithEviction() {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        redis.put(OWNING_GROUP_KEY, GROUP);
        ownersHash.put(GROUP_OWNER2, System.currentTimeMillis() - 200L);
        assertTrue(lock.lock());
        assertEquals(GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
        assertEquals(1, ownersHash.size());
    }

    @Test
    public void testAcquireSucceedsWhenOtherOwnershipExpired() {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        redis.put(OWNING_GROUP_KEY, ALT_GROUP);
        long now = System.currentTimeMillis();
        ownersHash.put(ALT_GROUP_OWNER, now - 300L);
        ownersHash.put(ALT_GROUP_OWNER2, now - 200L);
        assertTrue(lock.lock());
        assertEquals(GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
        assertEquals(1, ownersHash.size());
    }

    @Test
    public void testAcquireFailsWhenOwnedByOthersWithEviction() {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        redis.put(OWNING_GROUP_KEY, ALT_GROUP);
        long now = System.currentTimeMillis();
        ownersHash.put(ALT_GROUP_OWNER, now + 1000L);
        ownersHash.put(ALT_GROUP_OWNER2, now - 200L);
        assertFalse(lock.lock());
        assertEquals(ALT_GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(ALT_GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
        assertEquals(1, ownersHash.size());
    }

    @Test
    public void testAcquireSucceedsWithRefresh() {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        assertTrue(lock.lock(2, TimeUnit.MINUTES));
        assertEquals(GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
        assertEquals(1, ownersHash.size());
        List<LockTtlRefreshTask> refreshTasks = drainRefreshTasks();
        assertEquals(1, refreshTasks.size());
        LockTtlRefreshTask task = refreshTasks.get(0);
        assertEquals(GROUP_OWNER, task.getOwner());
        assertEquals(RESOURCE + SHARED_HASH_SUFFIX, task.getName());
        assertEquals(LONG_RUNNING_TTL, task.getTtl());
    }

    @Test
    public void testUnlockWithEviction() throws InterruptedException {
        initLockTest(RESOURCE, GROUP_OWNER);
        Lock lock = new GroupExclusiveLockImpl(redisTemplate, ttlRefreshTasksQueue, RESOURCE, GROUP_OWNER, GROUP);
        redis.put(OWNING_GROUP_KEY, GROUP);
        assertTrue(lock.lock());
        Thread.sleep(1000);
        assertEquals(GROUP, redis.get(OWNING_GROUP_KEY));
        assertTrue(ownersHash.containsKey(GROUP_OWNER));
        assertFalse(redis.containsKey(RESOURCE));
        assertEquals(1, ownersHash.size());
        ownersHash.put(GROUP_OWNER2, System.currentTimeMillis() - 200L);
        lock.unlock();
        assertTrue(ownersHash.isEmpty());
    }
}
