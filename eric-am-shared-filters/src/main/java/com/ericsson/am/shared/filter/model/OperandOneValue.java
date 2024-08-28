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

public enum OperandOneValue {
    EQUAL("eq"), NOT_EQUAL("neq"), GREATER_THAN("gt"), GREATER_THAN_EQUAL("gte"),
    LESS_THAN("lt"), LESS_THAN_EQUAL("lte");

    private String filterOperation;

    OperandOneValue(String filterOperation) {
        this.filterOperation = filterOperation;
    }

    public String getFilterOperation() {
        return filterOperation;
    }


    public static OperandOneValue fromFilterOperation(String filterOperation) {
        for (OperandOneValue operand : OperandOneValue.values()) {
            if (operand.filterOperation.equals(filterOperation)) {
                return operand;
            }
        }
        return null;
    }
}
