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

@JsonIgnoreProperties(ignoreUnknown = true)
public class TypeDefinitions {
    private String serviceModelID;

    @JsonProperty("typeDefinitions")
    private TypeDefinitionList typeDefinitionList;

    public String getServiceModelID() {
        return serviceModelID;
    }

    public void setServiceModelID(final String serviceModelID) {
        this.serviceModelID = serviceModelID;
    }

    public TypeDefinitionList getTypeDefinitionList() {
        return typeDefinitionList;
    }

    public void setTypeDefinitionList(final TypeDefinitionList typeDefinitionList) {
        this.typeDefinitionList = typeDefinitionList;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(serviceModelID)
                .append(typeDefinitionList)
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

        final TypeDefinitions that = (TypeDefinitions) o;

        return new EqualsBuilder()
                .append(serviceModelID, that.serviceModelID)
                .append(typeDefinitionList, that.typeDefinitionList)
                .isEquals();
    }
}
