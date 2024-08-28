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
package com.ericsson.am.shared.vnfd.model.nestedvnfd;

import java.util.Objects;

public class Checksum {
    private String algorithm;
    private String hash;

    public Checksum() {

    }

    public Checksum(final String algorithm, final String hash) {
        this.algorithm = algorithm;
        this.hash = hash;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Checksum checksum = (Checksum) o;
        return Objects.equals(algorithm, checksum.algorithm) && Objects.equals(hash, checksum.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, hash);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }
}
