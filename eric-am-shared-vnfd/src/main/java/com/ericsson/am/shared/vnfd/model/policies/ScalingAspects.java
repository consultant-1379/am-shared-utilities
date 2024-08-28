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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScalingAspects {

    @NotBlank(message = "type is mandatory for scaling aspect")
    @Pattern(regexp = "tosca.policies.nfv.ScalingAspects")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "ScalingAspect Properties can't be null")
    @Valid
    @JsonProperty("properties")
    private ScalingAspectProperties properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ScalingAspectProperties getProperties() {
        return properties;
    }

    public void setProperties(ScalingAspectProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ScalingAspects that = (ScalingAspects) o;
        return Objects.equals(type, that.type) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
