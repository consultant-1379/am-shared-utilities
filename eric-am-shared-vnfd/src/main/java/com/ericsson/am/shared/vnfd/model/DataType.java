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

import java.util.Map;
import java.util.Objects;

public class DataType extends BaseType {

    private Constraints constraints;
    private Map<String, Property> properties;

    public Constraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
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
        final DataType dataType = (DataType) o;
        return Objects.equals(constraints, dataType.constraints) && Objects.equals(properties, dataType.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraints, properties);
    }
}
