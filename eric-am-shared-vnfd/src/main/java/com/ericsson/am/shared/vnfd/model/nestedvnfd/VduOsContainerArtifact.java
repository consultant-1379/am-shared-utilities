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
public class VduOsContainerArtifact {
    private String nodeName;
    private String type;
    private String file;
    private String name;
    private String version;
    @JsonProperty(value = "container_format")
    private String containerFormat;
    private Checksum checksum;
    private String size;
    @JsonProperty(value = "disk_format")
    private String diskFormat;
    @JsonProperty(value = "min_disk")
    private String minDisk;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduOsContainerArtifact that = (VduOsContainerArtifact) o;
        return Objects.equals(nodeName, that.nodeName) && Objects.equals(type, that.type) && Objects.equals(file, that.file)
                && Objects.equals(name, that.name)
                && Objects.equals(version, that.version) && Objects.equals(containerFormat, that.containerFormat)
                && Objects.equals(checksum, that.checksum) && Objects.equals(size, that.size) && Objects.equals(diskFormat, that.diskFormat)
                && Objects.equals(minDisk, that.minDisk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeName, type, file, name, version, containerFormat, checksum, size, diskFormat, minDisk);
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getContainerFormat() {
        return containerFormat;
    }

    public void setContainerFormat(final String containerFormat) {
        this.containerFormat = containerFormat;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(final Checksum checksum) {
        this.checksum = checksum;
    }

    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
        this.size = size;
    }

    public String getDiskFormat() {
        return diskFormat;
    }

    public void setDiskFormat(final String diskFormat) {
        this.diskFormat = diskFormat;
    }

    public String getMinDisk() {
        return minDisk;
    }

    public void setMinDisk(final String minDisk) {
        this.minDisk = minDisk;
    }
}
