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


import com.ericsson.am.shared.vnfd.CommonUtility;
import org.json.JSONObject;

import jakarta.validation.Validator;
import java.util.Map;
import java.util.stream.Stream;

public interface PolicyParser<T> {
    Map<String, T> parse(Map<String, JSONObject> jsonObjectPolicies, Validator validator, String vnfdVersion);

    default Stream<Map.Entry<String, JSONObject>> filterPolicyByKey(Map<String, JSONObject> jsonObjectPolicies, String... policies) {
        return jsonObjectPolicies.entrySet().stream()
                .filter(entry -> CommonUtility.hasPolicyJsonObjectKey(entry.getValue(), policies));
    }
}
