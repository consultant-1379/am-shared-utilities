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
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VduInitialDeltaProperties {

    @NotNull(message = "vdu initial delta can't be null")
    @JsonProperty("initial_delta")
    @Valid
    private VduLevelDataType initialDelta;

    public VduLevelDataType getInitialDelta() {
        return initialDelta;
    }

    public void setInitialDelta(VduLevelDataType initialDelta) {
        this.initialDelta = initialDelta;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduInitialDeltaProperties that = (VduInitialDeltaProperties) o;
        return Objects.equals(initialDelta, that.initialDelta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialDelta);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
