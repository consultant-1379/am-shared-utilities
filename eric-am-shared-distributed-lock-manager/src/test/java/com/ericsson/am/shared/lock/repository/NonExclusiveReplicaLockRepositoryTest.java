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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.ericsson.am.shared.lock.LockManagerConfig;
import com.ericsson.am.shared.lock.config.TestRedisConfig;
import com.ericsson.am.shared.lock.nonexclusive.repository.NonExclusiveReplicaLockRepositoryImpl;

@SpringBootTest
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ContextConfiguration(classes = {
        TestRedisConfig.class,
        LockManagerConfig.class,
        NonExclusiveReplicaLockRepositoryImpl.class
})
class NonExclusiveReplicaLockRepositoryTest {

    @Autowired
    private NonExclusiveReplicaLockRepositoryImpl replicaLockRepository;

    @Test
    void addLockSuccess() {
        String key = "eric-eo-batch-manager:inventory:replicas:eric-eo-batch-manager-replica:item-subset-locks-1";

        replicaLockRepository.add(key, "resource1");
        replicaLockRepository.add(key, "resource2");

        Set<String> actual = replicaLockRepository.findAll(key);
        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual).contains("resource1");
        assertThat(actual).contains("resource2");
    }

    @Test
    void deleteSuccess() {
        String key = "eric-eo-batch-manager:inventory:replicas:eric-eo-batch-manager-replica:item-subset-locks-2";

        assertTrue(replicaLockRepository.add(key, "resource1"));
        assertFalse(replicaLockRepository.add(key, "resource1"));
        Set<String> actual = replicaLockRepository.findAll(key);
        assertThat(actual).isNotNull();
        assertThat(actual).contains("resource1");

        replicaLockRepository.remove(key, "resource1");
        assertThat(replicaLockRepository.findAll(key)).isEmpty();
    }
    @Test
    void deleteNonExistentLock() {
        String key = "eric-eo-batch-manager:inventory:replicas:eric-eo-batch-manager-replica:item-subset-locks-3";

        replicaLockRepository.add(key, "resource1");
        replicaLockRepository.remove(key, "non-existent");

        assertThat(replicaLockRepository.findAll(key)).isNotEmpty();
        assertThat(replicaLockRepository.findAll(key)).contains("resource1");
    }

    @Test
    void getTimeSuccess() {

        long time1 = replicaLockRepository.getTimeMs();
        long time2 = replicaLockRepository.getTimeMs();

        assertThat(time2).isGreaterThanOrEqualTo(time1);
    }

}