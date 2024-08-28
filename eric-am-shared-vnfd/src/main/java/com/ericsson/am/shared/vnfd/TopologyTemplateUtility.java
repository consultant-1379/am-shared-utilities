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
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeString;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.MODIFIABLE_ATTRIBUTES;
import static com.ericsson.am.shared.vnfd.utils.Constants.NOT_PROVIDED_IN_VNFD_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_PROPERTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.Property;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCompute;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerDeployableUnit;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDeltas;

public final class TopologyTemplateUtility {

    private TopologyTemplateUtility() {
    }

    public static TopologyTemplate createTopologyTemplate(JSONObject vnfd, NodeType nodeType) {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        Policies policies = PolicyUtility.createAndValidatePolicies(vnfd);
        topologyTemplate.setPolicies(policies);
        topologyTemplate.setNodeTemplate(NodeTemplateUtility.createNodeTemplate(nodeType, vnfd));
        topologyTemplate.setInputs(getVnfInfoModifiableAttributesMap(vnfd));
        topologyTemplate.setSubstitutionMappings(SubstitutionMappingsUtility.createSubstitutionMappings(vnfd));
        return topologyTemplate;
    }

    public static void validateDataType(JSONObject vnfd, TopologyTemplate topologyTemplate) {
        Optional<String> modifiableAttribute = getModifiableAttribute(vnfd);
        if (modifiableAttribute.isPresent() && !topologyTemplate.getInputs().containsKey(MODIFIABLE_ATTRIBUTES)) {
            throw new IllegalArgumentException(String.format(NOT_PROVIDED_IN_VNFD_ERROR_MESSAGE,
                    TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE,
                    DATA_TYPES_KEY));
        }
    }

    public static void validateModifiableAttributesDefaults(Map<String, DataType> inputs, Policies policies) {
        if (inputs.containsKey(MODIFIABLE_ATTRIBUTES)) {
            DataType modifiableAttributesObject = inputs.get(MODIFIABLE_ATTRIBUTES);
            Property vnfControlledScaling = modifiableAttributesObject.getProperties().get(VNF_CONTROLLED_SCALING_PROPERTY);
            JSONObject defaultValue = new JSONObject(vnfControlledScaling.getDefaultValue());
            Set<String> defaultAspects = defaultValue.keySet();

            Set<String> allScalingAspects = PolicyUtility.getScalingAspectNames(policies.getAllScalingAspects());

            checkDefaultAspectsInPolicies(defaultAspects, allScalingAspects);
        }
    }

    public static boolean isAllNonScalableVdusInVnfd(TopologyTemplate topologyTemplate) {
        Policies policies = topologyTemplate.getPolicies();
        List<VduOsContainerDeployableUnit> osContainerDeployableUnits = topologyTemplate.getNodeTemplate().getOsContainerDeployableUnit();
        if (Objects.isNull(policies) || Objects.isNull(policies.getAllScalingAspects()) || CollectionUtils.isEmpty(osContainerDeployableUnits)) {
            return true;
        }
        List<String> allVdus = osContainerDeployableUnits.stream().map(VduCompute::getVduComputeKey).collect(Collectors.toList());
        return policies.getAllScalingAspectDelta().values().stream()
                .map(ScalingAspectDeltas::getTargets)
                .flatMap(Arrays::stream)
                .noneMatch(allVdus::contains);
    }

    private static Map<String, DataType> getVnfInfoModifiableAttributesMap(final JSONObject vnfd) {
        Map<String, DataType> modifiableAttributes = new HashMap<>();
        var allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
        if (allDataType != null) {
            Optional<String> modifiableAttributeName = getModifiableAttribute(vnfd);
            if (modifiableAttributeName.isPresent()) {
                Optional<Map.Entry<String, DataType>> modifiableAttributeDataType =
                        allDataType.entrySet().stream().filter(dataType ->
                                                                       dataType.getKey().equals(modifiableAttributeName.get()) &&
                                dataType.getValue().getDerivedFrom().equals(TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE)).findFirst();
                modifiableAttributeDataType.ifPresent(stringDataTypeEntry -> modifiableAttributes.put(MODIFIABLE_ATTRIBUTES,
                                                                                                      stringDataTypeEntry.getValue()));
            }
        }
        return modifiableAttributes;
    }

    private static Optional<String> getModifiableAttribute(JSONObject vnfd) {
        JSONObject topologyTemplateJson = CommonUtility.getTopologyTemplate(vnfd);
        if (checkThatVnfdContainsModifiableAttributes(topologyTemplateJson)) {
            JSONObject modifiableAttributeJson = topologyTemplateJson.getJSONObject(INPUTS_KEY)
                    .getJSONObject(MODIFIABLE_ATTRIBUTES);
            if (hasPropertyOfTypeString(modifiableAttributeJson, TYPE_KEY)) {
                return Optional.of(modifiableAttributeJson.getString(TYPE_KEY));
            }
        }
        return Optional.empty();
    }

    private static boolean checkThatVnfdContainsModifiableAttributes(JSONObject topologyTemplateJson) {
        return validateInputs(topologyTemplateJson) && validateModifiableAttributes(topologyTemplateJson);
    }

    private static boolean validateModifiableAttributes(final JSONObject topologyTemplate) {
        JSONObject topologyTemplateInputs = topologyTemplate.getJSONObject(INPUTS_KEY);
        return hasPropertyOfTypeJsonObject(topologyTemplateInputs, MODIFIABLE_ATTRIBUTES);
    }

    private static boolean validateInputs(final JSONObject topologyTemplateJson) {
        return hasPropertyOfTypeJsonObject(topologyTemplateJson, INPUTS_KEY);
    }

    private static void checkDefaultAspectsInPolicies(final Set<String> defaultAspects, final Set<String> allScalingAspects) {
        if (!allScalingAspects.containsAll(defaultAspects)) {
            throw new IllegalArgumentException("All default aspects in vnfdControlledScaling of extension are not defined in policies");
        }
    }

    public static void validateTopologyTemplateForInputs(final JSONObject vnfd) {
        JSONObject topologyTemplate = vnfd.getJSONObject("topology_template");
        if (topologyTemplate.isNull("inputs")) {
            throw new IllegalArgumentException("Inputs must be defined in topology template section of VNFD");
        }
    }
}
