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
package utils.probes;

import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class PvcHealthCheckTest {

    private final PvcHealthCheck pvcHealthCheck = new PvcHealthCheck();
    private Path testDir;

    @AfterEach
    public void deleteTestDir(TestInfo info) throws IOException {
        if (info.getTags().contains("skipAfterEach")) {
            return;
        }
        Files.deleteIfExists(testDir);
    }

    @Test
    @Tag("skipAfterEach")
    void testPassingNullToMethod() {
        assertThrows(IllegalArgumentException.class, () ->
                pvcHealthCheck.checkPvcHealth(null, FsAccessMode.READ_ONLY));
    }

    @Test
    void testValidPermissionForReadOnlyPath() throws IOException {
        createTestDir(Set.of(OWNER_READ, OWNER_EXECUTE, GROUP_READ));
        assertTrue(pvcHealthCheck.checkPvcHealth(testDir, FsAccessMode.READ_ONLY));
    }

    @Test
    void testInvalidPermissionForReadOnlyPath() throws IOException {
        createTestDir(Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_WRITE));
        assertFalse(pvcHealthCheck.checkPvcHealth(testDir, FsAccessMode.READ_ONLY));
    }

    @Test
    @Tag("skipAfterEach")
    void testPathDoesNotExistsForReadOnly() {
        assertFalse(pvcHealthCheck.checkPvcHealth(Path.of("src/test/resources/test"), FsAccessMode.READ_ONLY));
    }

    @Test
    void testValidPermissionForReadWritePath() throws IOException {
        createTestDir(Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_WRITE));
        assertTrue(pvcHealthCheck.checkPvcHealth(testDir, FsAccessMode.READ_WRITE));
    }

    @Test
    void testInvalidPermissionForReadWritePath() throws IOException {
        createTestDir(Set.of(OWNER_READ, OWNER_EXECUTE, GROUP_READ));
        assertFalse(pvcHealthCheck.checkPvcHealth(testDir, FsAccessMode.READ_WRITE));
    }

    @Test
    @Tag("skipAfterEach")
    void testPathDoesNotExistsForReadWrite() {
        assertFalse(pvcHealthCheck.checkPvcHealth(Path.of("src/test/resources/test"), FsAccessMode.READ_WRITE));
    }

    void createTestDir(Set<PosixFilePermission> permissions) throws IOException {
        testDir = Files.createDirectory(Path.of("src/test/resources/test"));
        Files.setPosixFilePermissions(testDir, permissions);
    }

}