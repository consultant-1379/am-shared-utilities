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
public class NodeTemplate {
    private String name;
    private String description;
    @JsonProperty("nodetype")
    private String nodeType;
    @JsonProperty("parent_type")
    private String parentType;
    @JsonProperty("nodeproperties")
    private List<NodeProperty> nodeProperties;
    @JsonProperty("nodeattributes")
    private List<Nodeattribute> nodeAttributes;
    private List<Artifact> artifacts;
    private List<Capability> capabilities;
    private List<Requirement> requirement;
    private List<Interface> interfaces;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public List<NodeProperty> getNodeProperties() {
        return nodeProperties;
    }

    public void setNodeProperties(List<NodeProperty> nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    public List<Nodeattribute> getNodeAttributes() {
        return nodeAttributes;
    }

    public void setNodeAttributes(List<Nodeattribute> nodeAttributes) {
        this.nodeAttributes = nodeAttributes;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public List<Requirement> getRequirement() {
        return requirement;
    }

    public void setRequirement(List<Requirement> requirement) {
        this.requirement = requirement;
    }

    public List<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Interface> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeTemplate that = (NodeTemplate) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(nodeType, that.nodeType)
                && Objects.equals(parentType, that.parentType) && Objects.equals(nodeProperties, that.nodeProperties)
                && Objects.equals(nodeAttributes, that.nodeAttributes) && Objects.equals(artifacts, that.artifacts)
                && Objects.equals(capabilities, that.capabilities) && Objects.equals(requirement, that.requirement)
                && Objects.equals(interfaces, that.interfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,
                            description,
                            nodeType,
                            parentType,
                            nodeProperties,
                            nodeAttributes,
                            artifacts,
                            capabilities,
                            requirement,
                            interfaces);
    }
}
