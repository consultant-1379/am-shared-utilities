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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Requirement {
    private String name;
    private String node;
    @JsonProperty("node_requirement")
    private String nodeRequirement;
    private String capability;
    private Relationship relationship;

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

    public String getNodeRequirement() {
        return nodeRequirement;
    }

    public void setNodeRequirement(String nodeRequirement) {
        this.nodeRequirement = nodeRequirement;
    }

    public String getCapability() {
        return capability;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Requirement that = (Requirement) o;
        return Objects.equals(name, that.name) && Objects.equals(node, that.node) && Objects.equals(nodeRequirement, that.nodeRequirement)
                && Objects.equals(capability, that.capability) && Objects.equals(relationship, that.relationship);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, node, nodeRequirement, capability, relationship);
    }
}
