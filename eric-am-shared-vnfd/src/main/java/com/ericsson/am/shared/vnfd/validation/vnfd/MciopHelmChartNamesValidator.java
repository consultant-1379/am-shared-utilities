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
package com.ericsson.am.shared.vnfd.validation.vnfd;

import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryProperty;
import static com.ericsson.am.shared.vnfd.CommonUtility.getOptionalPropertyAsJsonObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGE_PREFIX;
import static com.ericsson.am.shared.vnfd.utils.Constants.MCIOP_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class MciopHelmChartNamesValidator implements VnfdValidator {

    @Override
    public VnfdValidationResult validate(JSONObject vnfd) {
        var nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);

        var singleNodeType = nodeTypes.keySet().stream().findFirst();
        if (singleNodeType.isEmpty()) {
            return new VnfdValidationResult(NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE);
        }

        var topologyTemplateJson = getMandatoryProperty(vnfd, TOPOLOGY_TEMPLATE_KEY);
        var nodeTemplatesJsonOpt = getOptionalPropertyAsJsonObject(topologyTemplateJson, NODE_TEMPLATES_KEY);
        if (nodeTemplatesJsonOpt.isEmpty()) {
            return new VnfdValidationResult();
        }

        var mciopChartNames = getMciopChartNames(nodeTemplatesJsonOpt.get());
        if (mciopChartNames.isEmpty()) {
            return new VnfdValidationResult();
        }

        var legacyChartNames = getLegacyChartNames(nodeTypes.getJSONObject(singleNodeType.get()));
        if (mciopChartNames.size() != legacyChartNames.size()) {
            return new VnfdValidationResult(String.format("Mciop nodes amount %s doesn't match exactly helm charts nodes %s",
                                                   mciopChartNames.size(),
                                                   legacyChartNames.size()));
        }

        var isValid = legacyChartNames.containsAll(mciopChartNames);
        return isValid ? new VnfdValidationResult() : new VnfdValidationResult("Mciop artifact names doesn't match to corresponded "
                                                                                       + "artifacts names in helm charts");
    }

    private static List<String> getMciopChartNames(JSONObject nodeTemplatesJson) {
        var mciopChartNames = new ArrayList<String>();
        nodeTemplatesJson.keySet().forEach(key -> {
            JSONObject nodeDetails = nodeTemplatesJson.getJSONObject(key);
            if (nodeDetails.has(TYPE_KEY) && MCIOP_NODE.equals(nodeDetails.get(TYPE_KEY)) && hasPropertyOfTypeJsonObject(nodeDetails,
                                                                                                                         ARTIFACTS_KEY)) {
                JSONObject artifact = nodeDetails.getJSONObject(ARTIFACTS_KEY);
                artifact.keySet().stream().findFirst().ifPresent(mciopChartNames::add);
            }
        });
        return mciopChartNames;
    }

    private static List<String> getLegacyChartNames(JSONObject nodeType) {
        var artifacts = getMandatoryProperty(nodeType, ARTIFACTS_KEY);
        return artifacts.keySet().stream().filter(chart -> chart.startsWith(HELM_PACKAGE_PREFIX)).collect(Collectors.toList());
    }

}
