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
public class VduVirtualBlockStorage {
    private String name;
    private String type;
    @JsonProperty(value = "virtual_block_storage_data")
    private VduVirtualBlockStorageData vduVirtualBlockStorageData;
    @JsonProperty(value = "per_vnfc_instance")
    private boolean perVnfcInstance;

    public VduVirtualBlockStorage() {

    }

    public VduVirtualBlockStorage(final String name,
                                  final String type,
                                  final VduVirtualBlockStorageData vduVirtualBlockStorageData, final boolean perVnfcInstance) {
        this.name = name;
        this.type = type;
        this.vduVirtualBlockStorageData = vduVirtualBlockStorageData;
        this.perVnfcInstance = perVnfcInstance;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduVirtualBlockStorage that = (VduVirtualBlockStorage) o;
        return perVnfcInstance == that.perVnfcInstance && Objects.equals(name, that.name) && Objects.equals(type, that.type)
                && Objects.equals(vduVirtualBlockStorageData, that.vduVirtualBlockStorageData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, vduVirtualBlockStorageData, perVnfcInstance);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public VduVirtualBlockStorageData getVduVirtualBlockStorageData() {
        return vduVirtualBlockStorageData;
    }

    public void setVduVirtualBlockStorageData(final VduVirtualBlockStorageData vduVirtualBlockStorageData) {
        this.vduVirtualBlockStorageData = vduVirtualBlockStorageData;
    }

    public boolean isPerVnfcInstance() {
        return perVnfcInstance;
    }

    public void setPerVnfcInstance(final boolean perVnfcInstance) {
        this.perVnfcInstance = perVnfcInstance;
    }
}
