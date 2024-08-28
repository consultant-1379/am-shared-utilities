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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import com.ericsson.am.shared.vnfd.model.typedefinition.EntrySchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodePropertiesDetails extends PropertiesDetails {

    @NotNull
    @JsonProperty("default")
    private Object defaultValue;

    @JsonProperty("constraints")
    private List<Object> constraints;

    @JsonProperty("entry_schema")
    private EntrySchema entrySchema;

    @AssertTrue(message = "Default value must match one of the values in constraints field")
    public boolean isValidDefaults() {
        if (constraints != null) {
            return this.constraints.contains(defaultValue);
        } else if ("list".equals(this.getType()) && entrySchema != null && entrySchema.getConstraints() != null) {
            return !Collections.disjoint(entrySchema.getConstraints(), (List<?>) defaultValue);
        } else {
            return true;
        }
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<Object> getConstraints() {
        return constraints;
    }

    @SuppressWarnings("unchecked")
    public void setConstraints(List<Object> constraints) {
        Map<?, ?> validKeys = (Map<?, ?>) constraints.get(0);
        List<?> constraintsList = (List<?>) validKeys.get("valid_values");
        this.constraints = (List<Object>) constraintsList;
    }

    public EntrySchema getEntrySchema() {
        return entrySchema;
    }

    public void setEntrySchema(final EntrySchema entrySchema) {
        this.entrySchema = entrySchema;
    }
}
