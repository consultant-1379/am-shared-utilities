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
package com.ericsson.am.shared.filter.model;

public enum OperandMultiValue {
    IN("in"), NOT_IN("nin"), CONTAINS("cont"), NOT_CONTAINS("ncont");

    private String filterOperation;

    OperandMultiValue(String filterOperation) {
        this.filterOperation = filterOperation;
    }

    public String getFilterOperation() {
        return filterOperation;
    }

    public static OperandMultiValue fromFilterOperation(String filterOperation) {
        for (OperandMultiValue operand : OperandMultiValue.values()) {
            if (operand.filterOperation.equals(filterOperation)) {
                return operand;
            }
        }
        return null;
    }
}
