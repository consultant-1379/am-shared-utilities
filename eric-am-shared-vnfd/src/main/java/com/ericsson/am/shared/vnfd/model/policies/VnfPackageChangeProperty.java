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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangeProperty {

    @NotNull(message = "modification_qualifier can't be null")
    @JsonProperty("modification_qualifier")
    @Pattern(regexp = "up|down", message = "modification_qualifier only supports 'up' and 'down' value")
    private String modificationQualifier;

    @JsonProperty("additional_modification_description")
    private String additionalModificationDescription;

    @NotNull(message = "destination_flavour_id can't be null")
    @JsonProperty("destination_flavour_id")
    private String destinationFlavourId;

    @JsonIgnore
    @JsonProperty("selector")
    @Valid
    private List<VnfPackageChangeSelector> vnfPackageChangeSelectors;

    @JsonProperty("component_mappings")
    @Valid
    private VnfPackageChangeComponentMapping[] componentMappings;

    public String getModificationQualifier() {
        return modificationQualifier;
    }

    public void setModificationQualifier(String modificationQualifier) {
        this.modificationQualifier = modificationQualifier;
    }

    public String getAdditionalModificationDescription() {
        return additionalModificationDescription;
    }

    public void setAdditionalModificationDescription(String additionalModificationDescription) {
        this.additionalModificationDescription = additionalModificationDescription;
    }

    public String getDestinationFlavourId() {
        return destinationFlavourId;
    }

    public void setDestinationFlavourId(String destinationFlavourId) {
        this.destinationFlavourId = destinationFlavourId;
    }

    public VnfPackageChangeComponentMapping[] getComponentMappings() {
        return componentMappings;
    }

    public void setComponentMappings(VnfPackageChangeComponentMapping[] componentMappings) {
        this.componentMappings = componentMappings;
    }

    public List<VnfPackageChangeSelector> getVnfPackageChangeSelectors() {
        return vnfPackageChangeSelectors;
    }

    public void setVnfPackageChangeSelectors(final List<VnfPackageChangeSelector> vnfPackageChangeSelectors) {
        this.vnfPackageChangeSelectors = vnfPackageChangeSelectors;
    }

}
