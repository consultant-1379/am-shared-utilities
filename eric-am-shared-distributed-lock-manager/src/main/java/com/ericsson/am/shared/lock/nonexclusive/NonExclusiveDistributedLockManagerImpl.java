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
package com.ericsson.am.shared.lock.nonexclusive;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.am.shared.lock.exceptions.LockAcquisitionWaitingException;
import com.ericsson.am.shared.lock.exceptions.LockDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.LockHasNoTtlException;
import com.ericsson.am.shared.lock.exceptions.LockInternalException;
import com.ericsson.am.shared.lock.exceptions.LockTtlWithinRacingProtectionIntervalException;
import com.ericsson.am.shared.lock.exceptions.ProcessingTooLongException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyHasNoTtlException;
import com.ericsson.am.shared.lock.models.LockParameters;
import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveLockRepository;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveReplicaLockRepository;
import com.ericsson.am.shared.lock.repository.LockPriorityRepository;

@Service
public class NonExclusiveDistributedLockManagerImpl implements NonExclusiveDistributedLockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonExclusiveDistributedLockManagerImpl.class);

    // When ttl is within LOCK_RACING_PROTECTION_INTERVAL_MS, lock is considered as expired, but redis key exists
    // No modifications are allowed for such lock (modify, delete, change ttl), including refresh()
    // The only exception to the rule above: method acquire() when the lock has been just created
    // This is needed to prevent modifying redis key when the redis is deleting it due to ttl expiration at the same moment
    private static final long LOCK_RACING_PROTECTION_INTERVAL_MS = 2000;
    private static final long HEARTBEAT_INTERVAL_MS = 3000;
    private static final long LOCK_RELEASE_ON_REPLICAS_FAILURE_INTERVAL_MS = 3 * HEARTBEAT_INTERVAL_MS;

    @Autowired
    private NonExclusiveLockRepository lockRepository;
    @Autowired
    private NonExclusiveReplicaLockRepository replicaLockRepository;
    @Autowired
    private LockPriorityRepository lockPriorityRepository;

    @Override
    public boolean acquire(final String resourceId,
                           final String holder,
                           final int duration,
                           final String transferringHolder,
                           final int priority,
                           final Set<String> sharingGroups,
                           final LockParameters lockParameters) {

        long lockAcquisitionRetryStartRedisTimeMs = replicaLockRepository.getTimeMs();
        long lockAcquisitionRetryEndRedisTimeMs = lockAcquisitionRetryStartRedisTimeMs + lockParameters.getLockAcquisitionRetryTimeoutMs();

        LOGGER.debug(
                "[ACQUIRE] Start acquire(resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                        + "sharingGroups: {})",
                resourceId, holder, duration, transferringHolder, priority, sharingGroups);
        while (true) {
            if (transferringHolder == null) {
                Optional<Map.Entry<Integer, Long>> nextHolderPriority = requestNextHolderPriority(priority, lockParameters);
                if (nextHolderPriority.isPresent()) {
                    String failedAttemptReason = "blocked by priority; expected next priority: %s, requested priority: %s".formatted(
                            nextHolderPriority.get(),
                            priority);
                    if (needRetryForLockAcquisition(lockParameters, lockAcquisitionRetryStartRedisTimeMs, lockAcquisitionRetryEndRedisTimeMs,
                                                    failedAttemptReason)) {
                        sleepForTimeMs(lockParameters.getLockAcquisitionRetryIntervalMs());
                        continue;
                    }
                    LOGGER.debug(
                            "[ACQUIRE] Not acquired for (resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                                    + "sharingGroups: {}). Processing time: {}ms.",
                            resourceId, holder, duration, transferringHolder, priority, sharingGroups,
                            replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs);
                    return false;
                }
            } else {
                // scenario when (transferringHolder!=null and priority!=0) is not considered in the current implementation
                // priority will be ignored
                if (priority != 0) {
                    LOGGER.warn(
                            "[ACQUIRE] Transferring lock from another holder, but non-default priority is specified. Priority is ignored. "
                                    + "resourceId: {}, holder: {}", resourceId, holder);
                }
            }

            final NonExclusiveLock lock = buildLock(resourceId, holder, lockParameters.getReplicaName(), sharingGroups,
                                                    duration, priority);
            LOGGER.debug("[ACQUIRE] Built Lock: {}", lock);

            String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);

            // Note: purpose for refresh() in this case is to:
            // 1. Remove existing holders whose lock is expired
            refresh(resourceId, true, false, lockParameters);
            LOGGER.debug("[ACQUIRE] Refreshed resource {} locks before acquiring by holder {}", resourceId, holder);

            try {
                this.getResidualLockTtlMs(resourceId, false, lockParameters);
            } catch (LockTtlWithinRacingProtectionIntervalException | LockHasNoTtlException e) {
                if (needRetryForLockAcquisition(lockParameters, lockAcquisitionRetryStartRedisTimeMs, lockAcquisitionRetryEndRedisTimeMs,
                                                e.getMessage())) {
                    sleepForTimeMs(lockParameters.getLockAcquisitionRetryIntervalMs());
                    continue;
                }
                LOGGER.debug(
                        "[ACQUIRE] Not acquired for (resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                                + "sharingGroups: {}). Processing time: {}ms. Reason: {}",
                        resourceId, holder, duration, transferringHolder, priority, sharingGroups,
                        replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs, e.getMessage());
                return false;
            } catch (LockDoesNotExistException e) {
                LOGGER.debug("[ACQUIRE] New lock will be created for resource {}. Reason: {}", resourceId, e.getMessage());
            }

            if (transferringHolder != null) {
                acquireFromTransferringHolder(resourceId, holder, transferringHolder, lock, lockParameters);
                LOGGER.debug(
                        "[ACQUIRE] Acquired from transferring holder (resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                                + "sharingGroups: {}). Processing time: {}ms.",
                        resourceId, holder, duration, transferringHolder, priority, sharingGroups,
                        replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs);
                return true;
            }

            LOGGER.debug("[ACQUIRE] Creating holder (candidate) {} for resource {}", holder, resourceId);
            boolean isSet = lockRepository.putIfAbsent(globalLocksRedisKey, holder, lock);
            // if globalLocksRedisKey did not exist, at this point ttl may be -1
            if (!isSet) {
                String failedAttemptReason =
                        "same holder %s already locks the resource %s.".formatted(holder, resourceId);
                if (needRetryForLockAcquisition(lockParameters, lockAcquisitionRetryStartRedisTimeMs, lockAcquisitionRetryEndRedisTimeMs,
                                                failedAttemptReason)) {
                    sleepForTimeMs(lockParameters.getLockAcquisitionRetryIntervalMs());
                    continue;
                }
                LOGGER.debug(
                        "[ACQUIRE] Not acquired for (resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                                + "sharingGroups: {}). Processing time: {}ms.",
                        resourceId, holder, duration, transferringHolder, priority, sharingGroups,
                        replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs);
                return false;
            }

            final Map<String, NonExclusiveLock> allLocks = lockRepository.getAll(globalLocksRedisKey);
            LOGGER.debug("[ACQUIRE] All current locks for {}: {}", globalLocksRedisKey, allLocks);

            final List<Set<String>> allSharingGroups = allLocks.values().stream()
                    .map(NonExclusiveLock::getSharingGroups)
                    .collect(Collectors.toList());
            allSharingGroups.add(sharingGroups);
            final Set<String> commonSharingGroups = getCommonSharingGroups(allSharingGroups);
            LOGGER.debug("[ACQUIRE] Common sharing groups for {}: {}", globalLocksRedisKey, commonSharingGroups);
            if (allLocks.size() > 1 && commonSharingGroups.isEmpty()) {
                lockRepository.delete(globalLocksRedisKey, holder);
                LOGGER.debug("[ACQUIRE] Removed holder {} for resource {}", holder, resourceId);

                String failedAttemptReason = "lock is taken by other holders: %s".formatted(allLocks.keySet());
                if (needRetryForLockAcquisition(lockParameters, lockAcquisitionRetryStartRedisTimeMs, lockAcquisitionRetryEndRedisTimeMs,
                                                failedAttemptReason)) {
                    sleepForTimeMs(lockParameters.getLockAcquisitionRetryIntervalMs());
                    continue;
                }
                LOGGER.debug(
                        "[ACQUIRE] Not acquired for (resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                                + "sharingGroups: {}). Processing time: {}ms.",
                        resourceId, holder, duration, transferringHolder, priority, sharingGroups,
                        replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs);
                return false;
            }
            LOGGER.debug("[ACQUIRE] Start refreshing. Lock: {}", lock);
            // Note: purpose for refresh() is to:
            // 1. Add resourceId to REPLICA_LOCK_LIST_REDIS_KEY
            // 2. Update ttl of GLOBAL_LOCKS_REDIS_KEY based on expiration time of several holders
            refresh(resourceId, false, true, lockParameters);
            LOGGER.debug("[ACQUIRE] Refreshed resource {} locks after acquiring by holder {}", resourceId, holder);
            LOGGER.debug("[ACQUIRE] Lock is acquired. Lock: {}", lock);
            break;
        }
        LOGGER.debug(
                "[ACQUIRE] Acquired for (resourceId: {}, holder: {}, duration: {}, transferringHolder: {}, priority: {},"
                        + "sharingGroups: {}). Processing time: {}ms.",
                resourceId, holder, duration, transferringHolder, priority, sharingGroups,
                replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs);
        return true;
    }

    @Override
    public void updateDuration(final String resourceId, final String holder, final int duration, final LockParameters lockParameters) {
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        boolean isLockExpired = isLockExpired(resourceId, holder, lockParameters);
        if (isLockExpired) {
            throw new ProcessingTooLongException("Long running processing has been rolled back", resourceId, holder);
        }
        final Optional<NonExclusiveLock> lockOptional = lockRepository.get(globalLocksRedisKey, holder);
        if (lockOptional.isPresent()) {
            NonExclusiveLock lock = lockOptional.get();
            lock.setExpirationTime(replicaLockRepository.getTimeMs() + duration * 1000L);
            lockRepository.put(globalLocksRedisKey, holder, lock);
            refresh(resourceId, false, false, lockParameters);
        } else {
            throw new LockInternalException("Lock for resource %s is not expired, but holder %s is not present. This should never happen."
                                                    .formatted(resourceId, holder));
        }
    }

    @Override
    public void release(final String resourceId, final String holder, final LockParameters lockParameters) {
        LOGGER.debug("[RELEASE] Start releasing lock for resourceId: {}, holder: {}", resourceId, holder);
        boolean isLockExpired = isLockExpired(resourceId, holder, lockParameters);
        if (isLockExpired) {
            LOGGER.warn("[RELEASE] Lock was expired during releasing. resourceId: {}, holder: {}", resourceId, holder);
            LOGGER.debug("[RELEASE] End releasing lock. resourceId: {}, holder: {}", resourceId, holder);
            return;
        }
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        // if it was the last holder, redis will remove globalLocksRedisKey
        lockRepository.delete(globalLocksRedisKey, holder);
        refresh(resourceId, true, false, lockParameters);
        LOGGER.debug("[RELEASE] End releasing lock. resourceId: {}, holder: {}", resourceId, holder);
    }

    @Override
    public void refreshAll(final LockParameters lockParameters) {
        LOGGER.debug("[REFRESH_ALL] Lock parameters: {}", lockParameters);
        String replicaLockListRedisKey = lockParameters.getReplicaLockListRedisKey();
        Set<String> resourceIds = replicaLockRepository.findAll(replicaLockListRedisKey);
        // holder may not exist if lock is being released in parallel with refresh job
        resourceIds.forEach(resourceId -> refresh(resourceId, true, false, lockParameters));
    }

    /**
     * Remove all replicas expired holders, refresh ttl only for current replica holders
     */
    @Override
    public void refresh(final String resourceId, final boolean resourceMayNotExist, final boolean ttlMayNotBeSet,
                        final LockParameters lockParameters) {
        LOGGER.debug("[REFRESH] Start refresh({}, {}, {}, {})", resourceId, resourceMayNotExist, ttlMayNotBeSet,
                     lockParameters.getReplicaName());
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        String replicaLockListRedisKey = lockParameters.getReplicaLockListRedisKey();

        boolean isRemovedByTtl = refreshIfRemovedByTtl(resourceId, resourceMayNotExist, ttlMayNotBeSet, lockParameters,
                                                       globalLocksRedisKey, replicaLockListRedisKey);
        if (isRemovedByTtl) {
            LOGGER.debug("[REFRESH] End refresh({}, {}, {}, {})", resourceId, resourceMayNotExist, ttlMayNotBeSet,
                         lockParameters.getReplicaName());
            return;
        }

        final Set<String> holders = lockRepository.getHolders(globalLocksRedisKey);
        LOGGER.debug("[REFRESH] Lock for resource {} is not removed by ttl, processing holders: {}", resourceId, holders);

        long currentReplicaLatestExpirationTime = 0L;
        for (String holder : holders) {
            final Optional<NonExclusiveLock> lockOptional = lockRepository.get(globalLocksRedisKey, holder);
            if (lockOptional.isEmpty()) {
                continue;
            }
            final NonExclusiveLock lock = lockOptional.get();
            long currentTimeMs = replicaLockRepository.getTimeMs();
            if (lock.getExpirationTime() < currentTimeMs) {
                lockRepository.delete(globalLocksRedisKey, holder);
                LOGGER.debug("[REFRESH] Removed holder {}, key: {}, resourceId: {}, replica: {}", holder, globalLocksRedisKey, resourceId,
                             lockParameters.getReplicaName());
                // if it was the last holder in the lock
                if (lockRepository.getAll(globalLocksRedisKey).isEmpty()) {
                    removeFromReplicaLockList(resourceId, replicaLockListRedisKey, globalLocksRedisKey);
                }
                LOGGER.warn("[REFRESH] Lock is released. Reason: expiration is {}ms behind( {}ms < {}ms ). Lock: {}",
                            currentTimeMs - lock.getExpirationTime(), lock.getExpirationTime(), currentTimeMs, lock);
                continue;
            }
            // update redis key ttl only for current replica locks, but remove other replica locks if expired
            // Example:
            // Given
            // replica-1:holder-1 expirationTime: 12:05
            // replica-1:holder-2 expirationTime: 12:10
            // replica-2:holder-3 expirationTime: 12:15
            // replica-3:holder-4 expirationTime: 11:55
            // current redis time 12:00
            // When the method refresh() is running on replica-1:
            // 1. refresh ttl(based on latest expiration time) for its own holders: holder-1, holder-2
            // 2. remove expired locks for all replicas: holder-4
            if (lock.getReplicaId().equals(lockParameters.getReplicaName())) {
                currentReplicaLatestExpirationTime = Math.max(currentReplicaLatestExpirationTime, lock.getExpirationTime());
            }
        }
        LOGGER.debug("[REFRESH] Lock for resource {}, replica {} has latest expiration time {}ms", resourceId, lockParameters.getReplicaName(),
                     currentReplicaLatestExpirationTime);

        if (currentReplicaLatestExpirationTime > 0) {
            long newTtlMs = calculateNewTtlMs(currentReplicaLatestExpirationTime);
            Optional<Long> existingTtlOptional = getExistingTtl(resourceId, resourceMayNotExist, ttlMayNotBeSet, lockParameters);
            if (existingTtlOptional.isEmpty()) {
                return;
            }
            long existingTtlMs = existingTtlOptional.get();
            if (newTtlMs > existingTtlMs) {
                try {
                    this.setResidualLockTtlMs(resourceId, newTtlMs, lockParameters);
                } catch (LockDoesNotExistException e) {
                    LOGGER.debug("[REFRESH] End refresh({}, {}, {}, {}). Lock is removed in parallel with refresh. Details: {}", resourceId,
                                 resourceMayNotExist, ttlMayNotBeSet,
                                 lockParameters.getReplicaName(), e.getMessage());
                    return;
                }
            } else {
                if (existingTtlMs > 0) {
                    LOGGER.debug("[REFRESH] Not changing ttl for key {} : {}ms", globalLocksRedisKey, existingTtlMs);
                } else {
                    throw new LockInternalException("Ttl for resource %s is not set, but new ttl is %sms. This should never happen.".formatted(
                            resourceId,
                            newTtlMs));
                }
            }
            if (!replicaLockRepository.findAll(lockParameters.getReplicaLockListRedisKey()).contains(resourceId)) {
                boolean isAdded = replicaLockRepository.add(replicaLockListRedisKey, resourceId);
                if (isAdded) {
                    LOGGER.debug("[REFRESH] Added to replica lock list: key {}, resourceId: {}, replica: {}", globalLocksRedisKey, resourceId,
                                 lockParameters.getReplicaName());
                }
            }
        } else {
            LOGGER.debug("[REFRESH] Not refreshing lock for key {} because no holders of this replica {} have greater expiration time",
                         globalLocksRedisKey, lockParameters.getReplicaName());
        }
        LOGGER.debug("[REFRESH] End refresh({}, {}, {}, {})", resourceId, resourceMayNotExist, ttlMayNotBeSet,
                     lockParameters.getReplicaName());
    }

    @Override
    public long getResidualLockDurationMs(final String resourceId, final String holder, final LockParameters lockParameters) {

        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        final Optional<NonExclusiveLock> lockOptional = lockRepository.get(globalLocksRedisKey, holder);
        if (lockOptional.isPresent()) {
            NonExclusiveLock lock = lockOptional.get();
            long residualLockDurationMs = (lock.getExpirationTime() - replicaLockRepository.getTimeMs());
            if (residualLockDurationMs > 0) {
                return residualLockDurationMs;
            }
        }
        throw new ProcessingTooLongException("Lock is expired", resourceId, holder);
    }

    @Override
    public long getResidualLockTtlMs(final String resourceId, final boolean allowLockProtectionInterval, final LockParameters lockParameters)
            throws LockTtlWithinRacingProtectionIntervalException, LockDoesNotExistException, LockHasNoTtlException {
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        try {
            final Map<String, NonExclusiveLock> allLocks = lockRepository.getAll(globalLocksRedisKey);
            long ttlMs = lockRepository.getTtlMs(globalLocksRedisKey);
            // wa to check if possible, may be removed
            if (allLocks.isEmpty()) {
                LOGGER.error("Key {} exists but there are no holders. Key should have been deleted by redis.", globalLocksRedisKey);
                throw new LockDoesNotExistException(resourceId, "Resource has no holders");
            }
            if (!allowLockProtectionInterval && ttlMs < LOCK_RACING_PROTECTION_INTERVAL_MS) {
                LOGGER.warn("Lock for resource {} has ttl {}ms within LOCK_RACING_PROTECTION_INTERVAL_MS (0..{}ms). "
                                    + "This can happen if refresh periodical job was missed. All current locks: {}",
                            resourceId, ttlMs, LOCK_RACING_PROTECTION_INTERVAL_MS, allLocks);
                throw new LockTtlWithinRacingProtectionIntervalException(resourceId, ttlMs, LOCK_RACING_PROTECTION_INTERVAL_MS);
            }
            LOGGER.debug("Lock for resource {} has ttl {}ms", resourceId, ttlMs);
            return ttlMs;
        } catch (RedisKeyDoesNotExistException e) {
            throw new LockDoesNotExistException(resourceId, e);
        } catch (RedisKeyHasNoTtlException e) {
            throw new LockHasNoTtlException(resourceId, e);
        }
    }

    private Optional<Long> getExistingTtl(String resourceId, boolean resourceMayNotExist, boolean ttlMayNotBeSet, LockParameters lockParameters) {
        try {
            return Optional.of(this.getResidualLockTtlMs(resourceId, ttlMayNotBeSet, lockParameters));
        } catch (LockTtlWithinRacingProtectionIntervalException e) {
            LOGGER.debug("[REFRESH] Lock for resource {} will be removed soon due to small ttl, skipping refresh to avoid racing conditions. "
                                 + "Reason: {}", resourceId, e.getMessage());
            return Optional.empty();
        } catch (LockDoesNotExistException e) {
            LOGGER.debug("[REFRESH] End refresh({}, {}, {}, {}). Lock is removed in parallel with refresh. Details: {}", resourceId,
                         resourceMayNotExist, ttlMayNotBeSet,
                         lockParameters.getReplicaName(), e.getMessage());
            return Optional.empty();
        } catch (LockHasNoTtlException e) {
            if (ttlMayNotBeSet) {
                LOGGER.debug("[REFRESH] Assuming ttl is 0, which is normal if refresh() occurs during lock acquisition. Reason: {}",
                             e.getMessage());
                return Optional.of(0L);
            } else {
                LOGGER.debug("[REFRESH] End refresh({}, {}, {}, {}). Lock is removed and is being in the process of acquiring in parallel with "
                                     + "refresh."
                                     + " Details: {}",
                             resourceId, resourceMayNotExist, ttlMayNotBeSet,
                             lockParameters.getReplicaName(), e.getMessage());
                return Optional.empty();
            }
        }
    }

    private long calculateNewTtlMs(final long currentReplicaLatestExpirationTime) {
        long newTtlMs = currentReplicaLatestExpirationTime - replicaLockRepository.getTimeMs() + LOCK_RACING_PROTECTION_INTERVAL_MS;
        if (newTtlMs > LOCK_RELEASE_ON_REPLICAS_FAILURE_INTERVAL_MS) {
            newTtlMs = LOCK_RELEASE_ON_REPLICAS_FAILURE_INTERVAL_MS;
        }
        return newTtlMs;
    }

    private void removeFromReplicaLockList(final String resourceId, final String replicaLockListRedisKey, final String globalLocksRedisKey) {
        boolean isRemoved = replicaLockRepository.remove(replicaLockListRedisKey, resourceId);
        if (isRemoved) {
            LOGGER.debug("[REFRESH] Removed from replica lock list: key {}, resourceId: {}", globalLocksRedisKey, resourceId);
        }
    }

    private void setResidualLockTtlMs(final String resourceId, final long ttlMs, final LockParameters lockParameters)
            throws LockDoesNotExistException {
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        try {
            lockRepository.setTtlMs(globalLocksRedisKey, ttlMs);
            LOGGER.debug("Lock ttl has changed to {}ms for resource {}", ttlMs, resourceId);
        } catch (RedisKeyDoesNotExistException e) {
            throw new LockDoesNotExistException(resourceId, e);
        }
    }

    private void acquireFromTransferringHolder(String resourceId, String holder, String transferringHolder, NonExclusiveLock lock,
                                               LockParameters lockParameters) {
        final boolean isLockExpired = isLockExpired(resourceId, transferringHolder, lockParameters);
        if (isLockExpired) {
            LOGGER.warn("[ACQUIRE] Lock is expired for transferring holder: {}. resourceId: {}, holder: {}", transferringHolder, resourceId, holder);
            throw new ProcessingTooLongException(
                    "[ACQUIRE] Lock is expired for transferring holder: %s".formatted(transferringHolder), lock);
        }
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);
        lockRepository.put(globalLocksRedisKey, holder, lock);
        if (!transferringHolder.equals(holder)) {
            release(resourceId, transferringHolder, lockParameters);
        }
    }

    /**
     * Checks if lock is expired for specific holder (if holder != null)
     * or for any holder (if holder == null).
     * <p>
     * timeToLive is -2 if the key was removed by redis due to ttl expired
     * timeToLive is -1 if the key was created but ttl not set yet by acquire()
     * timeToLive is 0..LOCK_RACING_PROTECTION_INTERVAL_MS if refresh job was missed
     *
     * @param resourceId     the resource id
     * @param holder         the holder
     * @param lockParameters the lock parameters
     * @return isLockExpired
     */
    public boolean isLockExpired(String resourceId, String holder, LockParameters lockParameters) {
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(resourceId);

        try {
            this.getResidualLockTtlMs(resourceId, false, lockParameters);
        } catch (LockTtlWithinRacingProtectionIntervalException | LockDoesNotExistException e) {
            LOGGER.debug("[IS_EXPIRED] Lock is expired: {}", e.getMessage());
            return true;
        } catch (LockHasNoTtlException e) {
            // will check below if lock is expired
            LOGGER.debug("[IS_EXPIRED] Lock is being acquired in parallel (normal situation): {}", e.getMessage());
        }

        Set<String> currentHolders = lockRepository.getHolders(globalLocksRedisKey);
        LOGGER.debug("[IS_EXPIRED] Current holders for resourceId {}: {}", resourceId, currentHolders);

        try {
            long residualLockDurationMs = getResidualLockDurationMs(resourceId, holder, lockParameters);
            LOGGER.debug("[IS_EXPIRED] Lock is NOT expired: Resource {} has holder {} with residualLockDuration: {}ms",
                         resourceId, holder, residualLockDurationMs);
            return false;
        } catch (ProcessingTooLongException e) {
            LOGGER.debug("[IS_EXPIRED] Continue checking other holders: {}", e.getMessage());
        }

        LOGGER.debug("[IS_EXPIRED] Lock is expired: no holders for resource {} found or all holders' locks are expired. holder: {}",
                     resourceId, holder);
        return true;
    }

    private NonExclusiveLock buildLock(String resourceId, String holder, String replicaId, Set<String> sharingGroups,
                                       int duration, int priority) {
        long acquisitionTime = replicaLockRepository.getTimeMs();
        long expirationTime = acquisitionTime + duration * 1000L;
        return new NonExclusiveLock(holder, replicaId, sharingGroups, resourceId, acquisitionTime, expirationTime, priority);
    }

    private boolean needRetryForLockAcquisition(final LockParameters lockParameters,
                                                final long lockAcquisitionRetryStartRedisTimeMs,
                                                final long lockAcquisitionRetryEndRedisTimeMs,
                                                final String failedAttemptReasonMessage) {

        if (replicaLockRepository.getTimeMs() + lockParameters.getLockAcquisitionRetryIntervalMs() > lockAcquisitionRetryEndRedisTimeMs) {
            LOGGER.debug("[ACQUIRE] Can not acquire lock. Reason: {}", failedAttemptReasonMessage);
            return false;
        }
        LOGGER.debug("[ACQUIRE] Waiting to acquire lock. Reason: {}. Processing time: {}ms. Sleeping for {}ms. Acquisition timeout: {}ms.",
                     failedAttemptReasonMessage, replicaLockRepository.getTimeMs() - lockAcquisitionRetryStartRedisTimeMs,
                     lockParameters.getLockAcquisitionRetryIntervalMs(), lockParameters.getLockAcquisitionRetryTimeoutMs());
        return true;
    }

    /**
     * Check if redis key for the resource exists
     */
    private boolean refreshIfRemovedByTtl(final String resourceId,
                                          final boolean keyMayNotExist,
                                          final boolean ttlMayNotBeSet,
                                          final LockParameters lockParameters,
                                          final String globalLocksRedisKey,
                                          final String replicaLockListRedisKey) {

        try {
            // check only exceptions
            this.getResidualLockTtlMs(resourceId, ttlMayNotBeSet, lockParameters);
        } catch (LockTtlWithinRacingProtectionIntervalException e) {
            LOGGER.debug("[REFRESH] Lock for resource {} will be removed soon due to small ttl, skipping refresh to avoid racing conditions. "
                                 + "Reason: {}", resourceId, e.getMessage());
            return true;
        } catch (LockDoesNotExistException e) {
            // Note: not checking lockRepository.getAll(globalLocksRedisKey).isEmpty(), assuming it will work normally
            removeFromReplicaLockList(resourceId, replicaLockListRedisKey, globalLocksRedisKey);
            if (keyMayNotExist) {
                LOGGER.debug("[REFRESH] Lock for resource {} does not exist (normal situation), skipping refresh. Details: {})", resourceId,
                             e.getMessage());
            } else {
                LOGGER.warn("[REFRESH] Lock for resource {} is released, skipping refresh. Reason: TTL expired. Details: {}",
                            resourceId,
                            e.getMessage());
            }
            return true;
        } catch (LockHasNoTtlException e) {
            if (ttlMayNotBeSet) {
                LOGGER.debug("[REFRESH] Lock for resource {} has no ttl, continue refresh. This can happen if acquire() has created hash "
                                     + "map, but not set ttl yet (normal situation). Details: {})", resourceId, e.getMessage());
                return false;
            }
            LOGGER.debug("[REFRESH] Lock for resource {} has no ttl, skipping refresh. This can happen if acquire() has created hash map, but not "
                                 + "set ttl yet (normal situation). Details: {})", resourceId, e.getMessage());
            return true;
        }
        return false;
    }

    private static void sleepForTimeMs(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionWaitingException("Sleeping has been interrupted. Details: %s".formatted(e.getMessage()));
        }
    }

    private static Set<String> getCommonSharingGroups(List<Set<String>> sharingGroupsList) {
        Set<String> commonSharingGroups = new HashSet<>(sharingGroupsList.get(0));
        for (Set<String> sharingGroups : sharingGroupsList) {
            commonSharingGroups.retainAll(sharingGroups);
        }
        return commonSharingGroups;
    }

    private Optional<Map.Entry<Integer, Long>> requestNextHolderPriority(Integer requestedLockPriority, LockParameters lockParameters) {

        String lockPriorityRedisKey = lockParameters.getLockPriorityKey();
        long currentRedisTimeMs = replicaLockRepository.getTimeMs();
        long priorityExpirationRedisTimeMs = currentRedisTimeMs + 2L * lockParameters.getLockAcquisitionRetryIntervalMs();
        lockPriorityRepository.set(lockPriorityRedisKey, requestedLockPriority, priorityExpirationRedisTimeMs);

        Map<Integer, Long> nextHoldersPriorityMap = lockPriorityRepository.getAllByKey(lockPriorityRedisKey);
        LOGGER.debug("[ACQUIRE] Current redis time: {}ms. Next holder priority list: {}", currentRedisTimeMs, nextHoldersPriorityMap);

        return nextHoldersPriorityMap.entrySet().stream()
                .filter(nextHolderPriority -> nextHolderPriority.getKey() > requestedLockPriority
                        && nextHolderPriority.getValue() > replicaLockRepository.getTimeMs())
                .max(Map.Entry.comparingByKey());
    }
}
