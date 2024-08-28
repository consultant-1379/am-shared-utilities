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

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isWritable;

import java.nio.file.Path;
import java.util.Objects;

public class ReadOnlyAccessCheck implements AccessCheck {

    /**
     * @param path
     * @return Return {@code True} only if path exists, readable and not writable
     */
    public boolean check(Path path) {
        if (Objects.isNull(path)) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return exists(path) && isReadable(path) && !isWritable(path);
    }

}
