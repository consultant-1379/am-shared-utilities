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

import jakarta.persistence.criteria.JoinType;

public class FilterExpressionOneValue<T extends Comparable<T>> {

    private String key;
    private OperandOneValue operation;
    private T value;
    private JoinType joinType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public OperandOneValue getOperation() {
        return operation;
    }

    public void setOperation(OperandOneValue operation) {
        this.operation = operation;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }
}
