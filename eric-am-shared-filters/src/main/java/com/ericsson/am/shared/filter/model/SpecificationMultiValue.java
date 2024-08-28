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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationMultiValue<T> implements Specification<T> {

    private static final Pattern KEY_SEPARATOR_PATTERN = Pattern.compile("\\.");
    private static final long serialVersionUID = 4018430794250797836L;

    @SuppressWarnings("rawtypes")
    private final transient FilterExpressionMultiValue expressionMulti;

    public <M extends Comparable<M>> SpecificationMultiValue(FilterExpressionMultiValue<M> expressionMulti) {
        this.expressionMulti = expressionMulti;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (expressionMulti.getOperation().equals(OperandMultiValue.IN)) {
            return getPredicateForInOperation(root, builder);
        } else if (expressionMulti.getOperation().equals(OperandMultiValue.NOT_IN)) {
            return getPredicateForNotInOperation(root, builder);
        } else if (expressionMulti.getOperation().equals(OperandMultiValue.CONTAINS)) {
            return getPredicateForContainsOperation(root, builder);
        } else if (expressionMulti.getOperation().equals(OperandMultiValue.NOT_CONTAINS)) {
            return getPredicateForNotContainsOperation(root, builder);
        } else {
            throw new IllegalArgumentException("Invalid operation provided");
        }
    }

    private Predicate getPredicateForInOperation(Root<T> root, CriteriaBuilder builder) {
        if (expressionMulti.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(expressionMulti.getKey());
            return builder.or(root.join(keys[0], expressionMulti.getJoinType()).get(keys[1]).in(expressionMulti
                    .getValues()));
        } else {
            return builder.or(root.get(expressionMulti.getKey()).in(expressionMulti.getValues()));
        }
    }

    private Predicate getPredicateForNotInOperation(Root<T> root, CriteriaBuilder builder) {
        if (expressionMulti.getJoinType() != null) {
            String[] keys = KEY_SEPARATOR_PATTERN.split(expressionMulti.getKey());
            return builder.or(root.join(keys[0], expressionMulti.getJoinType()).get(keys[1]).in(expressionMulti
                    .getValues())).not();
        } else {
            return builder.or(root.get(expressionMulti.getKey()).in(expressionMulti.getValues())).not();
        }
    }

    private Predicate getPredicateForContainsOperation(Root<T> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        for (Object value : expressionMulti.getValues()) {
            if (expressionMulti.getJoinType() != null) {
                String[] keys = KEY_SEPARATOR_PATTERN.split(expressionMulti.getKey());
                predicates.add(builder.like(root.join(keys[0], expressionMulti.getJoinType()).get(keys[1]),
                        "%" + value + "%"));
            } else {
                predicates.add(builder.like(root.get(expressionMulti.getKey()), "%" + value + "%"));
            }
        }
        return builder.or(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate getPredicateForNotContainsOperation(Root<T> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        for (Object value : expressionMulti.getValues()) {
            if (expressionMulti.getJoinType() != null) {
                String[] keys = KEY_SEPARATOR_PATTERN.split(expressionMulti.getKey());
                predicates.add(builder.like(root.join(keys[0], expressionMulti.getJoinType()).get(keys[1]),
                        "%" + value + "%"));
            } else {
                predicates.add(builder.like(root.get(expressionMulti.getKey()), "%" + value + "%"));
            }
        }
        return builder.or(predicates.toArray(new Predicate[predicates.size()])).not();
    }
}
