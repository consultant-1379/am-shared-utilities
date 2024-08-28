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

import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNABLE_TO_PARSE_POLICIES;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Validator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.exception.PolicyParseException;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.policies.PolicyKeyEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HelmParamsMappingParser implements PolicyParser<HelmParamsMapping> {

    private static final String POLICY_KEY = PolicyKeyEnum.HELM_PARAMS_MAPPING.getVnfdKey();
    private static final Logger LOGGER = LoggerFactory.getLogger(HelmParamsMappingParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Map<String, HelmParamsMapping> parse(final Map<String, JSONObject> jsonObjectPolicies,
                                                final Validator validator,
                                                final String vnfdVersion) {
        Map<String, HelmParamsMapping> policyMap = new HashMap<>();
        filterPolicyByKey(jsonObjectPolicies, POLICY_KEY)
                .forEach(entry -> {
                    try {
                        policyMap.put(entry.getKey(), createHelmParamsMapping(entry.getValue()));
                    } catch (JsonProcessingException e) {
                        LOGGER.error(UNABLE_TO_PARSE_POLICIES, e);
                        throw new PolicyParseException(POLICY_KEY, e);
                    }
                });
        return policyMap;
    }

    private static HelmParamsMapping createHelmParamsMapping(JSONObject tempPolicy)
            throws JsonProcessingException {
        JSONObject properties = tempPolicy.getJSONObject(PROPERTIES_KEY);
        return OBJECT_MAPPER.readValue(properties.toString(), HelmParamsMapping.class);
    }
}
