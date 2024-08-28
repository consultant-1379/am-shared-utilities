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

import java.util.Map;
import java.util.Objects;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.policies.Policies;

public class TopologyTemplate {

    private SubstitutionMappings substitutionMappings;
    private NodeTemplate nodeTemplate;
    private Policies policies;
    private Map<String, DataType> inputs;

    public Map<String, DataType> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, DataType> inputs) {
        this.inputs = inputs;
    }

    public NodeTemplate getNodeTemplate() {
        return nodeTemplate;
    }

    public void setNodeTemplate(NodeTemplate nodeTemplate) {
        this.nodeTemplate = nodeTemplate;
    }

    public Policies getPolicies() {
        return policies;
    }

    public void setPolicies(Policies policies) {
        this.policies = policies;
    }

    public SubstitutionMappings getSubstitutionMappings() {
        return substitutionMappings;
    }

    public void setSubstitutionMappings(final SubstitutionMappings substitutionMappings) {
        this.substitutionMappings = substitutionMappings;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TopologyTemplate that = (TopologyTemplate) o;
        return Objects.equals(substitutionMappings, that.substitutionMappings) && Objects.equals(nodeTemplate, that.nodeTemplate)
                && Objects.equals(policies, that.policies) && Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(substitutionMappings, nodeTemplate, policies, inputs);
    }
}
