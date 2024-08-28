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

import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.ericsson.am.shared.locks.AbstractLock.SHARED_HASH_SUFFIX;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

abstract class LocksTestBase {
    @Mock
    protected RedisTemplate<String, String> redisTemplate;
    @Mock
    protected ValueOperations<String, String> valueOperations;
    @Mock
    protected HashOperations<String, String, Long> hashOperations;
    protected BlockingQueue<LockTtlRefreshTask> ttlRefreshTasksQueue;

    protected Lock lock;
    protected Map<String, String> redis;
    protected Map<String, Long> ownersHash;

    protected void mockRedisTemplate(Map<String, String> redis, String redisHashKey, Map<String, Long> redisHash) {
        lenient().when(redisTemplate.hasKey(eq(redisHashKey))).thenAnswer(invocation -> !redisHash.isEmpty());
        lenient().when(redisTemplate.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            boolean deleted = redis.containsKey(key);
            if (deleted) {
                redis.remove(key);
            }
            return deleted;
        });

        // Setup operation for values
        lenient().doAnswer(invocation ->
                redis.put(invocation.getArgument(0, String.class), invocation.getArgument(1, String.class)))
                .when(valueOperations).set(anyString(), anyString());
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString())).thenAnswer(invocation ->
                redis.putIfAbsent(invocation.getArgument(0, String.class), invocation.getArgument(1, String.class)) == null);
        lenient().when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenAnswer(invocation ->
                redis.putIfAbsent(invocation.getArgument(0, String.class), invocation.getArgument(1, String.class)) == null);
        lenient().when(valueOperations.get(anyString())).thenAnswer(invocation -> redis.get(invocation.getArgument(0, String.class)));
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Setup operations for hash
        lenient().doAnswer(invocation -> {
            String key = invocation.getArgument(1, String.class);
            Long value = invocation.getArgument(2, Long.class);
            redisHash.put(key, value);
            return null;
        }).when(hashOperations).put(eq(redisHashKey), anyString(), anyLong());
        lenient().doAnswer(invocation -> {
            String key = invocation.getArgument(1, String.class);
            Long value = invocation.getArgument(2, Long.class);
            return redisHash.putIfAbsent(key, value) == null;
        }).when(hashOperations).putIfAbsent(eq(redisHashKey), anyString(), anyLong());
        lenient().when(hashOperations.get(eq(redisHashKey), anyString())).thenAnswer(
                invocation -> redisHash.get(invocation.getArgument(1, String.class)));
        lenient().when(hashOperations.entries(eq(redisHashKey))).thenAnswer(invocation -> new HashMap<>(redisHash));
        lenient().when(hashOperations.delete(eq(redisHashKey), ArgumentMatchers.any())).thenAnswer(invocation -> {
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
        lenient().doReturn(hashOperations).when(redisTemplate).opsForHash();
    }

    protected void initLockTest(String lockKey, String owner) {
        ttlRefreshTasksQueue = new LinkedBlockingQueue<>();
        redis = new HashMap<>();
        ownersHash = new HashMap<>();
        mockRedisTemplate(redis, lockKey + SHARED_HASH_SUFFIX, ownersHash);
    }

    @NotNull
    protected List<LockTtlRefreshTask> drainRefreshTasks() {
        List<LockTtlRefreshTask> submittedTasks = new ArrayList<>();
        LockTtlRefreshTask task = ttlRefreshTasksQueue.poll();
        while (task != null) {
            submittedTasks.add(task);
            task = ttlRefreshTasksQueue.poll();
        }
        return submittedTasks;
    }
}
