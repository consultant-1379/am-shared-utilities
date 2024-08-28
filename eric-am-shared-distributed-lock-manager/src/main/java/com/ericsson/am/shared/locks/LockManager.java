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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * This class is an entry point in use of distributed locks. It works as a factory for locks,
 * but also controls a thread prolonging lock's lifetime (refreshing them) up to its timeout expiration.
 * Consult {@link Lock} regarding TTL and timeout.
 *
 * <p>Instance of this class should be configured as a bean, because refreshing thread lifecycle is bound to
 * bean's lifecycle event.</p>
 * <p>Currently LockManager offers three types of locks:<ul>
 *     <li>Exclusive lock, which can be owned by at most one owner at any time</li>
 *     <li>Shared mode of shared/exclusive lock which is basically a distributed implementation of read part of
 *     {@link java.util.concurrent.locks.ReadWriteLock}</li>
 *     <li>Exclusive mode of shared/exclusive lock which is basically a distributed implementation of write part of
 *     {@link java.util.concurrent.locks.ReadWriteLock}</li>
 * </ul>
 * If You're going to use one of these locks, have a look at overloaded getLock() methods, most of them
 * </p>
 */
public final class LockManager {
    private static final String ERR_NULL_RESOURCE = "Resource to lock can not be null";
    private final String namespace;
    private final String replicaName;
    private final RedisTemplate<String, LockMetadata> metadataRedisTemplate;
    private final RedisTemplate<String, String> locksRedisTemplate;
    private final BlockingQueue<LockTtlRefreshTask> refreshTasksQueue;
    private final ConcurrentMap<String, LockMetadata> locksMetadata;
    private final LocksTtlRefresher locksTtlRefresher;
    private Thread refreshThread;

