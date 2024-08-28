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
package com.ericsson.am.shared.vnfd.model;

import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeProperties {

    @Valid
    @NotNull(message = "DESCRIPTOR_ID_REQUIRED")
    @JsonProperty("descriptor_id")
    private NodePropertiesDetails descriptorId;

    @Valid
    @NotNull(message = "DESCRIPTOR_VERSION_REQUIRED")
    @JsonProperty("descriptor_version")
    private NodePropertiesDetails descriptorVersion;

    @Valid
    @NotNull(message = "PROVIDER_REQUIRED")
    @JsonProperty("provider")
    private NodePropertiesDetails provider;

    @Valid
    @NotNull(message = "PRODUCT_NAME_REQUIRED")
    @JsonProperty("product_name")
    private NodePropertiesDetails productName;

    @Valid
    @NotNull(message = "SOFTWARE_VERSION_REQUIRED")
    @JsonProperty("software_version")
    private NodePropertiesDetails softwareVersion;

    @Valid
    @NotNull(message = "VNFM_INFO_REQUIRED")
    @JsonProperty("vnfm_info")
    private NodePropertiesDetails vnfmInfo;

    @Valid
    @JsonProperty("flavour_id")
    private NodePropertiesDetails flavourId;

    private Map<String, Boolean> validFlavourIds;

    public NodePropertiesDetails getDescriptorId() {
        return descriptorId;
    }

    public void setDescriptorId(NodePropertiesDetails descriptorId) {
        this.descriptorId = descriptorId;
    }

    public NodePropertiesDetails getDescriptorVersion() {
        return descriptorVersion;
    }

    public void setDescriptorVersion(NodePropertiesDetails descriptorVersion) {
        this.descriptorVersion = descriptorVersion;
    }

    public NodePropertiesDetails getProvider() {
        return provider;
    }

    public void setProvider(NodePropertiesDetails provider) {
        this.provider = provider;
    }

    public NodePropertiesDetails getProductName() {
        return productName;
    }

    public void setProductName(NodePropertiesDetails productName) {
        this.productName = productName;
    }

    public NodePropertiesDetails getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(NodePropertiesDetails softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public NodePropertiesDetails getVnfmInfo() {
        return vnfmInfo;
    }

    public void setVnfmInfo(NodePropertiesDetails vnfmInfo) {
        this.vnfmInfo = vnfmInfo;
    }

    public NodePropertiesDetails getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(NodePropertiesDetails flavourId) {
        this.flavourId = flavourId;
    }

    public Map<String, Boolean> getValidFlavourIds() {
        return validFlavourIds;
    }

    public void setValidFlavourIds(Map<String, Boolean> validFlavourIds) {
        this.validFlavourIds = validFlavourIds;
    }
}
