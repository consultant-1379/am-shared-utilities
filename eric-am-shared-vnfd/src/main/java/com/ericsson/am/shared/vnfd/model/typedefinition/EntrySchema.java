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
package com.ericsson.am.shared.vnfd.model.typedefinition;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntrySchema {

    private String type;
    private String description;
    private List<Object> constraints;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<Object> getConstraints() {
        return constraints;
    }

    @SuppressWarnings("unchecked")
    public void setConstraints(final List<Object> constraints) {
        Map<?, ?> validKeys = (Map<?, ?>) constraints.get(0);
        this.constraints = (List<Object>) validKeys.get("valid_values");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EntrySchema that = (EntrySchema) o;

        return new EqualsBuilder().append(type, that.type)
                .append(description, that.description)
                .append(constraints, that.constraints)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(type).append(description).append(constraints).toHashCode();
    }
}
