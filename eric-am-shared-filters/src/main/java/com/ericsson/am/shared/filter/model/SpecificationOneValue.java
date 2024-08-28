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

import java.util.regex.Pattern;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationOneValue<T> implements Specification<T> {

    private static final Pattern KEY_SEPARATOR_PATTERN = Pattern.compile("\\.");
    private static final long serialVersionUID = -4762186333258863007L;

    @SuppressWarnings("rawtypes")
    private final transient FilterExpressionOneValue filterExpressionOneValue;

    public <M extends Comparable<M>> SpecificationOneValue(final FilterExpressionOneValue<M> filterExpressionOneValue) {
        this.filterExpressionOneValue = filterExpressionOneValue;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getOperation().equals(OperandOneValue.EQUAL)) {
            return getPredicateForEqualsOperation(root, builder);
        } else if (filterExpressionOneValue.getOperation().equals(OperandOneValue.NOT_EQUAL)) {
            return getPredicateForNotEqualsOperation(root, builder);
        } else if (filterExpressionOneValue.getOperation().equals(OperandOneValue.GREATER_THAN)) {
            return getPredicateForGreaterThanOperation(root, builder);
        } else if (filterExpressionOneValue.getOperation().equals(OperandOneValue.GREATER_THAN_EQUAL)) {
            return getPredicateForGreaterThanEqualOperation(root, builder);
        } else if (filterExpressionOneValue.getOperation().equals(OperandOneValue.LESS_THAN)) {
            return getPredicateForLessThanOperation(root, builder);
        } else if (filterExpressionOneValue.getOperation().equals(OperandOneValue.LESS_THAN_EQUAL)) {
            return getPredicateForLessThanEqualOperation(root, builder);
        } else {
            throw new IllegalArgumentException("Invalid operation provided");
        }
    }

    private Predicate getPredicateForEqualsOperation(Root<T> root, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(filterExpressionOneValue.getKey());
            return builder.equal(root.join(keys[0], filterExpressionOneValue.getJoinType()).get(keys[1]),
                    filterExpressionOneValue.getValue());
        } else {
            return builder.equal(root.get(filterExpressionOneValue.getKey()), filterExpressionOneValue.getValue());
        }
    }

    private Predicate getPredicateForNotEqualsOperation(Root<T> root, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(filterExpressionOneValue.getKey());
            return builder.notEqual(root.join(keys[0], filterExpressionOneValue.getJoinType()).get(keys[1]),
                    filterExpressionOneValue.getValue());
        } else {
            return builder.notEqual(root.get(filterExpressionOneValue.getKey()), filterExpressionOneValue
                    .getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate getPredicateForGreaterThanOperation(Root<T> root, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(filterExpressionOneValue.getKey());
            return builder.greaterThan(root.join(keys[0], filterExpressionOneValue.getJoinType()).get(keys[1]),
                    filterExpressionOneValue.getValue());
        } else {
            return builder.greaterThan(root.get(filterExpressionOneValue.getKey()), filterExpressionOneValue
                    .getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate getPredicateForGreaterThanEqualOperation(Root<T> root, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(filterExpressionOneValue.getKey());
            return builder.greaterThanOrEqualTo(root.join(keys[0], filterExpressionOneValue.getJoinType())
                    .get(keys[1]), filterExpressionOneValue.getValue());
        } else {
            return builder.greaterThanOrEqualTo(root.get(filterExpressionOneValue.getKey()), filterExpressionOneValue
                    .getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate getPredicateForLessThanOperation(Root<T> root, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(filterExpressionOneValue.getKey());
            return builder.lessThan(root.join(keys[0], filterExpressionOneValue.getJoinType()).get(keys[1]),
                    filterExpressionOneValue.getValue());
        } else {
            return builder.lessThan(root.get(filterExpressionOneValue.getKey()), filterExpressionOneValue.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate getPredicateForLessThanEqualOperation(Root<T> root, CriteriaBuilder builder) {
        if (filterExpressionOneValue.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(filterExpressionOneValue.getKey());
            return builder.lessThanOrEqualTo(root.join(keys[0], filterExpressionOneValue.getJoinType())
                    .get(keys[1]), filterExpressionOneValue.getValue());
        } else {
            return builder.lessThanOrEqualTo(root.get(filterExpressionOneValue.getKey()), filterExpressionOneValue
                    .getValue());
        }
    }
}
