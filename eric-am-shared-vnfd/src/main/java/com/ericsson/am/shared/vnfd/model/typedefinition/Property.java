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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {
    private String name;
    private boolean required;
    private String type;
    private String description;
    @JsonProperty("entry_schema")
    private EntrySchema entrySchema;
    private List<Constraint> constraints;
    @JsonProperty("default")
    private Object propDefault;
    private Metadata metadata;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

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

    public EntrySchema getEntrySchema() {
        return entrySchema;
    }

    public void setEntrySchema(final EntrySchema entrySchema) {
        this.entrySchema = entrySchema;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(final List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public Object getPropDefault() {
        return propDefault;
    }

    public void setPropDefault(final Object propDefault) {
        this.propDefault = propDefault;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .append(required)
            .append(type)
            .append(description)
            .append(entrySchema)
            .append(constraints)
            .append(metadata)
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

        final Property property = (Property) o;

        return new EqualsBuilder()
            .append(name, property.name)
            .append(required, property.required)
            .append(type, property.type)
            .append(description, property.description)
            .append(entrySchema, property.entrySchema)
            .append(constraints, property.constraints)
            .append(metadata, property.metadata)
            .isEquals();
    }
}
