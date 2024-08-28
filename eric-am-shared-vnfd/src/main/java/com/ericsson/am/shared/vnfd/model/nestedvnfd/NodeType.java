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

import com.ericsson.am.shared.vnfd.model.ArtifactsPropertiesDetail;
import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import com.ericsson.am.shared.vnfd.model.NodeProperties;

import java.util.List;
import java.util.Map;

public class NodeType {

    private String type;
    private NodeProperties nodeProperties;
    private List<ArtifactsPropertiesDetail> artifacts;
    private List<VnfmLcmInterface> interfaces;
    private Map<String, InterfaceTypeImpl> customInterface;

    public Map<String, InterfaceTypeImpl> getCustomInterface() {
        return customInterface;
    }

    public void setCustomInterface(Map<String, InterfaceTypeImpl> customInterface) {
        this.customInterface = customInterface;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ArtifactsPropertiesDetail> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<ArtifactsPropertiesDetail> artifacts) {
        this.artifacts = artifacts;
    }

    public List<VnfmLcmInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<VnfmLcmInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public NodeProperties getNodeProperties() {
        return nodeProperties;
    }

    public void setNodeProperties(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

}
