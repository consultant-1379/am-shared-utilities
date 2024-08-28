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

public class InstantiationLevelsProperties {

    @NotNull(message = "instantiation levels can't be null")
    @JsonProperty("levels")
    @Valid
    private Map<String, InstantiationLevelsDataInfo> instantiationLevelsDataInfo;

    @JsonProperty("default_level")
    private String defaultLevel;

    public Map<String, InstantiationLevelsDataInfo> getInstantiationLevelsDataInfo() {
        return this.instantiationLevelsDataInfo;
    }

    public void setInstantiationLevelsDataInfo(final Map<String, InstantiationLevelsDataInfo> instantiationLevelsDataInfo) {
        this.instantiationLevelsDataInfo = instantiationLevelsDataInfo;
    }

    public String getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(final String defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InstantiationLevelsProperties that = (InstantiationLevelsProperties) o;
        return Objects.equals(instantiationLevelsDataInfo, that.instantiationLevelsDataInfo) && Objects.equals(defaultLevel, that.defaultLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instantiationLevelsDataInfo, defaultLevel);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
