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
package com.ericsson.am.shared.vnfd.model.servicemodel;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallOperation {
    private String operation;
    private List<Object> inputs;

    public String getOperation() {
        return operation;
    }

    public void setOperation(final String operation) {
        this.operation = operation;
    }

    public List<Object> getInputs() {
        return inputs;
    }

    public void setInputs(final List<Object> inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CallOperation that = (CallOperation) o;
        return Objects.equals(operation, that.operation) && Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, inputs);
    }
}
