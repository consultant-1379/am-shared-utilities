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
import com.ericsson.am.shared.vnfd.model.policies.VduInstantiationLevels;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VduInstantiationLevelsParser implements PolicyParser<VduInstantiationLevels> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VduInstantiationLevelsParser.class);
    private static final String POLICY_KEY = PolicyKeyEnum.VDU_INSTANTIATION_LEVEL.getVnfdKey();

    @Override
    public Map<String, VduInstantiationLevels> parse(Map<String, JSONObject> jsonObjectPolicies, Validator validator, String vnfdVersion) {
        Map<String, VduInstantiationLevels> policyMap = new HashMap<>();
        filterPolicyByKey(jsonObjectPolicies, POLICY_KEY)
                .forEach(entry -> {
                    try {
                        policyMap.put(entry.getKey(), createVduInstantiationLevels(entry.getValue(), validator));
                    } catch (JsonProcessingException e) {
                        LOGGER.error(UNABLE_TO_PARSE_POLICIES, e);
                        throw new PolicyParseException(POLICY_KEY, e);
                    }
                });
        return policyMap;
    }

    private static VduInstantiationLevels createVduInstantiationLevels(JSONObject tempPolicy,
                                                                       Validator validator)
            throws JsonProcessingException {
        VduInstantiationLevels vduInstantiationLevels = new ObjectMapper().readValue(tempPolicy.toString(),
                                                                                     VduInstantiationLevels.class);
        final Set<ConstraintViolation<VduInstantiationLevels>> violations = validator.
                validate(vduInstantiationLevels);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return vduInstantiationLevels;
    }
}
