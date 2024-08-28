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
package com.ericsson.am.shared.vnfd.model.policies;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class VnfPackageChangeSelector {

    @JsonProperty("source_descriptor_id")
    private String sourceDescriptorId;

    @JsonProperty("destination_descriptor_id")
    private String destinationDescriptorId;

    @JsonProperty("source_software_version")
    private String sourceSoftwareVersion;

    @JsonProperty("destination_software_version")
    private String destinationSoftwareVersion;

    @NotNull(message = "source_flavour_id can't be null")
    @JsonProperty("source_flavour_id")
    private String sourceFlavourId;

    public String getSourceDescriptorId() {
        return sourceDescriptorId;
    }

    public void setSourceDescriptorId(String sourceDescriptorId) {
        this.sourceDescriptorId = sourceDescriptorId;
    }

    public String getDestinationDescriptorId() {
        return destinationDescriptorId;
    }

    public void setDestinationDescriptorId(String destinationDescriptorId) {
        this.destinationDescriptorId = destinationDescriptorId;
    }

    public String getSourceSoftwareVersion() {
        return sourceSoftwareVersion;
    }

    public void setSourceSoftwareVersion(String sourceSoftwareVersion) {
        this.sourceSoftwareVersion = sourceSoftwareVersion;
    }

    public String getDestinationSoftwareVersion() {
        return destinationSoftwareVersion;
    }

    public void setDestinationSoftwareVersion(String destinationSoftwareVersion) {
        this.destinationSoftwareVersion = destinationSoftwareVersion;
    }

    public String getSourceFlavourId() {
        return sourceFlavourId;
    }

    public void setSourceFlavourId(String sourceFlavourId) {
        this.sourceFlavourId = sourceFlavourId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VnfPackageChangeSelector that = (VnfPackageChangeSelector) o;
        return Objects.equals(sourceDescriptorId, that.sourceDescriptorId) &&
                Objects.equals(destinationDescriptorId, that.destinationDescriptorId) &&
                Objects.equals(sourceSoftwareVersion, that.sourceSoftwareVersion) &&
                Objects.equals(destinationSoftwareVersion, that.destinationSoftwareVersion) &&
                Objects.equals(sourceFlavourId, that.sourceFlavourId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceDescriptorId, destinationDescriptorId, sourceSoftwareVersion, destinationSoftwareVersion,
                sourceFlavourId);
    }
}
