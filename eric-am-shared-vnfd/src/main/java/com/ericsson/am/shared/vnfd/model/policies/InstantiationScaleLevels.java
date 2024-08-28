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

import java.util.Objects;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstantiationScaleLevels {

    @NotNull(message = "number of instances can't be null")
    @Min(value = 0, message = "number of instance can be only positive")
    @JsonProperty("scale_level")
    private Integer scaleLevel;

    public Integer getScaleLevel() {
        return scaleLevel;
    }

    public void setScaleLevel(final Integer scaleLevel) {
        this.scaleLevel = scaleLevel;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InstantiationScaleLevels that = (InstantiationScaleLevels) o;
        return Objects.equals(scaleLevel, that.scaleLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaleLevel);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
