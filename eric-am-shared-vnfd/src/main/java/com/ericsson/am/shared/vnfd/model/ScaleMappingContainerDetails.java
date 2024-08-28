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
package com.ericsson.am.shared.vnfd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScaleMappingContainerDetails {

    @JsonProperty("Requested_CPU_Resources")
    private String requestedCpuResources;

    @JsonProperty("CPU_Resource_Limit")
    private String cpuResourceLimit;

    @JsonProperty("Requested_Memory_Resources")
    private String requestedMemoryResources;

    @JsonProperty("Memory_Resource_Limit")
    private String memoryResourceLimit;

    @JsonProperty("Deployment_Allowed")
    private String deploymentAllowed;

    public String getRequestedCpuResources() {
        return requestedCpuResources;
    }

    public void setRequestedCpuResources(String requestedCpuResources) {
        this.requestedCpuResources = requestedCpuResources;
    }

    public String getCpuResourceLimit() {
        return cpuResourceLimit;
    }

    public void setCpuResourceLimit(String cpuResourceLimit) {
        this.cpuResourceLimit = cpuResourceLimit;
    }

    public String getRequestedMemoryResources() {
        return requestedMemoryResources;
    }

    public void setRequestedMemoryResources(String requestedMemoryResources) {
        this.requestedMemoryResources = requestedMemoryResources;
    }

    public String getMemoryResourceLimit() {
        return memoryResourceLimit;
    }

    public void setMemoryResourceLimit(String memoryResourceLimit) {
        this.memoryResourceLimit = memoryResourceLimit;
    }

    public String getDeploymentAllowed() {
        return deploymentAllowed;
    }

    public void setDeploymentAllowed(String deploymentAllowed) {
        this.deploymentAllowed = deploymentAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScaleMappingContainerDetails that = (ScaleMappingContainerDetails) o;
        return Objects.equals(requestedCpuResources, that.requestedCpuResources) &&
                Objects.equals(cpuResourceLimit, that.cpuResourceLimit) &&
                Objects.equals(requestedMemoryResources, that.requestedMemoryResources) &&
                Objects.equals(memoryResourceLimit, that.memoryResourceLimit) &&
                Objects.equals(deploymentAllowed, that.deploymentAllowed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedCpuResources, cpuResourceLimit, requestedMemoryResources, memoryResourceLimit,
                deploymentAllowed);
    }
}
