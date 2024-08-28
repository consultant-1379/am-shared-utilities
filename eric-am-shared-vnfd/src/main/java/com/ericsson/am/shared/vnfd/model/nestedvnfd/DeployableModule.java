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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeployableModule {
    private String nodeName;
    private String type;
    private List<String> associatedArtifacts;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeployableModule that = (DeployableModule) o;
        return Objects.equals(nodeName, that.nodeName) && Objects.equals(type, that.type) && Objects.equals(
                associatedArtifacts,
                that.associatedArtifacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeName, type, associatedArtifacts);
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public List<String> getAssociatedArtifacts() {
        return associatedArtifacts;
    }

    public void setAssociatedArtifacts(final List<String> associatedArtifacts) {
        this.associatedArtifacts = associatedArtifacts;
    }
}
