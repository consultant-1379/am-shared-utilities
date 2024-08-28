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
package com.ericsson.am.shared.vnfd.model.servicemodel;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Capability {
    private String name;
    private String node;
    @JsonProperty("node_capability")
    private String nodeCapability;
    @JsonProperty("capabilitytype")
    private String capabilityType;
    @JsonProperty("capabilityproperties")
    private List<Capabilityproperty> capabilityProperties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getNodeCapability() {
        return nodeCapability;
    }

    public void setNodeCapability(String nodeCapability) {
        this.nodeCapability = nodeCapability;
    }

    public String getCapabilityType() {
        return capabilityType;
    }

    public void setCapabilityType(String capabilityType) {
        this.capabilityType = capabilityType;
    }

    public List<Capabilityproperty> getCapabilityProperties() {
        return capabilityProperties;
    }

    public void setCapabilityProperties(List<Capabilityproperty> capabilityProperties) {
        this.capabilityProperties = capabilityProperties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Capability that = (Capability) o;
        return Objects.equals(name, that.name) && Objects.equals(node, that.node) && Objects.equals(nodeCapability, that.nodeCapability)
                && Objects.equals(capabilityType, that.capabilityType) && Objects.equals(capabilityProperties, that.capabilityProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, node, nodeCapability, capabilityType, capabilityProperties);
    }
}
