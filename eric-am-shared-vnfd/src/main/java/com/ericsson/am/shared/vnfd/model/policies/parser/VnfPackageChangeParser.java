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

import static com.ericsson.am.shared.vnfd.model.policies.PolicyKeyEnum.CUSTOM_VNF_PACKAGE_CHANGE_POLICY_TYPE;
import static com.ericsson.am.shared.vnfd.model.policies.PolicyKeyEnum.VNF_PACKAGE_CHANGE_POLICY_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNABLE_TO_PARSE_POLICIES;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_TOSCA_1_3_VERSION;

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
import com.ericsson.am.shared.vnfd.mapper.VnfPackageChangePolicyMapper;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VnfPackageChangeParser implements PolicyParser<VnfPackageChangePolicyTosca1dot2> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VnfPackageChangeParser.class);

    public static VnfPackageChangePolicyTosca1dot2 createVnfPackageChangePolicy(JSONObject tempPolicy,
                                                                                Validator validator)
            throws JsonProcessingException {
        VnfPackageChangePolicyTosca1dot2 vnfPackageChangePolicy = new ObjectMapper().readValue(tempPolicy.toString(),
                VnfPackageChangePolicyTosca1dot2.class);
        VnfPackageChangePolicyMapper.convert(tempPolicy, vnfPackageChangePolicy);
        final Set<ConstraintViolation<VnfPackageChangePolicyTosca1dot2>> violations = validator.
                validate(vnfPackageChangePolicy);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return vnfPackageChangePolicy;
    }

    public static VnfPackageChangePolicyTosca1dot2 createVnfPackageChangePolicyTosca1dot3(JSONObject tempPolicy,
                                                                                          Validator validator)
            throws JsonProcessingException {
        VnfPackageChangePolicyTosca1dot3 vnfPackageChangePolicy = new ObjectMapper().readValue(tempPolicy.toString(),
                VnfPackageChangePolicyTosca1dot3.class);
        VnfPackageChangePolicyMapper.convert(tempPolicy, vnfPackageChangePolicy);
        final Set<ConstraintViolation<VnfPackageChangePolicyTosca1dot3>> violations = validator.
                validate(vnfPackageChangePolicy);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return VnfPackageChangePolicyMapper.convert(vnfPackageChangePolicy);
    }

    public static VnfPackageChangePolicyTosca1dot2 getVnfPackageChangePolicy(Validator validator, String vnfdVersion,
                                                                             JSONObject policyContent) {
        try {
            if (VNFD_TOSCA_1_3_VERSION.equals(vnfdVersion)) {
                return createVnfPackageChangePolicyTosca1dot3(policyContent, validator);
            } else {
                return createVnfPackageChangePolicy(policyContent, validator);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(UNABLE_TO_PARSE_POLICIES, e.getMessage());
            throw new PolicyParseException(VNF_PACKAGE_CHANGE_POLICY_TYPE.getVnfdKey(), e);
        }
    }

    @Override
    public Map<String, VnfPackageChangePolicyTosca1dot2> parse(Map<String, JSONObject> jsonObjectPolicies, Validator validator, String vnfdVersion) {
        Map<String, VnfPackageChangePolicyTosca1dot2> policyMap = new HashMap<>();
        filterPolicyByKey(jsonObjectPolicies, VNF_PACKAGE_CHANGE_POLICY_TYPE.getVnfdKey(), CUSTOM_VNF_PACKAGE_CHANGE_POLICY_TYPE.getVnfdKey())
                .forEach(entry -> policyMap.put(entry.getKey(), getVnfPackageChangePolicy(validator, vnfdVersion, entry.getValue())));
        return policyMap;
    }
}
