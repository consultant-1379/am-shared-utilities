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
public class Targets {
    @JsonProperty("node_templates")
    private List<String> nodeTemplates;

    @JsonProperty("group_templates")
    private List<Object> groupTemplates;

    public List<String> getNodeTemplates() {
        return nodeTemplates;
    }

    public void setNodeTemplates(final List<String> nodeTemplates) {
        this.nodeTemplates = nodeTemplates;
    }

    public List<Object> getGroupTemplates() {
        return groupTemplates;
    }

    public void setGroupTemplates(final List<Object> groupTemplates) {
        this.groupTemplates = groupTemplates;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Targets targets = (Targets) o;
        return Objects.equals(nodeTemplates, targets.nodeTemplates) && Objects.equals(groupTemplates, targets.groupTemplates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeTemplates, groupTemplates);
    }
}
