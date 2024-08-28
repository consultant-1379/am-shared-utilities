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
package com.ericsson.am.shared.vnfd.model.policies;

import java.util.Map;
import java.util.Objects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScalingAspectProperties {

    @Valid
    @NotNull(message = "Minimum one scaling aspect should be provided")
    @JsonProperty("aspects")
    private Map<String, ScalingAspectDataType> allAspects;

    public Map<String, ScalingAspectDataType> getAllAspects() {
        return allAspects;
    }

    public void setAllAspects(Map<String, ScalingAspectDataType> allAspects) {
        this.allAspects = allAspects;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ScalingAspectProperties that = (ScalingAspectProperties) o;
        return Objects.equals(allAspects, that.allAspects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allAspects);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
