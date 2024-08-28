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

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class LockTtlRefresherTest {
    private static final String RESOURCE_EXCLUSIVE01 = "RE01";
    private static final String RESOURCE_SHARED01 = "RS01" + AbstractLock.SHARED_HASH_SUFFIX;
    private static final String FORMAT_RC_SHARED = "RS%02d" + AbstractLock.SHARED_HASH_SUFFIX;
    private static final String FORMAT_OW_SHARED = "OS%02d";
    private static final String OWNER_EXCLUSIVE01 = "OE01";
    private static final String OWNER_SHARED01 = "OS01";

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private HashOperations<String, String, Long> hashOperations;
    private BlockingQueue<LockTtlRefreshTask> lockTtlRefreshTasks;
    private Map<String, String> redis;
    private Map<String, Long> redisTimeouts;
    private Map<String, Map<String, Long>> ownersHashes;
    private List<TtlUpdate> updates;


    @Test
    void testSingleExclusiveUpdates() throws InterruptedException {
        Thread refreshThread = initTest();
        LockTtlRefreshTask task = new LockTtlRefreshTask(RESOURCE_EXCLUSIVE01, OWNER_EXCLUSIVE01, LockType.EXCLUSIVE);
        task.setTtl(2000L);
        refreshThread.start();
        redis.put(RESOURCE_EXCLUSIVE01, OWNER_EXCLUSIVE01);
        task.setExpiration(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(9));
        lockTtlRefreshTasks.put(task);
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        refreshThread.interrupt();
        updates.forEach(tu -> {
            assertEquals(RESOURCE_EXCLUSIVE01, tu.name);
            assertEquals(OWNER_EXCLUSIVE01, tu.owner);
            assertTrue(tu.expBefore == 0L || tu.updatedAt < tu.expBefore);
        });
        assertTrue(Math.abs(task.getExpiration() - updates.get(updates.size() - 1).expAfter) < 100L);
    }

    @Test
    void testSingleExclusiveUpdatesUntilReleased() throws InterruptedException {
        Thread refreshThread = initTest();
        redis.put(RESOURCE_EXCLUSIVE01, OWNER_EXCLUSIVE01);
        LockTtlRefreshTask task = new LockTtlRefreshTask(RESOURCE_EXCLUSIVE01, OWNER_EXCLUSIVE01, LockType.EXCLUSIVE);
        task.setTtl(2000L);
        refreshThread.start();
        task.setExpiration(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(9));
        lockTtlRefreshTasks.put(task);
        Thread.sleep(TimeUnit.SECONDS.toMillis(6));
        redis.remove(RESOURCE_EXCLUSIVE01);
        long removed = System.currentTimeMillis();
        Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        updates.forEach(tu -> {
            assertEquals(RESOURCE_EXCLUSIVE01, tu.name);
            assertEquals(OWNER_EXCLUSIVE01, tu.owner);
            assertTrue(tu.expBefore == 0L || tu.updatedAt < tu.expBefore);
        });
        assertTrue(updates.get(updates.size() - 1).updatedAt < removed);
    }

    @Test
    void testSingleSharedUpdated() throws InterruptedException {
        Thread refreshThread = initTest();
        LockTtlRefreshTask task = new LockTtlRefreshTask(RESOURCE_SHARED01, OWNER_SHARED01, LockType.SHARED);
        task.setTtl(2000L);
        refreshThread.start();
        ownersHashes.computeIfAbsent(RESOURCE_SHARED01, k -> new ConcurrentHashMap<>())
                .put(OWNER_SHARED01, System.currentTimeMillis() + 2000L);
        task.setExpiration(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(9));
        lockTtlRefreshTasks.put(task);
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        refreshThread.interrupt();
        assertFalse(updates.isEmpty());
        updates.forEach(tu -> {
            assertEquals(RESOURCE_SHARED01, tu.name);
            assertEquals(OWNER_SHARED01, tu.owner);
            assertTrue(tu.expBefore == 0L || tu.updatedAt < tu.expBefore);
        });
        assertTrue(Math.abs(task.getExpiration() - updates.get(updates.size() - 1).expAfter) < 100L);
    }

    @Test
    void testSingleSharedUpdatedUntilReleased() throws InterruptedException {
        Thread refreshThread = initTest();
        LockTtlRefreshTask task = new LockTtlRefreshTask(RESOURCE_SHARED01, OWNER_SHARED01, LockType.SHARED);
        task.setTtl(2000L);
        refreshThread.start();
        ownersHashes.computeIfAbsent(RESOURCE_SHARED01, k -> new ConcurrentHashMap<>())
                .put(OWNER_SHARED01, System.currentTimeMillis() + 2000L);
        task.setExpiration(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(9));
        lockTtlRefreshTasks.put(task);
        Thread.sleep(TimeUnit.SECONDS.toMillis(6));
        ownersHashes.get(RESOURCE_SHARED01).remove(OWNER_SHARED01);
        long removed = System.currentTimeMillis();
        Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        refreshThread.interrupt();
        assertFalse(updates.isEmpty());
        updates.forEach(tu -> {
            assertEquals(RESOURCE_SHARED01, tu.name);
            assertEquals(OWNER_SHARED01, tu.owner);
            assertTrue(tu.expBefore == 0L || tu.updatedAt < tu.expBefore);
        });
        assertTrue(updates.get(updates.size() - 1).updatedAt < removed);
    }

    @Test
    void testMultipleSharedUpdated() throws InterruptedException {
        Thread refreshThread = initTest();
        List<String> resources = new ArrayList<>();
        List<LockTtlRefreshTask> tasks = new ArrayList<>(20);
        for (int i = 0; i < 4; i++) {
            resources.add(String.format(FORMAT_RC_SHARED, i));
        }
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 20; i++) {
            final String owner = String.format(FORMAT_OW_SHARED, i);
            final LockTtlRefreshTask task = new LockTtlRefreshTask(resources.get(random.nextInt(4)), owner, LockType.SHARED);
            task.setTtl(random.nextInt(2) == 0 ? 7000L : 5000L);
            tasks.add(task);
        }
        refreshThread.start();
        for (LockTtlRefreshTask task : tasks) {
            ownersHashes.computeIfAbsent(task.getName(), k -> new ConcurrentHashMap<>())
                    .put(task.getOwner(), System.currentTimeMillis() + task.getTtl());
            task.setExpiration(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(17));
            lockTtlRefreshTasks.put(task);
            Thread.sleep(random.nextInt(10) * 20L);
        }
        Thread.sleep(TimeUnit.SECONDS.toMillis(20));
        refreshThread.interrupt();
        assertFalse(updates.isEmpty());
        updates.forEach(tu -> assertTrue(tu.updatedAt < tu.expBefore));
    }

    private Thread initTest() {
        redis = new HashMap<>();
        redisTimeouts = new HashMap<>();
        ownersHashes = new HashMap<>();
        updates = new ArrayList<>();
        mockRedisTemplate();
        lockTtlRefreshTasks = new LinkedBlockingQueue<>();
        return new Thread(new LocksTtlRefresher(lockTtlRefreshTasks, redisTemplate));
    }

    private void mockRedisTemplate() {
        lenient().when(redisTemplate.hasKey(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            return redis.containsKey(key)
                    || !Optional.ofNullable(ownersHashes.get(key)).map(Map::isEmpty).orElse(Boolean.TRUE);
        });
        lenient().when(redisTemplate.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            boolean deleted = redis.containsKey(key);
            if (deleted) {
                redis.remove(key);
                redisTimeouts.remove(key);
            }
            return deleted;
        });
        lenient().when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            if (!redis.containsKey(key)) {
                return Boolean.FALSE;
            }
            long oldTimeout = Optional.ofNullable(redisTimeouts.get(key)).orElse(0L);
            long newTimeout = System.currentTimeMillis() + invocation.getArgument(1, Long.class);
            updates.add(new TtlUpdate(key, redis.get(key), System.currentTimeMillis(), oldTimeout, newTimeout));
            redisTimeouts.put(key, newTimeout);
            return Boolean.TRUE;
        });

        // Setup operation for values
        mockValueOperations();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Setup operations for hash
        mockHashOperations();
        lenient().doReturn(hashOperations).when(redisTemplate).opsForHash();
    }

    private void mockValueOperations() {
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString())).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0, String.class);
            return redis.putIfAbsent(key, invocation.getArgument(1, String.class)) == null;
        });
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> {
            final String key = invocation.getArgument(0, String.class);
            long expiring = invocation.getArgument(3, TimeUnit.class)
                    .toMillis(invocation.getArgument(2, Long.class)) + System.currentTimeMillis();
            if (redis.putIfAbsent(key, invocation.getArgument(1, String.class)) == null) {
                redisTimeouts.put(key, expiring);
            }
            return Boolean.FALSE;
        });
        lenient().when(valueOperations.get(anyString()))
                .thenAnswer(invocation -> redis.get(invocation.getArgument(0, String.class)));
    }

    private void mockHashOperations() {
        //        hashOperations = mock(HashOperations.class);
        lenient().doAnswer(invocation -> {
            String ownersKey = invocation.getArgument(0, String.class);
            String owner = invocation.getArgument(1, String.class);
            Long value = invocation.getArgument(2, Long.class);
            Map<String, Long> ownersHash = ownersHashes.computeIfAbsent(ownersKey, key -> new ConcurrentHashMap<>());
            long oldTimeout = Optional.ofNullable(ownersHash.get(owner)).orElse(0L);
            updates.add(new TtlUpdate(ownersKey, owner, System.currentTimeMillis(), oldTimeout, value));
            ownersHash.put(owner, value);
            return null;
        }).when(hashOperations).put(anyString(), anyString(), anyLong());
        lenient().doAnswer(invocation -> {
            String ownersKey = invocation.getArgument(0, String.class);
            String owner = invocation.getArgument(1, String.class);
            Long value = invocation.getArgument(2, Long.class);
            return ownersHashes.computeIfAbsent(ownersKey, key -> new ConcurrentHashMap<>())
                    .putIfAbsent(owner, value) == null;
        }).when(hashOperations).putIfAbsent(anyString(), anyString(), anyLong());
        lenient().when(hashOperations.get(anyString(), anyString())).thenAnswer(
                invocation -> Optional.ofNullable(ownersHashes.get(invocation.getArgument(0, String.class)))
                        .map(owners -> owners.get(invocation.getArgument(1, String.class))).orElse(null));
        lenient().when(hashOperations.entries(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(ownersHashes.get(invocation.getArgument(0, String.class))).map(HashMap::new)
                        .orElseGet(HashMap::new));
        lenient().when(hashOperations.delete(anyString(), any())).thenAnswer(invocation -> {
            Map<String, Long> redisHash = ownersHashes.get(invocation.getArgument(0, String.class));
            if (Optional.ofNullable(redisHash).map(Map::isEmpty).orElse(Boolean.TRUE)) {
                return 0L;
            }
            Object[] args = invocation.getArguments();
            long removed = 0;
            for (int i = 1; i < args.length; i++) {
                if (redisHash.containsKey(args[i])) {
                    redisHash.remove(args[i]);
                    removed++;
                }
            }
            return removed;
        });
        lenient().when(hashOperations.hasKey(anyString(), anyString())).thenAnswer(invocation ->
                Optional.ofNullable(ownersHashes.get(invocation.getArgument(0, String.class)))
                        .map(oh -> oh.containsKey(invocation.getArgument(1, String.class)))
                        .orElse(Boolean.FALSE));
    }

    @AllArgsConstructor
    private static class TtlUpdate {
        public final String name;
        public final String owner;
        public final long updatedAt;
        public final long expBefore;
        public final long expAfter;
    }
}
