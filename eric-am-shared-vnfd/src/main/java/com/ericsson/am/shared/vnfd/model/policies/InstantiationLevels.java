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

public class InstantiationLevels {

    @NotBlank(message = "type is mandatory for Instantiation level")
    @Pattern(regexp = "tosca.policies.nfv.InstantiationLevels")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "properties can't be null for instantiation levels")
    @Valid
    @JsonProperty("properties")
    private InstantiationLevelsProperties properties;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public InstantiationLevelsProperties getProperties() {
        return properties;
    }

    public void setProperties(final InstantiationLevelsProperties properties) {
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
        final InstantiationLevels that = (InstantiationLevels) o;
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