    /**
     * This is the only constructor provided, so you should be careful with it ;)
     * Seriously, along with typical dependencies like RedisConnectionFactory it should be configured
     * with two string values: {@code keyspace} and {@code replicaName}.<br/>
     * {@code keyspace} is an application specific keyspace (i.e. key prefix) in Redis server, which may
     * be guarded by ACL. All the locks on any resource uses the {@code keyspace} as a base key's prefix.<br/>
     * {@code replicaName} is just an identifier of current application instance or replica which is used
     * as one of parameters to construct the owner once it was not provided explicitly.<br/>
     *
     * @param redisConnectionFactory RedisConnectionFactory instance used to open a connections when needed
     * @param objectMapper           no surprises here as well, Jackson's ObjectMapper used by some serializers.
     * @param keyspace               a base prefix used for all Redis keys created by locks, usually application specific.
     * @param replicaName            identifier of current application instance or replica, used to construct default owner's name
     */
    public LockManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper, String keyspace, String replicaName) {
        this.namespace = keyspace + ":lock:";
        this.replicaName = replicaName;
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        metadataRedisTemplate = new RedisTemplate<>();
        metadataRedisTemplate.setConnectionFactory(redisConnectionFactory);
        metadataRedisTemplate.setKeySerializer(stringRedisSerializer);
        metadataRedisTemplate.setValueSerializer(stringRedisSerializer);
        metadataRedisTemplate.setHashKeySerializer(stringRedisSerializer);
        metadataRedisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, LockMetadata.class));
        metadataRedisTemplate.afterPropertiesSet();
        locksRedisTemplate = new RedisTemplate<>();
        locksRedisTemplate.setConnectionFactory(redisConnectionFactory);
        locksRedisTemplate.setKeySerializer(stringRedisSerializer);
        locksRedisTemplate.setValueSerializer(stringRedisSerializer);
        locksRedisTemplate.setHashKeySerializer(stringRedisSerializer);
        locksRedisTemplate.setHashValueSerializer(new LongRedisSerializer());
        locksRedisTemplate.afterPropertiesSet();
        locksMetadata = new ConcurrentHashMap<>();
        refreshTasksQueue = new ArrayBlockingQueue<>(16);
        locksTtlRefresher = new LocksTtlRefresher(refreshTasksQueue, locksRedisTemplate);
    }

    /**
     * This method should <b>not</b> be called manually but will be called by Spring framework right after
     * dependency injection succeeds. It starts
     */
    @PostConstruct
    public void start() {
        refreshThread = new Thread(locksTtlRefresher);
        refreshThread.start();
    }

    @PreDestroy
    public void stop() {
        refreshThread.interrupt();
    }

    /**
     * Returns an exclusive lock for given resource name.
     * Owner's name is built from replica name and calling thread name.
     * Lock returned by this method uses default ttl and timeout both of 3s.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param resource resource name to protect with the lock.
     * @return {@link Lock} implementation
     */
    public Lock getLock(String resource) {
        final String owner = replicaName + ":" + Thread.currentThread().getName();
        final String name = namespace + Objects.requireNonNull(resource, ERR_NULL_RESOURCE);
        return new LockImpl(locksRedisTemplate, refreshTasksQueue, name, owner);
    }

    /**
     * Returns an exclusive lock for given resource name, with customized owner's name.
     * Lock returned by this method uses default ttl and timeout both of 3s.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param resource resource name to protect with the lock.
     * @param owner    lock owner name to use for acquisition
     * @return {@link Lock} implementation
     */
    public Lock getLock(String resource, String owner) {
        final String name = namespace + Objects.requireNonNull(resource, ERR_NULL_RESOURCE);
        return new LockImpl(locksRedisTemplate, refreshTasksQueue, name, owner);
    }

    /**
     * Returns an exclusive lock for given resource name, with customized owner's name, ttl and timeout.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param resource    resource name to protect with the lock.
     * @param owner       lock owner name to use for acquisition
     * @param ttl         time-to-live for lock unless get refreshed. It is typically relatively short so lock get released
     *                    quickly in case of replica failure. Time-to-live is periodically updated by refreshing task executed
     *                    in dedicated thread, so lock will be held until released or timeout get reached if replica is alive.
     * @param ttlTimeUnit time unit in which ttl value is specified. Value of 0 means default 5s ttl will be used
     * @param timeout     timeout value for lock. Lock will be automatically released once timeout get reached.
     * @param timeUnit    time unit in which timeout value is specified
     * @return {@link Lock} implementation
     */
    public Lock getLock(String resource, String owner, long ttl, TimeUnit ttlTimeUnit, long timeout, TimeUnit timeUnit) {
        final String name = namespace + Objects.requireNonNull(resource, ERR_NULL_RESOURCE);
        final String lockOwner = Optional.ofNullable(owner).orElseGet(() -> replicaName + ":" + Thread.currentThread().getName());
        return new LockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner,
                ttlTimeUnit.toMillis(ttl), timeUnit.toMillis(timeout));
    }

    /**
     * Returns an exclusive or shared lock implementation of shared/exclusive lock depending on mode parameter
     * for given resource name. Shared/exclusive lock is basically a kind of {@link java.util.concurrent.locks.ReadWriteLock}
     * where "shared" is a read lock and exclusive is a write one.
     * Owner's name is built from replica name and calling thread name.
     * Lock returned by this method uses default ttl and timeout both of 3s.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param mode     mode of the requested lock
     * @param resource resource name to protect with the lock
     * @return {@link Lock} implementation
     */
    public Lock getLock(LockMode mode, String resource) {
        Objects.requireNonNull(mode, "Lock mode of shared/exclusive lock can't be null");
        String name = namespace + Objects.requireNonNull(resource, ERR_NULL_RESOURCE);
        String owner = replicaName + ":" + Thread.currentThread().getName();
        if (mode == LockMode.SHARED) {
            return new SharedLockImpl(locksRedisTemplate, refreshTasksQueue, name, owner);
        } else if (mode == LockMode.EXCLUSIVE) {
            return new ExclusiveLockImpl(locksRedisTemplate, refreshTasksQueue, name, owner);
        }
        throw new UnsupportedOperationException("Lock modes other than shared or exclusive are not supported for this lock type");
    }

    /**
     * Returns an exclusive or shared lock implementation of shared/exclusive lock depending on mode parameter
     * for given resource name with customized owner's name. Shared/exclusive lock is basically a kind of
     * {@link java.util.concurrent.locks.ReadWriteLock} where "shared" is a read lock and exclusive is a write one.
     * Lock returned by this method uses default ttl and timeout both of 3s.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param mode     mode of the requested lock
     * @param resource resource name to protect with the lock.
     * @param owner    lock owner name to use for acquisition
     * @return {@link Lock} implementation for exclusive or shared implementation of shared/exclusive lock
     */
    public Lock getLock(LockMode mode, String resource, String owner) {
        Objects.requireNonNull(mode, "Lock mode of shared/exclusive lock can't be null");
        final String name = namespace + Objects.requireNonNull(resource, ERR_NULL_RESOURCE);
        final String lockOwner = Optional.ofNullable(owner).orElseGet(() -> replicaName + ":" + Thread.currentThread().getName());
        if (mode == LockMode.SHARED) {
            return new SharedLockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner);
        } else if (mode == LockMode.EXCLUSIVE) {
            return new ExclusiveLockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner);
        }
        throw new UnsupportedOperationException("Lock modes other than shared or exclusive are not supported for this lock type");
    }

    /**
     * Returns an exclusive or shared lock implementation of shared/exclusive lock depending on mode parameter
     * for given resource name. Shared/exclusive lock is basically a kind of {@link java.util.concurrent.locks.ReadWriteLock}
     * where "shared" is a read lock and exclusive is a write one.<br/>
     * This method allows full customization, setting owner name, ttl and timeout.<br/>
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param mode        mode of the requested lock
     * @param resource    resource name to protect with the lock.
     * @param owner       lock owner name to use for acquisition
     * @param ttl         time-to-live for lock unless get refreshed. It is typically relatively short so lock get released
     *                    quickly in case of replica failure. Time-to-live is periodically updated by refreshing task executed
     *                    in dedicated thread, so lock will be held until released or timeout get reached if replica is alive.
     * @param ttlTimeUnit time unit in which ttl value is specified. Value of 0 means default 5s ttl will be used
     * @param timeout     timeout value for lock. Lock will be automatically released once timeout get reached.
     * @param timeUnit    time unit in which timeout value is specified
     * @return {@link Lock} implementation for exclusive or shared implementation of shared/exclusive lock
     */
    public Lock getLock(LockMode mode, String resource, String owner, long ttl, TimeUnit ttlTimeUnit,
                        long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(mode, "Lock mode of shared/exclusive lock can't be null");
        final String name = namespace + Objects.requireNonNull(resource, ERR_NULL_RESOURCE);
        final String lockOwner = Optional.ofNullable(owner).orElseGet(() -> replicaName + ":" + Thread.currentThread().getName());
        if (mode == LockMode.SHARED) {
            return new SharedLockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner, ttlTimeUnit.toMillis(ttl),
                    timeUnit.toMillis(timeout));
        } else if (mode == LockMode.EXCLUSIVE) {
            return new ExclusiveLockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner,
                    ttlTimeUnit.toMillis(ttl), timeUnit.toMillis(timeout));
        }
        throw new UnsupportedOperationException("Lock modes other than shared or exclusive are not supported for this lock type");
    }

    /**
     * Returns a group exclusive lock for given resource name. This type of lock is exclusive for group, i.e. shared
     * between owners that claims themselves to be in the same group, but doesn't allow members of other groups.
     * The simplest use case for it is allowing only one kind of operations to be run at any moment if different kinds
     * can't be executed in parallel. Well, the real world example is package onboarding and package removal: using this
     * lock type either several onboardings or several deletions may run at any moment but not both.
     * Owner's name may be null, in a such case it will be built from replica name and calling thread name.
     * Lock returned by this method uses default ttl and timeout both of 3s.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param resource resource name to protect with the lock.
     * @param owner owner's name. {@code null} value is allowed, in such case owner's name will be generated
     * @param group group name this owner claims to be in. {@code null} value is not allowed.
     * @return {@link Lock} implementation
     */
    public Lock getLock(String resource, String owner, String group) {
        final String name = namespace + resource;
        final String lockOwner = Optional.ofNullable(owner).orElseGet(() -> replicaName + ":" + Thread.currentThread().getName());
        return new GroupExclusiveLockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner, group);
    }

    /**
     * Returns a group exclusive lock for given resource name. This type of lock is exclusive for group, i.e. shared
     * between owners that claims themselves to be in the same group, but doesn't allow members of other groups.
     * The simplest use case for it is allowing only one kind of operations to be run at any moment if different kinds
     * can't be executed in parallel. Well, the real world example is package onboarding and package removal: using this
     * lock type either several onboardings or several deletions may run at any moment but not both.
     * Owner's name may be null, in a such case it will be built from replica name and calling thread name.
     * Lock returned by this method uses default ttl and timeout both of 3s.
     * <p>Note: "Protecting resource" doesn't mean that it becomes unusable without acquiring a lock.
     * It just means that several actors aware of this resource locking wouldn't access it simultaneously</p>
     * <p>Note 2: locking implementation doesn't care about resource naming,
     * although it may be a good practice to include resource type as resource name prefix.</p>
     *
     * @param resource resource name to protect with the lock.
     * @param owner owner's name. {@code null} value is allowed, in such case owner's name will be generated
     * @param group group name this owner claims to be in. {@code null} value is not allowed.
     * @param ttl         time-to-live for lock unless get refreshed. It is typically relatively short so lock get released
     *                    quickly in case of replica failure. Time-to-live is periodically updated by refreshing task executed
     *                    in dedicated thread, so lock will be held until released or timeout get reached if replica is alive.
     * @param ttlTimeUnit time unit in which ttl value is specified. Value of 0 means default 5s ttl will be used
     * @param timeout     timeout value for lock. Lock will be automatically released once timeout get reached.
     * @param timeUnit    time unit in which timeout value is specified
     * @return {@link Lock} implementation
     */
    public Lock getLock(String resource, String owner, String group, long ttl, TimeUnit ttlTimeUnit,
                        long timeout, TimeUnit timeUnit) {
        final String name = namespace + resource;
        final String lockOwner = Optional.ofNullable(owner).orElseGet(() -> replicaName + ":" + Thread.currentThread().getName());
        final long ttlMillis = ttlTimeUnit.toMillis(ttl);
        final long timeoutMillis = timeUnit.toMillis(timeout);
        return new GroupExclusiveLockImpl(locksRedisTemplate, refreshTasksQueue, name, lockOwner, group, ttlMillis, timeoutMillis);
    }
}
