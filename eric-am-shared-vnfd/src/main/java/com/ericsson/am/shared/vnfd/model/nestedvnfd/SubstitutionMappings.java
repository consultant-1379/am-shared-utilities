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

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SubstitutionMappings {

    private String nodeType;
    private Map<String, List<String>> requirements;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(final String nodeType) {
        this.nodeType = nodeType;
    }

    public Map<String, List<String>> getRequirements() {
        return requirements;
    }

    public void setRequirements(final Map<String, List<String>> requirements) {
        this.requirements = requirements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SubstitutionMappings that = (SubstitutionMappings) o;
        return Objects.equals(nodeType, that.nodeType) && Objects.equals(requirements, that.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, requirements);
    }
}
