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

import java.util.List;

import jakarta.persistence.criteria.JoinType;

public class FilterExpressionMultiValue<T extends Comparable<T>> {

    private String key;
    private OperandMultiValue operation;
    private List<T> values;
    private JoinType joinType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public OperandMultiValue getOperation() {
        return operation;
    }

    public void setOperation(OperandMultiValue operation) {
        this.operation = operation;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

}
