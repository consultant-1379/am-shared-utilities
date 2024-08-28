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
package com.ericsson.am.shared.lock.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.ericsson.am.shared.lock.LockManagerConfig;
import com.ericsson.am.shared.lock.config.TestRedisConfig;
import com.ericsson.am.shared.lock.exceptions.RedisInvalidValueException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyDoesNotExistException;
import com.ericsson.am.shared.lock.exceptions.RedisKeyHasNoTtlException;
import com.ericsson.am.shared.lock.nonexclusive.models.NonExclusiveLock;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveLockRepository;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveLockRepositoryImpl;

@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ContextConfiguration(classes = {
        TestRedisConfig.class,
        LockManagerConfig.class,
        NonExclusiveLockRepositoryImpl.class
})
class NonExclusiveLockRepositoryTest {

    @Autowired
    private NonExclusiveLockRepository lockRepository;

    @Test
    void addLockSuccess() {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks-1:resource1";
        NonExclusiveLock lock = buildLock("holder1");

        lockRepository.put(key, "holder1", lock);

        Optional<NonExclusiveLock> actual = lockRepository.get(key, "holder1");
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(lock);
    }

    @Test
    void addLockIfAnotherLockExists() throws RedisKeyDoesNotExistException, RedisKeyHasNoTtlException {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks-2:resource1";
        NonExclusiveLock firstLock = buildLock("holder1");
        NonExclusiveLock secondLock = buildLock("holder2");

        assertTrue(lockRepository.putIfAbsent(key, "holder1", firstLock));
        assertFalse(lockRepository.putIfAbsent(key, "holder1", firstLock));
        lockRepository.setTtlMs(key, 1000);
        assertTrue(lockRepository.putIfAbsent(key, "holder2", secondLock));
        lockRepository.setTtlMs(key, 3000);

        assertTrue(lockRepository.getTtlMs(key) >= 2000);
        Map<String, NonExclusiveLock> actual = lockRepository.getAll(key);
        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get("holder1").equals(firstLock)).isTrue();
        assertThat(actual.get("holder2").equals(secondLock)).isTrue();
    }

    @Test
    void getIfNotExists() {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks:non-existent";

        Optional<NonExclusiveLock> actual = lockRepository.get(key, "non-existent");
        assertThat(actual).isEmpty();
    }

    @Test
    void getSuccessIfExpired() throws InterruptedException, RedisKeyDoesNotExistException {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks-4:resource1";
        NonExclusiveLock lock = buildLock("holder1");

        lockRepository.put(key, "holder1", lock);
        lockRepository.setTtlMs(key, 2000);
        TimeUnit.SECONDS.sleep(2);

        Optional<NonExclusiveLock> actual = lockRepository.get(key, "holder1");
        assertThat(actual).isEmpty();
    }

    @Test
    void deleteHolderSuccess() {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks-7:resource1";
        NonExclusiveLock lock = buildLock("holder1");

        lockRepository.put(key, "holder1", lock);

        long deletedLocks = lockRepository.delete(key, "holder1");

        assertThat(deletedLocks).isEqualTo(1);
        Optional<NonExclusiveLock> actual = lockRepository.get(key, "holder1");
        assertThat(actual).isEmpty();
    }

    @Test
    void deleteKeySuccess() {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks-7:resource1";
        NonExclusiveLock lock = buildLock("holder1");

        lockRepository.put(key, "holder1", lock);

        boolean deletedLocks = lockRepository.delete(key);

        assertTrue(deletedLocks);
        Map<String, NonExclusiveLock> actual = lockRepository.getAll(key);
        assertThat(actual).isEmpty();
    }

    @Test
    void deleteNonExistentLock() {
        String key = "eric-eo-batch-manager:inventory:item-subset-locks:non-existent-2";

        long deletedLocks = lockRepository.delete(key, "non-existent");

        assertThat(deletedLocks).isEqualTo(0);
    }

    @Test
    void getHoldersIfKeyDoesNotExist() {

        Set<String> holders = lockRepository.getHolders("non-existent");
        assertThat(holders).isEmpty();
    }

    @Test
    void setInvalidTtlError() {
        assertThrows(RedisInvalidValueException.class, () -> lockRepository.setTtlMs("key", -1));
    }

    @Test
    void setTtlIfKeyDoesNotExistError() {
        assertThrows(RedisKeyDoesNotExistException.class, () -> lockRepository.setTtlMs("non-existent", 1));
    }

    private NonExclusiveLock buildLock(String holder) {
        return new NonExclusiveLock(holder, "replica1", Collections.emptySet(), "all", 1698240881, 1698240885, 0);
    }
}