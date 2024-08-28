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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstantiationLevelsDataInfo {

    @JsonProperty("scale_info")
    @Valid
    private Map<String, InstantiationScaleLevels> scaleInfo;

    @NotBlank(message = "instantiation levels description must not be blank")
    @JsonProperty("description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Map<String, InstantiationScaleLevels> getScaleInfo() {
        return scaleInfo;
    }

    public void setScaleInfo(final Map<String, InstantiationScaleLevels> scaleInfo) {
        this.scaleInfo = scaleInfo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InstantiationLevelsDataInfo that = (InstantiationLevelsDataInfo) o;
        return Objects.equals(scaleInfo, that.scaleInfo) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaleInfo, description);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
