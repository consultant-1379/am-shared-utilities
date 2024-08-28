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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScalingAspectDataType {

    @NotBlank(message = "aspect name is mandatory")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "description is mandatory")
    @JsonProperty("description")
    private String description;

    @NotNull(message = "max scale level should be provided")
    @Min(value = 0, message = "max scale level can be only positive")
    @JsonProperty("max_scale_level")
    private Integer maxScaleLevel;

    @JsonProperty("step_deltas")
    private List<String> stepDeltas;

    @JsonProperty("allScalingAspectDelta")
    private Map<String, ScalingAspectDeltas> allScalingAspectDelta;

    @JsonProperty("enabled")
    private boolean enabled = true;

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

    public Integer getMaxScaleLevel() {
        return maxScaleLevel;
    }

    public void setMaxScaleLevel(Integer maxScaleLevel) {
        this.maxScaleLevel = maxScaleLevel;
    }

    public List<String> getStepDeltas() {
        return stepDeltas;
    }

    public void setStepDeltas(List<String> stepDeltas) {
        this.stepDeltas = stepDeltas;
    }

    public Map<String, ScalingAspectDeltas> getAllScalingAspectDelta() {
        return allScalingAspectDelta;
    }

    public void setAllScalingAspectDelta(Map<String, ScalingAspectDeltas> allScalingAspectDelta) {
        this.allScalingAspectDelta = allScalingAspectDelta;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ScalingAspectDataType that = (ScalingAspectDataType) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description)
                && Objects.equals(maxScaleLevel, that.maxScaleLevel) && Objects.equals(stepDeltas, that.stepDeltas)
                && Objects.equals(allScalingAspectDelta, that.allScalingAspectDelta)
                && enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, maxScaleLevel, stepDeltas, allScalingAspectDelta, enabled);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
