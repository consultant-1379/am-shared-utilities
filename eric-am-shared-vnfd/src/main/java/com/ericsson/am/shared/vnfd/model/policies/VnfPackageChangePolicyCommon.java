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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangePolicyCommon {

    @NotBlank(message = "type is mandatory for VnfPackageChangePolicy")
    @Pattern(regexp = "(tosca|ericsson).policies.nfv.VnfPackageChange")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "Properties can't be null")
    @Valid
    @JsonProperty("properties")
    private VnfPackageChangeProperty properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VnfPackageChangeProperty getProperties() {
        return properties;
    }

    public void setProperties(VnfPackageChangeProperty properties) {
        this.properties = properties;
    }
}
