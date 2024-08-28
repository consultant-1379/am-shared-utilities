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

import static utils.probes.FsAccessMode.READ_WRITE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvcHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(PvcHealthCheck.class);

    /**
     * Verifies path compliance to access mode provided.
     * In case of {@code READ_WRITE} mode, it also creates and deletes tmp file. <br/>
     * See {@code FsAccessMode}, {@code ReadOnlyAccessCheck}, {@code ReadWriteAccessCheck}
     * @param mountPath
     * @param mode Access mode according to which path will be verified
     * @return {@code True} only if provided path access constraints are compliant with access mode provided
     */
    public boolean checkPvcHealth(Path mountPath, FsAccessMode mode) {
        if (Objects.isNull(mountPath)) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        boolean isValid = mode.checkAccess(mountPath);
        Path tmpFile = null;
        try {
            if (isValid && READ_WRITE.equals(mode)) {
                tmpFile = Files.createTempFile(mountPath, "pvc-check", null);
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred while creating tmp file in '{}': {}", mountPath, e);
            return false;
        } finally {
            try {
                if (Objects.nonNull(tmpFile)) {
                    Files.deleteIfExists(tmpFile);
                }
            } catch (IOException e) {
                LOGGER.error("Error occurred while deleting tmp file '{}': {}", tmpFile, e);
                isValid = false;
            }
        }

        return isValid;
    }
}
