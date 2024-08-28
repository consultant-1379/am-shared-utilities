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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScaleMapping {

    @JsonProperty("Scaling-Parameter-Name")
    private String scalingParameterName;

    @JsonProperty("Mciop-Name")
    private String mciopName;

    @JsonProperty("Auto-Scaling-MinReplicas-Name")
    private String autoScalingMinReplicasName;

    @JsonProperty("Auto-Scaling-MaxReplicas-Name")
    private String autoScalingMaxReplicasName;

    @JsonProperty("Auto-Scaling-Enabled")
    private String autoScalingEnabled;

    @JsonProperty("Storage")
    private Map<String, String> storages = new HashMap<>();

    private Map<String, ScaleMappingContainerDetails> containers = new HashMap<>();

    public String getScalingParameterName() {
        return scalingParameterName;
    }

    public void setScalingParameterName(String scalingParameterName) {
        this.scalingParameterName = scalingParameterName;
    }

    public String getMciopName() {
        return mciopName;
    }

    public void setMciopName(String mciopName) {
        this.mciopName = mciopName;
    }

    public String getAutoScalingMinReplicasName() {
        return autoScalingMinReplicasName;
    }

    public void setAutoScalingMinReplicasName(String autoScalingMinReplicasName) {
        this.autoScalingMinReplicasName = autoScalingMinReplicasName;
    }

    public String getAutoScalingMaxReplicasName() {
        return autoScalingMaxReplicasName;
    }

    public void setAutoScalingMaxReplicasName(String autoScalingMaxReplicasName) {
        this.autoScalingMaxReplicasName = autoScalingMaxReplicasName;
    }

    public String getAutoScalingEnabled() {
        return autoScalingEnabled;
    }

    public void setAutoScalingEnabled(String autoScalingEnabled) {
        this.autoScalingEnabled = autoScalingEnabled;
    }

    public Map<String, String> getStorages() {
        return storages;
    }

    public void setStorages(final Map<String, String> storages) {
        this.storages = storages;
    }

    public Map<String, ScaleMappingContainerDetails> getContainers() {
        return containers;
    }

    public void setContainers(final Map<String, ScaleMappingContainerDetails> containers) {
        this.containers = containers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScaleMapping that = (ScaleMapping) o;
        return Objects.equals(scalingParameterName, that.scalingParameterName) &&
                Objects.equals(mciopName, that.mciopName) &&
                Objects.equals(autoScalingMinReplicasName, that.autoScalingMinReplicasName) &&
                Objects.equals(autoScalingMaxReplicasName, that.autoScalingMaxReplicasName) &&
                Objects.equals(autoScalingEnabled, that.autoScalingEnabled) &&
                Objects.equals(storages, that.storages) &&
                Objects.equals(containers, that.containers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scalingParameterName, mciopName, autoScalingMinReplicasName, autoScalingMaxReplicasName,
                autoScalingEnabled, storages, containers);
    }
}
