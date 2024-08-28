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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VduVirtualBlockStorageData {
    @JsonProperty(value = "size_of_storage")
    private String sizeOfStorage;
    @JsonProperty(value = "rdma_enabled")
    private boolean rdmaEnabled;

    public VduVirtualBlockStorageData() {

    }

    public VduVirtualBlockStorageData(final String sizeOfStorage, final boolean rdmaEnabled) {
        this.sizeOfStorage = sizeOfStorage;
        this.rdmaEnabled = rdmaEnabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduVirtualBlockStorageData that = (VduVirtualBlockStorageData) o;
        return rdmaEnabled == that.rdmaEnabled && Objects.equals(sizeOfStorage, that.sizeOfStorage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sizeOfStorage, rdmaEnabled);
    }

    public String getSizeOfStorage() {
        return sizeOfStorage;
    }

    public void setSizeOfStorage(final String sizeOfStorage) {
        this.sizeOfStorage = sizeOfStorage;
    }

    public boolean isRdmaEnabled() {
        return rdmaEnabled;
    }

    public void setRdmaEnabled(final boolean rdmaEnabled) {
        this.rdmaEnabled = rdmaEnabled;
    }
}
