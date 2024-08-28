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

import static com.ericsson.am.shared.vnfd.HelmChartUtility.parseHelmCharts;
import static com.ericsson.am.shared.vnfd.VnfdUtility.getArtifactKeys;
import static java.lang.String.format;

import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryProperty;
import static com.ericsson.am.shared.vnfd.CommonUtility.getOptionalPropertyAsJsonObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.VnfdUtility.nodeTemplateExists;
import static com.ericsson.am.shared.vnfd.utils.Constants.ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.ASSOCIATED_ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEFAULT_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_PROPERTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ERIC_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NAME_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_IN_DEPLOYABLE_MODULE_ARE_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULES_FOR_ASSOCIATED_ARTIFACTS_DUPLICATES_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULES_FOR_DUPLICATES_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULES_FOR_NAMES_DUPLICATES_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS_HAS_FAILED_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULE_RELATION_HAS_FAILED_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_EMPTY_DEPLOYABLE_MODULE_NAME_HAS_FAILED;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_OPTIONALITY_OF_DEPLOYABLE_MODULE_HAS_FAILED;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.google.common.collect.Sets;

public class DeployableModuleValidator implements VnfdValidator {

    @Override
    public VnfdValidationResult validate(final JSONObject vnfd) {
        List<String> artifactKeys = getArtifactKeys(vnfd);
        final Optional<JSONObject> topologyTemplateJson = getOptionalPropertyAsJsonObject(vnfd, TOPOLOGY_TEMPLATE_KEY);
        final JSONObject dataTypes = getMandatoryProperty(vnfd, DATA_TYPES_KEY);
        final JSONObject nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);
        final JSONObject modifiableAttributesExtensionsProperties = getModifiableAttributesProperties(dataTypes);
        final JSONObject nodeTemplateJson = getNodeTemplate(topologyTemplateJson.orElseGet(JSONObject::new));

        Set<JSONObject> deployableModulesFromNodeTemplate = nodeTemplateJson.keySet()
                .stream()
                .map(nodeTemplateJson::getJSONObject)
                .filter(DeployableModuleValidator::filterDeployableModuleType)
                .collect(Collectors.toSet());

        Set<String> deployableModulesKeysFromExtensions = getModifiableModulesKeysFromExtensions(modifiableAttributesExtensionsProperties);

        if (!deployableModulesKeysFromExtensions.isEmpty()) {
            VnfdValidationResult result =
                    deployableModulesWhichPresentInExtensionsShouldBePresentInNodeTemplate(deployableModulesKeysFromExtensions, nodeTemplateJson);
            if (!result.isValid()) {
                return result;
            }
        }

