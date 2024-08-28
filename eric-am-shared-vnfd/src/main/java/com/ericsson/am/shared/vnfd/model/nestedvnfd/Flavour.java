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

import java.util.Objects;

public class Flavour {

    private String id;
    private String nodeType;
    private TopologyTemplate topologyTemplate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TopologyTemplate getTopologyTemplate() {
        return topologyTemplate;
    }

    public void setTopologyTemplate(TopologyTemplate topologyTemplate) {
        this.topologyTemplate = topologyTemplate;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(final String nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Flavour flavour = (Flavour) o;
        return Objects.equals(id, flavour.id) && Objects.equals(nodeType, flavour.nodeType)
                && Objects.equals(topologyTemplate, flavour.topologyTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nodeType, topologyTemplate);
    }
}
