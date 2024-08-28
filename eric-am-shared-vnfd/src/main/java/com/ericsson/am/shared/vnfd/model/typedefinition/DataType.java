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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataType {
    private String name;
    @JsonProperty("derived_from")
    private String derivedFrom;
    private String description;
    private List<Property> properties;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(final String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(final List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(derivedFrom)
                .append(description)
                .append(properties)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataType dataType = (DataType) o;

        return new EqualsBuilder()
                .append(name, dataType.name)
                .append(derivedFrom, dataType.derivedFrom)
                .append(description, dataType.description)
                .append(properties, dataType.properties)
                .isEquals();
    }
}
