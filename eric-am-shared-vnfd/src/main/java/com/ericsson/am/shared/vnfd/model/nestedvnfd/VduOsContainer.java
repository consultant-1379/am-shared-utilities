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
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VduOsContainer {
    private String type;
    private String nodeName;
    private String name;
    private String description;
    @JsonProperty(value = "requested_cpu_resources")
    private String requestedCpuResources;
    @JsonProperty(value = "cpu_resource_limit")
    private String cpuResourceLimit;
    @JsonProperty(value = "requested_memory_resources")
    private String requestedMemoryResources;
    @JsonProperty(value = "memory_resource_limit")
    private String memoryResourceLimit;
    private List<VduOsContainerArtifact> artifacts;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduOsContainer that = (VduOsContainer) o;
        return Objects.equals(type, that.type) && Objects.equals(nodeName, that.nodeName) && Objects.equals(name, that.name)
                && Objects.equals(description, that.description) && Objects.equals(requestedCpuResources, that.requestedCpuResources)
                && Objects.equals(cpuResourceLimit, that.cpuResourceLimit) && Objects.equals(requestedMemoryResources, that.requestedMemoryResources)
                && Objects.equals(memoryResourceLimit, that.memoryResourceLimit) && Objects.equals(artifacts, that.artifacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type,
                            nodeName,
                            name,
                            description,
                            requestedCpuResources,
                            cpuResourceLimit,
                            requestedMemoryResources,
                            memoryResourceLimit,
                            artifacts);
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getRequestedCpuResources() {
        return requestedCpuResources;
    }

    public void setRequestedCpuResources(final String requestedCpuResources) {
        this.requestedCpuResources = requestedCpuResources;
    }

    public String getCpuResourceLimit() {
        return cpuResourceLimit;
    }

    public void setCpuResourceLimit(final String cpuResourceLimit) {
        this.cpuResourceLimit = cpuResourceLimit;
    }

    public String getRequestedMemoryResources() {
        return requestedMemoryResources;
    }

    public void setRequestedMemoryResources(final String requestedMemoryResources) {
        this.requestedMemoryResources = requestedMemoryResources;
    }

    public String getMemoryResourceLimit() {
        return memoryResourceLimit;
    }

    public void setMemoryResourceLimit(final String memoryResourceLimit) {
        this.memoryResourceLimit = memoryResourceLimit;
    }

    public List<VduOsContainerArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(final List<VduOsContainerArtifact> artifacts) {
        this.artifacts = artifacts;
    }
}
