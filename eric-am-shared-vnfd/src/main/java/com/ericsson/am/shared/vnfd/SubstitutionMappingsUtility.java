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
package com.ericsson.am.shared.vnfd;

import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.REQUIREMENTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_KEY;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.SubstitutionMappings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SubstitutionMappingsUtility {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SubstitutionMappingsUtility() {
    }

    public static SubstitutionMappings createSubstitutionMappings(JSONObject vnfd) {
        SubstitutionMappings substitutionMappings = null;
        if (hasPropertyOfTypeJsonObject(vnfd, TOPOLOGY_TEMPLATE_KEY)) {
            JSONObject topologyTemplate = CommonUtility.getTopologyTemplate(vnfd);
            if (hasPropertyOfTypeJsonObject(topologyTemplate, SUBSTITUTION_MAPPINGS)) {
                JSONObject substitutionMappingJson = topologyTemplate.getJSONObject(SUBSTITUTION_MAPPINGS);
                substitutionMappings = new SubstitutionMappings();
                substitutionMappings.setNodeType(substitutionMappingJson.getString(NODE_TYPE_KEY));
                substitutionMappings.setRequirements(buildRequirements(substitutionMappingJson));
            }
        }
        return substitutionMappings;
    }

    private static Map<String, List<String>> buildRequirements(JSONObject substitutionMapping) {
        if (substitutionMapping.has(REQUIREMENTS_KEY)) {
            JSONObject jsonObject = substitutionMapping.getJSONObject(REQUIREMENTS_KEY);
            return jsonObject.toMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> OBJECT_MAPPER
                            .convertValue(entry.getValue(), new TypeReference<>() {
                            })));
        }

        return Collections.emptyMap();
    }
}
