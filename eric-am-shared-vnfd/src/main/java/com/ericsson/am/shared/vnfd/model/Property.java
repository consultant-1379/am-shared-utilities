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
package com.ericsson.am.shared.vnfd.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Property extends PropertiesDetails {

    private String status;
    private List<Object> constraints;
    private EntrySchema entrySchema;
    private String externalSchema;
    private Map<String, String> metadata;
    private String defaultValue;
    private DataType typeValue;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Object> constraints) {
        this.constraints = constraints;
    }

    public EntrySchema getEntrySchema() {
        return entrySchema;
    }

    public void setEntrySchema(EntrySchema entrySchema) {
        this.entrySchema = entrySchema;
    }

    public String getExternalSchema() {
        return externalSchema;
    }

    public void setExternalSchema(String externalSchema) {
        this.externalSchema = externalSchema;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public DataType getTypeValue() {
        return typeValue;
    }

    public void setTypeValue(DataType typeValue) {
        this.typeValue = typeValue;
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
        return Objects.equals(status, property.status) && Objects.equals(constraints, property.constraints)
                && Objects.equals(entrySchema, property.entrySchema) && Objects.equals(externalSchema, property.externalSchema)
                && Objects.equals(metadata, property.metadata) && Objects.equals(defaultValue, property.defaultValue)
                && Objects.equals(typeValue, property.typeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, constraints, entrySchema, externalSchema, metadata, defaultValue, typeValue);
    }
}