        if (!deployableModulesFromNodeTemplate.isEmpty()) {
            if (deployableModulesDoNotHaveProperties(deployableModulesFromNodeTemplate)) {
                return new VnfdValidationResult(PROPERTIES_IN_DEPLOYABLE_MODULE_ARE_MISSING);
            } else if (deployableModulesNamesShouldNotBeEmptyOrNull(deployableModulesFromNodeTemplate)) {
                return new VnfdValidationResult(VALIDATION_OF_EMPTY_DEPLOYABLE_MODULE_NAME_HAS_FAILED);
            }

            Set<String> chartsKeys = parseHelmCharts(topologyTemplateJson, nodeTypes, dataTypes, artifactKeys)
                    .stream()
                    .map(HelmChart::getChartKey)
                    .collect(Collectors.toSet());

            VnfdValidationResult associatedArtifactsValidationResult = associatedArtifactsShouldNotBeEmptyOrNull(deployableModulesFromNodeTemplate,
                                                                                                                 chartsKeys);

            if (!associatedArtifactsShouldNotBeEmptyOrNull(deployableModulesFromNodeTemplate, chartsKeys).isValid()) {
                return associatedArtifactsValidationResult;
            }

            VnfdValidationResult identicalDeployableModulesValidationResult =
                    deployableModulesShouldNotBeIdentical(deployableModulesFromNodeTemplate);

            if (!identicalDeployableModulesValidationResult.isValid()) {
                return identicalDeployableModulesValidationResult;
            }
        }
        return new VnfdValidationResult();
    }

    private static boolean deployableModulesDoNotHaveProperties(final Set<JSONObject> deployableModules) {
        return deployableModules.stream()
                .anyMatch(module -> module.isNull(PROPERTIES_KEY) || module.getJSONObject(PROPERTIES_KEY).isEmpty());
    }

    private static boolean deployableModulesNamesShouldNotBeEmptyOrNull(Set<JSONObject> deployableModules) {

        Predicate<JSONObject> propertyNameIsBlank = depModuleProperties ->
                depModuleProperties.isNull(NAME_KEY) || depModuleProperties.getString(NAME_KEY).isEmpty();

        return deployableModules
                .stream()
                .map(module -> module.getJSONObject(PROPERTIES_KEY))
                .anyMatch(propertyNameIsBlank);
    }

    private static VnfdValidationResult deployableModulesShouldNotBeIdentical(Set<JSONObject> deployableModulesFromNodeTemplate) {

        for (JSONObject deployableModule : deployableModulesFromNodeTemplate) {
            JSONObject deployableModuleProps = deployableModule.getJSONObject(PROPERTIES_KEY);
            JSONArray deployableModuleArtifacts = deployableModule.getJSONObject(PROPERTIES_KEY).getJSONArray(ASSOCIATED_ARTIFACTS_KEY);

            boolean isDeployableModuleWithDuplicatedNames = doesDeployableModuleHaveDuplicatedName(deployableModulesFromNodeTemplate,
                                                                                             deployableModuleProps);

            boolean isDeployableModuleWithDuplicatedCharts = doesDeployableModuleHaveDuplicatedCharts(deployableModulesFromNodeTemplate,
                                                                                                     deployableModuleArtifacts);

            if (isDeployableModuleWithDuplicatedNames && isDeployableModuleWithDuplicatedCharts) {
                return new VnfdValidationResult(format(VALIDATION_OF_DEPLOYABLE_MODULES_FOR_DUPLICATES_FORMAT,
                                                       deployableModuleProps.getString(NAME_KEY),
                                                       deployableModuleProps.getJSONArray(ASSOCIATED_ARTIFACTS_KEY).toString()));
            } else if (isDeployableModuleWithDuplicatedNames) {
                return new VnfdValidationResult(format(VALIDATION_OF_DEPLOYABLE_MODULES_FOR_NAMES_DUPLICATES_FORMAT,
                                                       deployableModuleProps.getString(NAME_KEY)));
            } else if (isDeployableModuleWithDuplicatedCharts) {
                return new VnfdValidationResult(format(VALIDATION_OF_DEPLOYABLE_MODULES_FOR_ASSOCIATED_ARTIFACTS_DUPLICATES_FORMAT,
                                                       deployableModuleProps.getJSONArray(ASSOCIATED_ARTIFACTS_KEY).toString()));
            }
        }
        return new VnfdValidationResult();
    }

    private static boolean doesDeployableModuleHaveDuplicatedName(Set<JSONObject> deployableModulesFromNodeTemplate,
                                                                  JSONObject deployableModuleProps) {
        return deployableModulesFromNodeTemplate
                .stream()
                .map(module -> module.getJSONObject(PROPERTIES_KEY))
                .filter(moduleProps -> deployableModuleProps.getString(NAME_KEY).equals(moduleProps.getString(NAME_KEY)))
                .count() > 1;
    }

    private static boolean doesDeployableModuleHaveDuplicatedCharts(Set<JSONObject> deployableModulesFromNodeTemplate,
                                                                    JSONArray deployableModuleArtifacts) {
        return deployableModulesFromNodeTemplate
                .stream()
                .map(module -> module.getJSONObject(PROPERTIES_KEY).getJSONArray(ASSOCIATED_ARTIFACTS_KEY))
                .filter(moduleArtifacts -> deployableModulesValidationForDuplicatedAssociatedArtifacts(deployableModuleArtifacts, moduleArtifacts))
                .count() > 1;
    }

    private static boolean deployableModulesValidationForDuplicatedAssociatedArtifacts(JSONArray sourceModuleArtifacts,
                                                                                       JSONArray targetModuleArtifacts) {
        if (sourceModuleArtifacts.length() == targetModuleArtifacts.length()) {
            Set<String> sourceModuleArtifactsSet = convertAssociatedArtifactsToSet(sourceModuleArtifacts);
            Set<String> targetModuleArtifactsSet = convertAssociatedArtifactsToSet(targetModuleArtifacts);
            return sourceModuleArtifactsSet.containsAll(targetModuleArtifactsSet);
        }
        return false;
    }

    private static Set<String> convertAssociatedArtifactsToSet(JSONArray deployableModuleArtifacts) {
        Set<String> result = new HashSet<>();
        deployableModuleArtifacts.forEach(e -> result.add(e.toString()));
        return result;
    }

    private static VnfdValidationResult deployableModulesWhichPresentInExtensionsShouldBePresentInNodeTemplate(
            Set<String> deployableModulesKeysFromExtensions, JSONObject nodeTemplateJson) {

        Set<String> deployableModulesKeysFromNodeTemplate = nodeTemplateJson.keySet()
                .stream()
                .filter(key -> filterDeployableModuleType(nodeTemplateJson.getJSONObject(key)))
                .collect(Collectors.toSet());

        Sets.SetView<String> depModuleUnknownReference = Sets.difference(deployableModulesKeysFromExtensions, deployableModulesKeysFromNodeTemplate);

        if (!deployableModulesKeysFromExtensions.containsAll(deployableModulesKeysFromNodeTemplate)) {
            return new VnfdValidationResult(VALIDATION_OF_OPTIONALITY_OF_DEPLOYABLE_MODULE_HAS_FAILED);
        } else if (!depModuleUnknownReference.isEmpty()) {
            return new VnfdValidationResult(
                    format(VALIDATION_OF_DEPLOYABLE_MODULE_RELATION_HAS_FAILED_FORMAT, depModuleUnknownReference)
            );
        } else {
            return new VnfdValidationResult();
        }
    }

    private static VnfdValidationResult associatedArtifactsShouldNotBeEmptyOrNull(Set<JSONObject> deployableModules, Set<String> chartsKeys) {
        boolean associatedArtifactsIsNotValid = deployableModules
                .stream()
                .map(deployableModule -> deployableModule.getJSONObject(PROPERTIES_KEY))
                .anyMatch(prop -> prop.isNull(ASSOCIATED_ARTIFACTS_KEY) || prop.getJSONArray(ASSOCIATED_ARTIFACTS_KEY).isEmpty());
        if (associatedArtifactsIsNotValid) {
            return new VnfdValidationResult(ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING);
        }

        Set<String> associatedArtifactsListFromDepModules = deployableModules
                .stream()
                .map(module -> module.getJSONObject(PROPERTIES_KEY))
                .map(props -> props.getJSONArray(ASSOCIATED_ARTIFACTS_KEY))
                .flatMap(array -> array.toList().stream())
                .map(Object::toString)
                .collect(Collectors.toSet());

        Sets.SetView<String> associatedArtifactsUnknownReference = Sets.difference(associatedArtifactsListFromDepModules, chartsKeys);

        if (!associatedArtifactsUnknownReference.isEmpty()) {
            return new VnfdValidationResult(
                    format(VALIDATION_OF_DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS_HAS_FAILED_FORMAT, associatedArtifactsUnknownReference)
            );
        } else {
            return new VnfdValidationResult();
        }
    }

    private static JSONObject getModifiableAttributesProperties(JSONObject dataTypes) {
        if (hasPropertyOfTypeJsonObject(dataTypes, ERIC_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE)) {
            return dataTypes.getJSONObject(ERIC_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE).getJSONObject(PROPERTIES_KEY);
        } else {
            return new JSONObject();
        }
    }

    private static JSONObject getNodeTemplate(JSONObject topologyTemplateJson) {
        if (nodeTemplateExists(topologyTemplateJson)) {
            return topologyTemplateJson.getJSONObject(NODE_TEMPLATES_KEY);
        } else {
            return new JSONObject();
        }
    }

    private static Set<String> getModifiableModulesKeysFromExtensions(JSONObject modifiableAttributesExtensionsProperties) {
        if (modifiableAttributesExtensionsProperties.isNull(DEPLOYABLE_MODULES_PROPERTY)
                || modifiableAttributesExtensionsProperties.getJSONObject(DEPLOYABLE_MODULES_PROPERTY).isNull(DEFAULT_KEY)) {
            return Collections.emptySet();
        }

        return modifiableAttributesExtensionsProperties.getJSONObject(DEPLOYABLE_MODULES_PROPERTY).getJSONObject(DEFAULT_KEY).keySet();
    }

    private static boolean filterDeployableModuleType(JSONObject node) {
        return DEPLOYABLE_MODULES_NODE.equals(node.getString(TYPE_KEY));
    }
}
