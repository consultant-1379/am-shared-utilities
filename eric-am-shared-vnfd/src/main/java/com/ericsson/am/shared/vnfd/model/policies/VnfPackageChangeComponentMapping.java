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
import jakarta.validation.constraints.Pattern;

public class VnfPackageChangeComponentMapping {

    @NotNull(message = "component_type can't be null")
    @JsonProperty("component_type")
    @Pattern(regexp = "vdu|cp|virtual_link|virtual_storage|deployment_flavour|instantiation_level|scaling_aspect",
            message = "component_type only support's vdu, cp, virtual_link, virtual_storage, deployment_flavour, " +
                    "instantiation_level and scaling_aspect value")
    private String componentType;

    @NotNull(message = "source_id can't be null")
    @JsonProperty("source_id")
    private String sourceId;

    @NotNull(message = "destination_id can't be null")
    @JsonProperty("destination_id")
    private String destinationId;

    @JsonProperty("description")
    private String description;

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
