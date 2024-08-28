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

import com.fasterxml.jackson.annotation.JsonProperty;

public class VduProfile {

    private Integer minNumberOfInstances;
    private Integer maxNumberOfInstances;

    public VduProfile() {

    }

    public VduProfile(final Integer minNumberOfInstances, final Integer maxNumberOfInstances) {
        this.minNumberOfInstances = minNumberOfInstances;
        this.maxNumberOfInstances = maxNumberOfInstances;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduProfile that = (VduProfile) o;
        return Objects.equals(minNumberOfInstances, that.minNumberOfInstances) && Objects.equals(maxNumberOfInstances, that.maxNumberOfInstances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minNumberOfInstances, maxNumberOfInstances);
    }

    public Integer getMinNumberOfInstances() {
        return minNumberOfInstances;
    }

    @JsonProperty("min_number_of_instances")
    public void setMinNumberOfInstances(final Integer minNumberOfInstances) {
        this.minNumberOfInstances = minNumberOfInstances;
    }

    public Integer getMaxNumberOfInstances() {
        return maxNumberOfInstances;
    }

    @JsonProperty("max_number_of_instances")
    public void setMaxNumberOfInstances(final Integer maxNumberOfInstances) {
        this.maxNumberOfInstances = maxNumberOfInstances;
    }
}
