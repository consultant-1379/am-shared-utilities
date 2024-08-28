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
public class Topology {
    private List<Input> inputs;
    @JsonProperty("substitution_mappings")
    private SubstitutionMappings substitutionMappings;
    @JsonProperty("node_templates")
    private List<NodeTemplate> nodeTemplates;
    private List<Policy> policies;

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public SubstitutionMappings getSubstitutionMappings() {
        return substitutionMappings;
    }

    public void setSubstitutionMappings(SubstitutionMappings substitutionMappings) {
        this.substitutionMappings = substitutionMappings;
    }

    public List<NodeTemplate> getNodeTemplates() {
        return nodeTemplates;
    }

    public void setNodeTemplates(List<NodeTemplate> nodeTemplates) {
        this.nodeTemplates = nodeTemplates;
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Topology topology = (Topology) o;
        return Objects.equals(inputs, topology.inputs) && Objects.equals(substitutionMappings, topology.substitutionMappings)
                && Objects.equals(nodeTemplates, topology.nodeTemplates) && Objects.equals(policies, topology.policies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, substitutionMappings, nodeTemplates, policies);
    }
}
