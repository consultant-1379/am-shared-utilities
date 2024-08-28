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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.exception.PolicyParseException;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.PolicyKeyEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InstantiationLevelsParser implements PolicyParser<InstantiationLevels> {
    private static final String POLICY_KEY = PolicyKeyEnum.INSTANTIATION_LEVEL.getVnfdKey();
    private static final String DEFAULT_LEVEL_MUST_BE_SET_IF_MULTIPLE_LEVELS_PRESENT = "Default level must be " +
            "defined if multiple Instantiation levels are present in Policies";
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiationLevelsParser.class);

    @Override
    public Map<String, InstantiationLevels> parse(Map<String, JSONObject> jsonObjectPolicies, Validator validator, String vnfdVersion) {
        Map<String, InstantiationLevels> policyMap = new HashMap<>();
        filterPolicyByKey(jsonObjectPolicies, POLICY_KEY)
                .forEach(entry -> {
                    try {
                        policyMap.put(entry.getKey(), createInstantiationLevels(entry.getValue(), validator));
                    } catch (JsonProcessingException e) {
                        LOGGER.error(UNABLE_TO_PARSE_POLICIES, e);
                        throw new PolicyParseException(POLICY_KEY, e);
                    }
                });
        return policyMap;
    }

    private static InstantiationLevels createInstantiationLevels(JSONObject tempPolicy, Validator validator)
            throws JsonProcessingException {
        InstantiationLevels instantiationLevels = new ObjectMapper().readValue(tempPolicy.toString(),
                                                                               InstantiationLevels.class);

        final Set<ConstraintViolation<InstantiationLevels>> violations = validator.
                validate(instantiationLevels);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (instantiationLevels.getProperties().getInstantiationLevelsDataInfo().size() > 1
                && isDefaultInstantiationLevelNullOrEmpty(instantiationLevels)) {
            throw new IllegalArgumentException(DEFAULT_LEVEL_MUST_BE_SET_IF_MULTIPLE_LEVELS_PRESENT);
        }
        if (instantiationLevels.getProperties().getInstantiationLevelsDataInfo().size() == 1) {
            String defaultLevel = null;
            Iterator<String> iterator = instantiationLevels.getProperties().getInstantiationLevelsDataInfo().keySet().iterator();
            if (iterator.hasNext()) {
                defaultLevel = iterator.next();
            }
            instantiationLevels.getProperties().setDefaultLevel(defaultLevel);
        }
        return instantiationLevels;
    }

    private static boolean isDefaultInstantiationLevelNullOrEmpty(final InstantiationLevels instantiationLevels) {
        return instantiationLevels.getProperties().getDefaultLevel() == null || instantiationLevels.getProperties().getDefaultLevel().isEmpty();
    }
}
