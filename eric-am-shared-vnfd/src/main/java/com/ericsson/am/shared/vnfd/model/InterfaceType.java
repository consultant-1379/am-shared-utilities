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

public class InterfaceType extends BaseType {

    private Object inputs;
    private Map<String, CustomOperation> operation;

    public Object getInputs() {
        return inputs;
    }

    public void setInputs(Object inputs) {
        this.inputs = inputs;
    }

    public Map<String, CustomOperation> getOperation() {
        return operation;
    }

    public void setOperation(Map<String, CustomOperation> operation) {
        this.operation = operation;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InterfaceType that = (InterfaceType) o;
        return Objects.equals(inputs, that.inputs) && Objects.equals(operation, that.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, operation);
    }
}
