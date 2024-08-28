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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VduCompute {

    private String name;
    private String description;
    private VduProfile vduProfile;
    private String vduComputeKey;

    public VduCompute() {

    }

    public VduCompute(final String name, final String description, final VduProfile vduProfile, final String vduComputeKey) {
        this.name = name;
        this.description = description;
        this.vduProfile = vduProfile;
        this.vduComputeKey = vduComputeKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduCompute that = (VduCompute) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description)
                && Objects.equals(vduProfile, that.vduProfile) && Objects.equals(vduComputeKey, that.vduComputeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, vduProfile, vduComputeKey);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VduProfile getVduProfile() {
        return vduProfile;
    }

    @JsonProperty("vdu_profile")
    public void setVduProfile(final VduProfile vduProfile) {
        this.vduProfile = vduProfile;
    }

    public String getVduComputeKey() {
        return vduComputeKey;
    }

    public void setVduComputeKey(final String vduComputeKey) {
        this.vduComputeKey = vduComputeKey;
    }
}
