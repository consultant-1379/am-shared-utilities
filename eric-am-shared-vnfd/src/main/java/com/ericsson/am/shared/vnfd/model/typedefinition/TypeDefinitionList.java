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
public class TypeDefinitionList {
    @JsonProperty("data_types")
    private List<DataType> dataTypes;

    public List<DataType> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(final List<DataType> dataTypes) {
        this.dataTypes = dataTypes;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(dataTypes)
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

        final TypeDefinitionList that = (TypeDefinitionList) o;

        return new EqualsBuilder().append(dataTypes, that.dataTypes)
                .isEquals();
    }
}
