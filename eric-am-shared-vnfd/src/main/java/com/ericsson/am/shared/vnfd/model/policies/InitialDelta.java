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

import java.util.Arrays;
import java.util.Objects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InitialDelta {

    @NotBlank(message = "type is mandatory for initial delta")
    @Pattern(regexp = "tosca.policies.nfv.VduInitialDelta")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "properties can't be null for initial deltas")
    @Valid
    @JsonProperty("properties")
    private VduInitialDeltaProperties properties;

    @NotNull(message = "target can't be null")
    @Size(min = 1)
    @JsonProperty("targets")
    private String[] targets;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VduInitialDeltaProperties getProperties() {
        return properties;
    }

    public void setProperties(VduInitialDeltaProperties properties) {
        this.properties = properties;
    }

    public String[] getTargets() {
        return targets;
    }

    public void setTargets(String[] targets) {
        this.targets = targets;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InitialDelta that = (InitialDelta) o;
        return Objects.equals(type, that.type) && Objects.equals(properties, that.properties) && Arrays.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, properties);
        result = 31 * result + Arrays.hashCode(targets);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
