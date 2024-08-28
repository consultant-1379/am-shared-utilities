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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.am.shared.lock.LockManagerConfig;
import com.ericsson.am.shared.lock.config.TestRedisConfig;
import com.ericsson.am.shared.lock.exceptions.LockDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.LockHasNoTtlException;
import com.ericsson.am.shared.lock.exceptions.LockTtlWithinRacingProtectionIntervalException;
import com.ericsson.am.shared.lock.exceptions.ProcessingTooLongException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyHasNoTtlException;
import com.ericsson.am.shared.lock.models.LockParameters;
import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveLockRepositoryImpl;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveReplicaLockRepositoryImpl;
import com.ericsson.am.shared.lock.repository.LockPriorityRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringBootTest
@ContextConfiguration(classes = {
        TestRedisConfig.class,
        LockManagerConfig.class,
        NonExclusiveDistributedLockManagerImpl.class,
        NonExclusiveLockRepositoryImpl.class,
        NonExclusiveReplicaLockRepositoryImpl.class,
        LockPriorityRepositoryImpl.class,
        ObjectMapper.class
})
@TestPropertySource(properties = {
        "logging.level.com.ericsson.am.shared.lock=DEBUG"
})
public class NonExclusiveDistributedLockManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonExclusiveDistributedLockManagerTest.class);

    private static final int LOCK_ACQUISITION_RETRY_TIMEOUT = 2000;
    private static final int LOCK_ACQUISITION_RETRY_INTERVAL = 200;
    private static final int MAX_TIME_TO_ACQUIRE_LOCK_MS = 200;
    private static final int LOCK_RACING_PROTECTION_INTERVAL_MS = 2000;
    private static final String RESOURCE_ID_1 = "resource1";
    private static final String RESOURCE_ID_2 = "resource2";
    private static final String HOLDER_1 = "holder1";
    private static final String HOLDER_2 = "holder2";
    private static final String HOLDER_3 = "holder3";
    private final LockParameters lockParameters = new LockParameters(
            "eric-eo-batch-manager", "eric-eo-batch-manager-replica-1", "inventory",
            LOCK_ACQUISITION_RETRY_TIMEOUT,
            LOCK_ACQUISITION_RETRY_INTERVAL);
    private final LockParameters lockParametersWithZeroRetryTimeout = new LockParameters(
            "eric-eo-batch-manager", "eric-eo-batch-manager-replica-1", "inventory",
            0,
            LOCK_ACQUISITION_RETRY_INTERVAL);
    private final String resource1GlobalLockRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(RESOURCE_ID_1);
    private final String resource2GlobalLockRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(RESOURCE_ID_2);

    @Autowired
    private NonExclusiveDistributedLockManagerImpl nonExclusiveDistributedLockManager;

    @Autowired
    private NonExclusiveReplicaLockRepositoryImpl replicaLockRepository;

    @SpyBean
    private NonExclusiveLockRepositoryImpl lockRepository;

    @AfterEach
    public void after() {
        Set<String> holdersResource1 = lockRepository.getHolders(resource1GlobalLockRedisKey);
        Set<String> holdersResource2 = lockRepository.getHolders(resource2GlobalLockRedisKey);
        holdersResource1.forEach(holder -> lockRepository.delete(String.format(lockParameters.getGlobalLocksRedisKey(),
                                                                               RESOURCE_ID_1), holder));
        holdersResource2.forEach(holder -> lockRepository.delete(String.format(lockParameters.getGlobalLocksRedisKey(),
                                                                               RESOURCE_ID_2), holder));
        replicaLockRepository.remove(lockParameters.getReplicaLockListRedisKey(), RESOURCE_ID_1);
        replicaLockRepository.remove(lockParameters.getReplicaLockListRedisKey(), RESOURCE_ID_2);
    }

    @Test
    void acquireAndReleaseExclusiveLockSuccess() {

        int durationS = 10;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        durationS,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();

        long ttlMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThat(ttlMs).isGreaterThanOrEqualTo(durationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
        assertThat(ttlMs).isLessThanOrEqualTo(durationS * 1000);

        nonExclusiveDistributedLockManager.release(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters))
                .isInstanceOf(ProcessingTooLongException.class)
                .hasMessage("Lock is expired. resourceId: resource1, holder: holder1");
    }

    @Test
    void acquireTwoResourcesByOneHolderExclusiveSuccess() {
        int resource1LockDurationS = 10;
        int resource2LockDurationS = 15;
        boolean isFirstResourceAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                     HOLDER_1,
                                                                                     resource1LockDurationS,
                                                                                     null,
                                                                                     0,
                                                                                     Collections.emptySet(),
                                                                                     lockParameters);
        assertThat(isFirstResourceAcquired).isTrue();
        boolean isSecondResourceAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_2,
                                                                                      HOLDER_1,
                                                                                      resource2LockDurationS,
                                                                                      null,
                                                                                      0,
                                                                                      Collections.emptySet(),
                                                                                      lockParameters);
        assertThat(isSecondResourceAcquired).isTrue();
        long resource1ResidualLockDurationMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters);
        long resource2ResidualLockDurationMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_2, HOLDER_1, lockParameters);

        assertThat(resource1ResidualLockDurationMs).isGreaterThanOrEqualTo(resource1LockDurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
        assertThat(resource1ResidualLockDurationMs).isLessThanOrEqualTo(resource1LockDurationS * 1000);

        assertThat(resource2ResidualLockDurationMs).isGreaterThanOrEqualTo(resource2LockDurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
        assertThat(resource2ResidualLockDurationMs).isLessThanOrEqualTo(resource2LockDurationS * 1000);
    }

    @Test
    void acquireExclusiveLockForTheSameResourceTwoTimesError() {
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        10,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                HOLDER_1,
                                                                15,
                                                                null,
                                                                0,
                                                                Collections.emptySet(),
                                                                lockParameters);
        assertThat(isAcquired).isFalse();
    }

    @Test
    void acquireExclusiveLockForTheSameByTwoHoldersError() {
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 10,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_2,
                                                                                 20,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder2).isFalse();
    }

    @Test
    void releaseNonExistingLockSuccess() {
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        10,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        nonExclusiveDistributedLockManager.release(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();

        nonExclusiveDistributedLockManager.release(RESOURCE_ID_1, HOLDER_1, lockParameters);
    }

    @Test
    void exclusiveLockExpiresWhenHoldingMoreThanDurationSuccess() throws InterruptedException {
        int firstLockDuration = 2;
        int timeToWaitForExpiration = firstLockDuration + 2; // including LOCK_RACING_PROTECTION_INTERVAL
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 firstLockDuration,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        sleepForMs(timeToWaitForExpiration * 1000);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();

        boolean isAcquiredByHolder2 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_2,
                                                                                 20,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder2).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_2, lockParameters)).isFalse();

        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.updateDuration(RESOURCE_ID_1, HOLDER_1, 1, lockParameters))
                .isInstanceOf(ProcessingTooLongException.class)
                .hasMessage("Long running processing has been rolled back. resourceId: resource1, holder: holder1");
    }

    @Test
    void exclusiveLockExpiresOnReplicaFailureSuccess() throws InterruptedException {
        // lockReleaseOnReplicaFailureInterval = 9 by default;
        int sleepInterval = 9000;
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 10,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        sleepForMs(sleepInterval);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();

        verifyLockInRepository(HOLDER_1, false);

        boolean isAcquiredByHolder2 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_2,
                                                                                 20,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder2).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_2, lockParameters)).isFalse();

        verifyLockInRepository(HOLDER_2, true);
    }

    @Test
    void acquireExclusiveLockWithTransferringHolderSuccess() {

        int holder1LockDurationS = 10;
        int holder2LockDurationS = 20;

        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1LockDurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_2,
                                                                                 holder2LockDurationS,
                                                                                 HOLDER_1,
                                                                                 1,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder2).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_2, lockParameters)).isFalse();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParameters))
                .isGreaterThanOrEqualTo(holder2LockDurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);

        nonExclusiveDistributedLockManager.release(RESOURCE_ID_1, HOLDER_2, lockParameters);
        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParameters))
                .isInstanceOf(ProcessingTooLongException.class)
                .hasMessage("Lock is expired. resourceId: resource1, holder: holder2");
    }

    @Test
    void acquireExclusiveLockWithSameTransferringHolderSuccess() {

        int holder1LockDurationS = 10;
        int holder1TransferringLockDurationS = 20;

        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1LockDurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder1WithSameTransferringHolder = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                           HOLDER_1,
                                                                                                           holder1TransferringLockDurationS,
                                                                                                           HOLDER_1,
                                                                                                           1,
                                                                                                           Collections.emptySet(),
                                                                                                           lockParameters);
        assertThat(isAcquiredByHolder1WithSameTransferringHolder).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters))
                .isGreaterThanOrEqualTo(holder1TransferringLockDurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    @Test
    void acquireExpiredExclusiveLockWithTransferringHolderError() throws InterruptedException {
        int firstLockDuration = 2;
        int timeToWaitForExpiration = firstLockDuration + 2; // including LOCK_RACING_PROTECTION_INTERVAL
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 firstLockDuration,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        sleepForMs(timeToWaitForExpiration * 1000);

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();
        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                            HOLDER_2,
                                                                            20,
                                                                            HOLDER_1,
                                                                            0,
                                                                            Collections.emptySet(),
                                                                            lockParameters))
                .isInstanceOf(ProcessingTooLongException.class);

        boolean isAcquiredByHolder2WithoutTransferringHolder = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                          HOLDER_2,
                                                                                                          20,
                                                                                                          null,
                                                                                                          0,
                                                                                                          Collections.emptySet(),
                                                                                                          lockParameters);
        assertThat(isAcquiredByHolder2WithoutTransferringHolder).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_2, lockParameters)).isFalse();
    }

    @Test
    void acquireExpiredExclusiveLockWithTtlWithoutTransferringHolderError() throws InterruptedException {
        int firstLockDuration = 1; // ttl will be 4s
        int timeToWaitForExpiration = 1; // including LOCK_RACING_PROTECTION_INTERVAL
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 firstLockDuration,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        sleepForMs(timeToWaitForExpiration * 1000);

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();
        assertThat(nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                              HOLDER_2,
                                                              20,
                                                              null,
                                                              0,
                                                              Set.of(HOLDER_1),
                                                              lockParametersWithZeroRetryTimeout)).isFalse();
    }

    /**
     * Acquire exclusive lock waiting when previous lock is released success.
     * holder1()->true         | holder2()->FALSE         | expire holder1 | holder2()->true
     */
    @Test
    void acquireExclusiveLockWaitingWhenPreviousLockIsReleasedSuccess() throws InterruptedException {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2WithoutTransferringHolder = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                          HOLDER_2,
                                                                                                          holder2DurationS,
                                                                                                          null,
                                                                                                          0,
                                                                                                          Collections.emptySet(),
                                                                                                          lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2WithoutTransferringHolder).isFalse();

        sleepForMs(holder1DurationS * 1000 + LOCK_RACING_PROTECTION_INTERVAL_MS);
        isAcquiredByHolder2WithoutTransferringHolder = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                  HOLDER_2,
                                                                                                  holder2DurationS,
                                                                                                  null,
                                                                                                  0,
                                                                                                  Collections.emptySet(),
                                                                                                  lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2WithoutTransferringHolder).isTrue();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder2DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    /**
     * Acquire lock with sharing group waiting when previous lock is released success.
     * holder1()->true         | holder2("group2")->FALSE | expire holder1 | holder2("group2")->true
     */
    @Test
    void acquireLockWithSharingGroupsWaitingWhenExclusiveLockIsReleasedSuccess() throws InterruptedException {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2WithSharingGroup = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                 HOLDER_2,
                                                                                                 holder2DurationS,
                                                                                                 null,
                                                                                                 0,
                                                                                                 Set.of(HOLDER_2),
                                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2WithSharingGroup).isFalse();

        sleepForMs(holder1DurationS * 1000 + LOCK_RACING_PROTECTION_INTERVAL_MS);
        isAcquiredByHolder2WithSharingGroup = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                         HOLDER_2,
                                                                                         holder2DurationS,
                                                                                         null,
                                                                                         0,
                                                                                         Set.of(HOLDER_2),
                                                                                         lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2WithSharingGroup).isTrue();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder2DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    /**
     * Acquire lock without sharing group waiting when previous exclusive lock with group is released success.
     * holder1("group1")->true | holder2()->FALSE         | expire holder1 | holder2()->true
     */
    @Test
    void acquireExclusiveLockWaitingWhenLockWithSharingGroupIsReleasedSuccess() throws InterruptedException {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2Exclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                          HOLDER_2,
                                                                                          holder2DurationS,
                                                                                          null,
                                                                                          0,
                                                                                          Collections.emptySet(),
                                                                                          lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2Exclusive).isFalse();

        sleepForMs(holder1DurationS * 1000 + LOCK_RACING_PROTECTION_INTERVAL_MS);
        isAcquiredByHolder2Exclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                  HOLDER_2,
                                                                                  holder2DurationS,
                                                                                  null,
                                                                                  0,
                                                                                  Collections.emptySet(),
                                                                                  lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2Exclusive).isTrue();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder2DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    /**
     * Acquire lock with sharing group 1 waiting when previous lock with group 2 is released success.
     * holder1("group1")->true | holder2("group2")->FALSE | expire holder1 | holder2("group2")->true
     */
    @Test
    void acquireLockWithSharingGroupWaitingWhenLockWithSharingGroupIsReleasedSuccess() throws InterruptedException {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2Exclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                          HOLDER_2,
                                                                                          holder2DurationS,
                                                                                          null,
                                                                                          0,
                                                                                          Set.of(HOLDER_2),
                                                                                          lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2Exclusive).isFalse();

        sleepForMs(holder1DurationS * 1000 + LOCK_RACING_PROTECTION_INTERVAL_MS);
        isAcquiredByHolder2Exclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                  HOLDER_2,
                                                                                  holder2DurationS,
                                                                                  null,
                                                                                  0,
                                                                                  Set.of(HOLDER_2),
                                                                                  lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2Exclusive).isTrue();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder2DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    /**
     * Acquire lock with different sharing groups waiting when previous lock with non-matching group is released success.
     * holder1("group1")->true | holder2("group1", "group2")->true | holder3("group2", "group3")->FALSE | expire holder 1 | holder3("group2",
     * "group3")->true
     */
    @Test
    void acquireLockWithSharingGroupWaitingWhenLockNonMatchingGroupIsReleasedSuccess() throws InterruptedException {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_2,
                                                                                 holder2DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1, HOLDER_2),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2).isTrue();

        boolean isAcquiredByHolder3 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 "holder3",
                                                                                 holder2DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_2, "holder3"),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder3).isFalse();

        sleepForMs(holder1DurationS * 1000 + LOCK_RACING_PROTECTION_INTERVAL_MS);

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_2, lockParameters)).isFalse();

        isAcquiredByHolder3 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                         "holder3",
                                                                         holder2DurationS,
                                                                         null,
                                                                         0,
                                                                         Set.of(HOLDER_2, "holder3"),
                                                                         lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder3).isTrue();
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, "holder3", lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder2DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    /**
     * Acquire two locks with  same sharing group success.
     * holder1("group1")->true | holder2("group1")->true
     */
    @Test
    void acquireTwoLocksWithSameSharingGroupSuccess() throws InterruptedException {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();
        boolean isAcquiredByHolder2Exclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                          HOLDER_2,
                                                                                          holder2DurationS,
                                                                                          null,
                                                                                          0,
                                                                                          Set.of(HOLDER_1),
                                                                                          lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2Exclusive).isTrue();

        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder1DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
        assertThat(nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_2, lockParametersWithZeroRetryTimeout))
                .isGreaterThanOrEqualTo(holder2DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    @Test
    void acquireExclusiveLockWithTooSmallLockRetryTimeoutError() {
        int holder1DurationS = 1; // ttl will be set to 3
        int holder2DurationS = 20; // ttl will be set to 3

        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 holder1DurationS,
                                                                                 null,
                                                                                 0,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();

        LockParameters lockParameters2 = new LockParameters(
                "eric-eo-batch-manager", "eric-eo-batch-manager-replica-1", "inventory",
                holder1DurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS,
                LOCK_ACQUISITION_RETRY_INTERVAL);
        boolean isAcquiredByHolder2WithoutTransferringHolder = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                          HOLDER_2,
                                                                                                          holder2DurationS,
                                                                                                          null,
                                                                                                          0,
                                                                                                          Collections.emptySet(),
                                                                                                          lockParameters2);
        assertThat(isAcquiredByHolder2WithoutTransferringHolder).isFalse();
    }

    @Test
    void acquireLockWaitingWhenHigherPriorityLockIsReleasedError() {
        int firstLockDuration = 2;
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 firstLockDuration,
                                                                                 null,
                                                                                 2,
                                                                                 Collections.emptySet(),
                                                                                 lockParameters);
        assertThat(isAcquiredByHolder1).isTrue();

        LockParameters lockParameters2 = new LockParameters(
                "eric-eo-batch-manager", "eric-eo-batch-manager-replica-1", "inventory",
                0,
                LOCK_ACQUISITION_RETRY_INTERVAL);
        boolean isAcquiredByHolder2WithoutTransferringHolder = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                                          HOLDER_2,
                                                                                                          10,
                                                                                                          null,
                                                                                                          1,
                                                                                                          Collections.emptySet(),
                                                                                                          lockParameters2);
        assertThat(isAcquiredByHolder2WithoutTransferringHolder).isFalse();
    }

    @Test
    void acquireExclusiveLockWhenNonExclusivePresentError() {
        boolean isAcquiredByHolder1NonExclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                             HOLDER_1,
                                                                                             1,
                                                                                             null,
                                                                                             0,
                                                                                             Set.of(HOLDER_1, HOLDER_2),
                                                                                             lockParameters);
        assertThat(isAcquiredByHolder1NonExclusive).isTrue();

        boolean isAcquiredByHolder2NonExclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                             HOLDER_2,
                                                                                             1,
                                                                                             null,
                                                                                             0,
                                                                                             Set.of(HOLDER_2),
                                                                                             lockParameters);
        assertThat(isAcquiredByHolder2NonExclusive).isTrue();

        LockParameters testLockParameters = new LockParameters(
                "eric-eo-batch-manager", "eric-eo-batch-manager-replica-1", "inventory",
                1,
                LOCK_ACQUISITION_RETRY_INTERVAL);
        boolean isAcquiredByHolder2Exclusive = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                          HOLDER_2,
                                                                                          2,
                                                                                          null,
                                                                                          0,
                                                                                          Collections.emptySet(),
                                                                                          testLockParameters);
        assertThat(isAcquiredByHolder2Exclusive).isFalse();
    }

    @Test
    void updateDurationSuccess() {

        int durationS = 10;
        int newDurationS = 20;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        durationS,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        long ttlMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThat(ttlMs).isGreaterThanOrEqualTo(durationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);

        nonExclusiveDistributedLockManager.updateDuration(RESOURCE_ID_1, HOLDER_1, newDurationS, lockParameters);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        ttlMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThat(ttlMs).isGreaterThanOrEqualTo(newDurationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);
    }

    @Test
    void updateDurationForOtherReplicaError() {
        int duration = 10;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();

        String globalLocksRedisKey = resource1GlobalLockRedisKey;
        Optional<NonExclusiveLock> lockBeforeDurationUpdate = lockRepository.get(globalLocksRedisKey, HOLDER_1);

        LockParameters otherReplicaLockParameters = new LockParameters(
                "eric-eo-batch-manager", "eric-eo-batch-manager-replica-2", "inventory",
                LOCK_ACQUISITION_RETRY_TIMEOUT,
                LOCK_ACQUISITION_RETRY_INTERVAL);
        nonExclusiveDistributedLockManager.updateDuration(RESOURCE_ID_1, HOLDER_1, 20, otherReplicaLockParameters);

        Optional<NonExclusiveLock> lockAfterDurationUpdate = lockRepository.get(globalLocksRedisKey, HOLDER_1);

        assertThat(lockAfterDurationUpdate.get().getExpirationTime()).isGreaterThan(lockBeforeDurationUpdate.get().getExpirationTime());
    }

    @Test
    void acquireExclusiveLockWithDifferentPriorityByMultipleHoldersSuccess() {
        int lockDuration = 1; // ttl will be calculated as 3sec
        int numberOfHolders = 5;
        LockParameters testLockParameters = new LockParameters(
                "eric-eo-batch-manager", "eric-eo-batch-manager-replica-1", "inventory",
                17000, // 1s + 5*1s + 200ms*2 + 200ms + 2000ms*5 // - lock is not removed while in LOCK_RACING_PROTECTION_INTERVAL_MS
                200);

        List<Callable<Pair<Integer, Long>>> acquireLockTasks =
                createAcquireLockTasks(testLockParameters, numberOfHolders);

        // acquire lock to not give possibility for the next holder take lock immediately
        nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                   "holder0",
                                                   lockDuration,
                                                   null,
                                                   0,
                                                   Collections.emptySet(),
                                                   testLockParameters);

        List<Pair<Integer, Long>> priorityAcquisitionTimePairResults = executeAcquireLockRequests(numberOfHolders, acquireLockTasks);

        priorityAcquisitionTimePairResults.sort(Comparator.comparingLong(Pair<Integer, Long>::getRight));

        verifyLocksAcquiredInPriorityOder(priorityAcquisitionTimePairResults);

        List<Pair<Integer, String>> priorityAcquisitionDateTimePairResults = priorityAcquisitionTimePairResults.stream().map(pair -> {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(pair.getRight()), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");
            return Pair.of(pair.getLeft(), dateTime.format(formatter));
        }).collect(Collectors.toList());

        LOGGER.info("Retrieved acquired lock results, pairs lock priority:acquisitionTime: {}", priorityAcquisitionDateTimePairResults);
    }

    /**
     * Test refreshAll() anf refresh() removes lock when ttl greater than duration success.
     * Assumption: logic between sleeps takes less than 1 s to execute, otherwise the test result will be false-negative
     */
    @Test
    void refreshAllRemovesLockWhenTtlGreaterThanDurationSuccess()
    throws InterruptedException, RedisKeyDoesNotExistException, RedisKeyHasNoTtlException, LockDoesNotExistException,
            LockTtlWithinRacingProtectionIntervalException, LockHasNoTtlException {

        int durationS = 9;
        // maxCalculatedTtl must be set to LOCK_RELEASE_ON_REPLICAS_FAILURE_INTERVAL value
        int maxCalculatedTtlMs = 9000;
        int firstRefreshInterval3Ms = 3000;
        int secondRefreshInterval6Ms = 6000;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        durationS,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();

        long residualLockDurationMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThat(residualLockDurationMs).isLessThanOrEqualTo(durationS * 1000); // 9s
        assertThat(residualLockDurationMs).isGreaterThanOrEqualTo(durationS * 1000 - MAX_TIME_TO_ACQUIRE_LOCK_MS);

        long ttlMs = nonExclusiveDistributedLockManager.getResidualLockTtlMs(RESOURCE_ID_1, false, lockParameters);
        // 9s: (residualLockDuration + racingProtectionInterval = 11s) is greater than maxCalculatedTtl
        assertThat(ttlMs).isLessThanOrEqualTo(maxCalculatedTtlMs); // 9s
        assertThat(ttlMs).isGreaterThanOrEqualTo(maxCalculatedTtlMs - MAX_TIME_TO_ACQUIRE_LOCK_MS);

        sleepForMs(firstRefreshInterval3Ms);
        // 1) refreshAll - > should prolong ttl for the lock
        nonExclusiveDistributedLockManager.refreshAll(lockParameters);

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        residualLockDurationMs = nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters);
        assertThat(residualLockDurationMs).isLessThanOrEqualTo(durationS * 1000 - firstRefreshInterval3Ms); // 6s
        assertThat(residualLockDurationMs).isGreaterThanOrEqualTo(durationS * 1000 - firstRefreshInterval3Ms - MAX_TIME_TO_ACQUIRE_LOCK_MS);
        ttlMs = nonExclusiveDistributedLockManager.getResidualLockTtlMs(RESOURCE_ID_1, true, lockParameters);
        assertThat(ttlMs).isLessThanOrEqualTo(durationS * 1000); // 9s maxCalculatedTtl
        assertThat(ttlMs).isGreaterThanOrEqualTo(
                durationS * 1000 - firstRefreshInterval3Ms + LOCK_RACING_PROTECTION_INTERVAL_MS - MAX_TIME_TO_ACQUIRE_LOCK_MS);

        // lock isExpired, but not removed by ttl(redis expireTime)
        sleepForMs(secondRefreshInterval6Ms);

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();
        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.getResidualLockTtlMs(RESOURCE_ID_1, false, lockParameters))
                .isInstanceOf(LockTtlWithinRacingProtectionIntervalException.class);

        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters))
                .isInstanceOf(ProcessingTooLongException.class)
                .hasMessage("Lock is expired. resourceId: resource1, holder: holder1");

        ttlMs = nonExclusiveDistributedLockManager.getResidualLockTtlMs(RESOURCE_ID_1, true, lockParameters);
        assertThat(ttlMs).isLessThanOrEqualTo(durationS * 1000 - secondRefreshInterval6Ms); // 3s
        assertThat(ttlMs).isGreaterThanOrEqualTo(0); // 2s

        assertThat(lockRepository.getAll(resource1GlobalLockRedisKey)).isNotEmpty();
        assertThat(lockRepository.getHolders(resource1GlobalLockRedisKey)).isNotEmpty();
        assertThat(replicaLockRepository.findAll(lockParameters.getReplicaLockListRedisKey())).isNotEmpty();

        // 2) refreshAll - > should not prolong ttl because lock is expired due to expiration time passed
        nonExclusiveDistributedLockManager.refreshAll(lockParameters);

        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isTrue();
        assertThatThrownBy(() -> nonExclusiveDistributedLockManager.getResidualLockDurationMs(RESOURCE_ID_1, HOLDER_1, lockParameters))
                .isInstanceOf(ProcessingTooLongException.class)
                .hasMessage("Lock is expired. resourceId: resource1, holder: holder1");
    }

    @Test
    void refreshOtherReplicaLockSuccess() {

        int duration = 2;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        LockParameters lockParametersReplica2 = new LockParameters(
                "eric-eo-batch-manager", "eric-eo-batch-manager-replica-2", "inventory",
                1000,
                LOCK_ACQUISITION_RETRY_INTERVAL);

        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParametersReplica2);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
        assertThat(lockRepository.getAll(resource1GlobalLockRedisKey)).isNotEmpty();
        assertThat(lockRepository.getHolders(resource1GlobalLockRedisKey)).isNotEmpty();
        assertThat(replicaLockRepository.findAll(lockParameters.getReplicaLockListRedisKey())).isNotEmpty();
    }

    @Test
    void refreshIfLockIsExpiredSuccess() throws InterruptedException, RedisKeyDoesNotExistException, RedisKeyHasNoTtlException {

        int duration = 1;
        boolean isAcquiredByHolder1 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_1,
                                                                                 duration,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1, HOLDER_2),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder1).isTrue();

        sleepForMs(duration * 1000);
        long ttlMs = lockRepository.getTtlMs(resource1GlobalLockRedisKey);
        assertThat(ttlMs).isLessThanOrEqualTo(LOCK_RACING_PROTECTION_INTERVAL_MS);
        assertThat(ttlMs).isGreaterThan(duration);

        boolean isAcquiredByHolder2 = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                                 HOLDER_2,
                                                                                 duration,
                                                                                 null,
                                                                                 0,
                                                                                 Set.of(HOLDER_1, HOLDER_2),
                                                                                 lockParametersWithZeroRetryTimeout);
        assertThat(isAcquiredByHolder2).isFalse();

        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParameters);

        ttlMs = lockRepository.getTtlMs(resource1GlobalLockRedisKey);
        assertThat(ttlMs).isLessThanOrEqualTo(LOCK_RACING_PROTECTION_INTERVAL_MS);
        assertThat(ttlMs).isGreaterThan(0);
    }

    @Test
    void refreshProvidesWarningIfLockDoesNotExistSuccess() {
        List<ILoggingEvent> loggingEvents = runListAppender();
        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParameters);
        List<Level> actualMessages = loggingEvents.stream().map(ILoggingEvent::getLevel).toList();
        assertFalse(actualMessages.isEmpty());
        assertThat(actualMessages.stream().filter(m -> m == Level.WARN).count()).isEqualTo(1);
    }

    @Test
    void refreshOwnReplicaLockDoNothingIfKeyDoesNotExistSuccess() throws RedisKeyDoesNotExistException, RedisKeyHasNoTtlException {

        int duration = 2;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();

        AtomicInteger getTtlCallCount = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            if (getTtlCallCount.incrementAndGet() == 2) {
                throw new RedisKeyDoesNotExistException("Key does not exist");
            }
            return invocation.callRealMethod();
        }).when(lockRepository).getTtlMs(resource1GlobalLockRedisKey);

        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParameters);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
    }

    @Test
    void refreshOwnReplicaLockDoNothingIfKeyTtlIsNotSetSuccess() throws RedisKeyDoesNotExistException, RedisKeyHasNoTtlException {

        int duration = 2;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();

        AtomicInteger getTtlCallCount = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            if (getTtlCallCount.incrementAndGet() == 2) {
                throw new RedisKeyHasNoTtlException("Key has no ttl");
            }
            return invocation.callRealMethod();
        }).when(lockRepository).getTtlMs(resource1GlobalLockRedisKey);

        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParameters);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
    }

    @Test
    void refreshOwnReplicaLockDoNothingIfKeyTtlWithinLockProtectionSuccess() throws RedisKeyDoesNotExistException, RedisKeyHasNoTtlException {

        int duration = 2;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();

        AtomicInteger getTtlCallCount = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            if (getTtlCallCount.incrementAndGet() == 2) {
                return 1500L;
            }
            return invocation.callRealMethod();
        }).when(lockRepository).getTtlMs(resource1GlobalLockRedisKey);

        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParameters);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
    }

    @Test
    void refreshOwnReplicaLockDoNothingIfKeyDoesNotExistOnTtlUpdateSuccess() throws RedisKeyDoesNotExistException {

        int duration = 10;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();

        AtomicInteger setTtlCallCount = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            if (setTtlCallCount.incrementAndGet() == 1) {
                throw new RedisKeyDoesNotExistException("Key does not exist");
            }
            return invocation.callRealMethod();
        }).when(lockRepository).setTtlMs(any(String.class), any(Long.class));

        nonExclusiveDistributedLockManager.refresh(RESOURCE_ID_1, false, false, lockParameters);
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
    }

    @Test
    void acquireIfLockKeyWasRemovedInParallelBeforeAddingNewHolderSuccess() throws RedisKeyDoesNotExistException {

        AtomicInteger setTtlCallCount = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            if (setTtlCallCount.incrementAndGet() == 1) {
                throw new RedisKeyDoesNotExistException("Key does not exist");
            }
            return invocation.callRealMethod();
        }).when(lockRepository).setTtlMs(any(String.class), any(Long.class));

        int duration = 1;
        boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                        HOLDER_1,
                                                                        duration,
                                                                        null,
                                                                        0,
                                                                        Collections.emptySet(),
                                                                        lockParameters);
        assertThat(isAcquired).isTrue();
        assertThat(nonExclusiveDistributedLockManager.isLockExpired(RESOURCE_ID_1, HOLDER_1, lockParameters)).isFalse();
    }

    private static void sleepForMs(final int timeToWaitForExpiration) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(timeToWaitForExpiration);
    }

    private static void verifyLocksAcquiredInPriorityOder(final List<Pair<Integer, Long>> priorityAcquisitionTimePairResults) {
        IntStream.range(0, priorityAcquisitionTimePairResults.size() - 1).forEach(i ->
                                                                                  {
                                                                                      if (priorityAcquisitionTimePairResults.get(i).getLeft()
                                                                                              < priorityAcquisitionTimePairResults.get(i + 1)
                                                                                              .getLeft()) {
                                                                                          throw new RuntimeException(
                                                                                                  ("Thread with priority %s should have acquired "
                                                                                                          + "lock after thread with priority %s.")
                                                                                                          .formatted(
                                                                                                                  priorityAcquisitionTimePairResults.get(
                                                                                                                          i).getLeft(),
                                                                                                                  priorityAcquisitionTimePairResults.get(
                                                                                                                          i + 1).getLeft()));
                                                                                      }
                                                                                  });
    }

    private static List<Pair<Integer, Long>> executeAcquireLockRequests(final int numberOfHolders,
                                                                        List<Callable<Pair<Integer, Long>>> acquireLockTasks) {

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(numberOfHolders);
        ExecutorCompletionService<Pair<Integer, Long>> completionService = new ExecutorCompletionService<>(executorService);

        IntStream.range(0, numberOfHolders).forEach(index ->
                                                            executorService.schedule(() -> {
                                                                completionService.submit(acquireLockTasks.get(index));
                                                                return null;
                                                            }, 0, TimeUnit.MILLISECONDS));
        List<Pair<Integer, Long>> results = new ArrayList<>();
        try {
            for (int i = 0; i < numberOfHolders; i++) {
                results.add(completionService.take().get());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch feature results.", e);
        } finally {
            executorService.shutdown();
        }
        return results;
    }

    private List<Callable<Pair<Integer, Long>>> createAcquireLockTasks(final LockParameters testLockParameters, int numberOfTasks) {
        List<Callable<Pair<Integer, Long>>> acquireLockTasks = new ArrayList<>();
        IntStream.rangeClosed(1, numberOfTasks).forEach(i -> acquireLockTasks.add(createAcquireLockTask(testLockParameters, i)));
        Collections.shuffle(acquireLockTasks);
        return acquireLockTasks;
    }

    private Callable<Pair<Integer, Long>> createAcquireLockTask(final LockParameters testLockParameters, final int index) {
        return () -> {
            String holder = "holder%s".formatted(index);
            boolean isAcquired = nonExclusiveDistributedLockManager.acquire(RESOURCE_ID_1,
                                                                            holder,
                                                                            1,
                                                                            null,
                                                                            index,
                                                                            Collections.emptySet(),
                                                                            testLockParameters);
            if (isAcquired) {
                String globalLocksRedisKey = testLockParameters.getGlobalLocksRedisKey().formatted(RESOURCE_ID_1);
                Optional<NonExclusiveLock> lock = lockRepository.get(globalLocksRedisKey, holder);
                assertThat(lock.isPresent()).isTrue();
                return Pair.of(index, lock.get().getAcquisitionTime());
            }
            throw new RuntimeException("Lock is not acquired for holder %s".formatted(holder));
        };
    }

    private void verifyLockInRepository(String holder, boolean isGlobalKeyPresent) {
        String globalLocksRedisKey = lockParameters.getGlobalLocksRedisKey().formatted(NonExclusiveDistributedLockManagerTest.RESOURCE_ID_1);
        assertThat(lockRepository.get(globalLocksRedisKey, holder).isPresent()).isEqualTo(isGlobalKeyPresent);
        Set<String> resourceIdsForReplicas = replicaLockRepository.findAll(lockParameters.getReplicaLockListRedisKey());
        assertThat(resourceIdsForReplicas).isNotNull();
        assertThat(resourceIdsForReplicas).isNotEmpty();
        assertThat(resourceIdsForReplicas.size()).isEqualTo(1);
    }

    private List<ILoggingEvent> runListAppender() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(NonExclusiveDistributedLockManagerImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender.list;
    }
}
