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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceModel {
    private String id;
    private String creTime;
    private String name;
    private String version;
    private String description;
    @JsonProperty("mainSTfile")
    private String mainSTFile;
    @JsonProperty("dsl_version")
    private String toscaDefVersion;
    private Topology topology;
    private ServiceModelMetadata metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreTime() {
        return creTime;
    }

    public void setCreTime(String creTime) {
        this.creTime = creTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainSTFile() {
        return mainSTFile;
    }

    public void setMainSTFile(String mainSTFile) {
        this.mainSTFile = mainSTFile;
    }

    public String getToscaDefVersion() {
        return toscaDefVersion;
    }

    public void setToscaDefVersion(String toscaDefVersion) {
        this.toscaDefVersion = toscaDefVersion;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public ServiceModelMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final ServiceModelMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServiceModel that = (ServiceModel) o;
        return Objects.equals(id, that.id) && Objects.equals(creTime, that.creTime) && Objects.equals(name, that.name)
                && Objects.equals(version, that.version) && Objects.equals(description, that.description)
                && Objects.equals(mainSTFile, that.mainSTFile) && Objects.equals(toscaDefVersion, that.toscaDefVersion)
                && Objects.equals(topology, that.topology) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creTime, name, version, description, mainSTFile, toscaDefVersion, topology, metadata);
    }
}
