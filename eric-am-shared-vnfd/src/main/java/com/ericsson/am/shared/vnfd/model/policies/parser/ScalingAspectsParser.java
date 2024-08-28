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
package com.ericsson.am.shared.vnfd.model.policies.parser;

import static com.ericsson.am.shared.vnfd.utils.Constants.UNABLE_TO_PARSE_POLICIES;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.exception.PolicyParseException;
import com.ericsson.am.shared.vnfd.model.policies.PolicyKeyEnum;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspects;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScalingAspectsParser implements PolicyParser<ScalingAspects> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingAspectsParser.class);

    private static final String POLICY_KEY = PolicyKeyEnum.SCALING_ASPECT.getVnfdKey();

    @Override
    public Map<String, ScalingAspects> parse(Map<String, JSONObject> jsonObjectPolicies, Validator validator, String vnfdVersion) {
        Map<String, ScalingAspects> policyMap = new HashMap<>();
        filterPolicyByKey(jsonObjectPolicies, POLICY_KEY)
                .forEach(entry -> {
                    try {
                        policyMap.put(entry.getKey(), createScalingAspects(entry.getValue(), validator));
                    } catch (JsonProcessingException e) {
                        LOGGER.error(UNABLE_TO_PARSE_POLICIES, e);
                        throw new PolicyParseException(POLICY_KEY, e);
                    }
                });

        return policyMap;
    }

    private static ScalingAspects createScalingAspects(JSONObject tempPolicy, Validator validator)
            throws JsonProcessingException {
        ScalingAspects aspect = new ObjectMapper().readValue(tempPolicy.toString(), ScalingAspects.class);
        final Set<ConstraintViolation<ScalingAspects>> violations = validator.validate(aspect);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return aspect;
    }
}
