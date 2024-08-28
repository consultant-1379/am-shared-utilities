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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VduScalingAspectDeltasProperties {

    @NotBlank(message = "aspect name can't be blank for Vdu Scaling aspect delta")
    @JsonProperty("aspect")
    private String aspect;

    @NotNull(message = "deltas are mandatory can't be null")
    @Valid
    @JsonProperty("deltas")
    private Map<String, VduLevelDataType> deltas;

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public Map<String, VduLevelDataType> getDeltas() {
        return deltas;
    }

    public void setDeltas(Map<String, VduLevelDataType> deltas) {
        this.deltas = deltas;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduScalingAspectDeltasProperties that = (VduScalingAspectDeltasProperties) o;
        return Objects.equals(aspect, that.aspect) && Objects.equals(deltas, that.deltas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspect, deltas);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
