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

import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_DETAILS_NOT_PRESENT_IN_THE_NODE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.CHART_PARAM;
import static com.ericsson.am.shared.vnfd.utils.Constants.CONSTRAINTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEFAULT_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DESCRIPTION_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.EMPTY_JSON_OBJECT_PROVIDED;
import static com.ericsson.am.shared.vnfd.utils.Constants.ENTRY_SCHEMA_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_PRIORITY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_VALUES;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_CONSTRAINT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_ENTRY_SCHEMA_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_PROPERTY_KEY_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.METADATA_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NOT_A_VALID_INTERFACE;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.POLICY_TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTY_NOT_FOUND_IN_VNFD;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTY_SHOULD_BE_A_MAP;
import static com.ericsson.am.shared.vnfd.utils.Constants.REQUIRED_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNABLE_TO_PARSE_JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.model.ArtifactsPropertiesDetail;
import com.ericsson.am.shared.vnfd.model.EntrySchema;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.Property;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Inputs;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CommonUtility {

    private static final List<String> VALID_INPUT_KEYS = new ArrayList<>(Arrays.asList(
            HELM_PACKAGES_KEY, HELM_PACKAGES_PRIORITY, HELM_VALUES, ADDITIONAL_PARAMETERS_KEY));
    private static final List<String> VALID_DATA_TYPE_PROPERTY_KEY = new ArrayList<>(Arrays.asList(
            TYPE_KEY, DESCRIPTION_KEY, DEFAULT_KEY, REQUIRED_KEY, ENTRY_SCHEMA_KEY, CONSTRAINTS_KEY, METADATA_KEY));

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    private CommonUtility() {
    }

    public static Map<String, Property> buildDataTypeProperties(JSONObject properties) {
        Map<String, Property> allProperty = new HashMap<>();
        for (String key : properties.keySet()) {
            JSONObject jsonProperty = properties.getJSONObject(key);
            validateDataTypePropertyKey(jsonProperty, key);
            Property property = new Property();
            property.setType(getMandatoryJsonAttributeAsString(jsonProperty, TYPE_KEY, key));
            property.setDescription(getOptionalJsonAttributeAsString(jsonProperty, DESCRIPTION_KEY));
            property.setDefaultValue(getOptionalJsonAttributeAsString(jsonProperty, DEFAULT_KEY));
            property.setRequired(getOptionalJsonAttributeAsBoolean(jsonProperty, REQUIRED_KEY));
            if (jsonProperty.has(ENTRY_SCHEMA_KEY)) {
                property.setEntrySchema(getEntrySchema(jsonProperty, key));
            }
            if (jsonProperty.has(CONSTRAINTS_KEY)) {
                property.setConstraints(getConstraints(jsonProperty, CONSTRAINTS_KEY));
            }
            String chartParam = "";
            Map<String, String> metadata = getOptionalMetaDataJsonAttribute(jsonProperty, METADATA_KEY);
            if (!metadata.isEmpty()) {
                property.setMetadata(metadata);
                chartParam = metadata.getOrDefault(CHART_PARAM, key);
            }
            if (chartParam.isEmpty()) {
                allProperty.put(key, property);
            } else {
                allProperty.put(chartParam, property);
            }
        }
        return allProperty;
    }

    @SuppressWarnings("java:S2250")
    private static void validateDataTypePropertyKey(JSONObject jsonProperty, String propertyKey) {
        for (String propertyAttribute : jsonProperty.keySet()) {
            if (!VALID_DATA_TYPE_PROPERTY_KEY.contains(propertyAttribute)) {
                throw new IllegalArgumentException(String.format(INVALID_PROPERTY_KEY_ERROR_MESSAGE, propertyAttribute,
                                                                 propertyKey));
            }
        }
    }

    private static EntrySchema getEntrySchema(JSONObject jsonProperty, String key) {
        EntrySchema entrySchema = new EntrySchema();
        if (jsonProperty.get(ENTRY_SCHEMA_KEY) instanceof JSONObject) {
            JSONObject entry = jsonProperty.getJSONObject(ENTRY_SCHEMA_KEY);
            entrySchema.setType(getMandatoryJsonAttributeAsString(entry, TYPE_KEY, ENTRY_SCHEMA_KEY));
            entrySchema.setDescription(getOptionalJsonAttributeAsString(entry, DESCRIPTION_KEY));
            entrySchema.setConstraints(getOptionalJsonAttributeAsString(entry, CONSTRAINTS_KEY));
        } else {
            throw new IllegalArgumentException(String.format(INVALID_ENTRY_SCHEMA_ERROR_MESSAGE, key));
        }
        return entrySchema;
    }

    public static String getMandatoryJsonAttributeAsString(JSONObject jsonAttribute, String attributeName,
                                                           String sectionName) {
        if (jsonAttribute.has(attributeName)) {
            if (!(jsonAttribute.get(attributeName) instanceof String)) {
                throw new IllegalArgumentException(attributeName + " should be a string for " + sectionName);
            }
            String attribute = jsonAttribute.getString(attributeName);
            if (Strings.isBlank(attribute)) {
                throw new IllegalArgumentException(attributeName + " cannot be null or empty for " + sectionName);
            }
            return attribute;
        } else {
            throw new IllegalArgumentException(attributeName + " missing for property " + sectionName);
        }
    }

    public static JSONObject getMandatoryJsonAttributeAsJSONObject(JSONObject jsonAttribute, String attributeName,
                                                                   String sectionName) {
        if (jsonAttribute.has(attributeName)) {
            if (!(jsonAttribute.get(attributeName) instanceof JSONObject)) {
                throw new IllegalArgumentException(attributeName + " should be a map for " + sectionName);
            }
            JSONObject attribute = jsonAttribute.getJSONObject(attributeName);
            if (Objects.isNull(attribute)) {
                throw new IllegalArgumentException(attributeName + " cannot be null or empty for " + sectionName);
            }
            return attribute;
        } else {
            throw new IllegalArgumentException(attributeName + " missing for property " + sectionName);
        }
    }

    public static JSONObject getOptionalJsonAttribute(JSONObject json, String attributeName) {
        return json.has(attributeName) ? json.getJSONObject(attributeName) : null;
    }

    public static Map<String, String> getOptionalMetaDataJsonAttribute(JSONObject jsonAttribute, String attributeName) {
        if (!jsonAttribute.has(attributeName)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        var jsonObject = jsonAttribute.getJSONObject(attributeName);
        jsonObject.keySet().forEach(key -> {
            String value = jsonObject.get(key).toString();
            result.put(key, value);
        });
        return result;
    }

    public static List<Object> getConstraints(JSONObject jsonProperty, String key) {
        if (jsonProperty.get(CONSTRAINTS_KEY) instanceof JSONArray) {
            return jsonProperty.getJSONArray(CONSTRAINTS_KEY).toList();
        } else {
            throw new IllegalArgumentException(String.format(INVALID_CONSTRAINT_ERROR_MESSAGE, key));
        }
    }

    public static String getOptionalJsonAttributeAsString(JSONObject jsonAttribute, String attributeName) {
        return jsonAttribute.has(attributeName) ? jsonAttribute.get(attributeName).toString() : null;
    }

    public static boolean getOptionalJsonAttributeAsBoolean(JSONObject jsonAttribute, String attributeName) {
        return jsonAttribute.has(attributeName) && jsonAttribute.getBoolean(attributeName);
    }

    public static <T> T getPojo(JSONObject data, Class<T> pojoName) {
        if (data == null) {
            throw new IllegalArgumentException(EMPTY_JSON_OBJECT_PROVIDED);
        }
        try {
            return OBJECT_MAPPER.readValue(data.toString(), pojoName);
        } catch (JSONException | IOException ex) {
            throw new IllegalArgumentException(String.format(UNABLE_TO_PARSE_JSON, ex.getMessage()), ex);
        }
    }

    public static boolean hasPolicyJsonObjectKey(JSONObject entryValue, String... policies) {
        return entryValue.has(POLICY_TYPE_KEY) && List.of(policies).contains(entryValue.getString(POLICY_TYPE_KEY));
    }

    public static String getPropertyAsString(String operationName, JSONObject jsonObject, String key) {
        if (jsonObject.has(key)) {
            return jsonObject.getString(key);
        } else {
            throw new IllegalArgumentException(String.format("Cannot get %s from Vnfd for operation %s", key, operationName));
        }
    }

    public static Optional<JSONObject> getOptionalPropertyAsJsonObject(JSONObject jsonObject, String propertyName) {
        if (hasPropertyOfTypeJsonObject(jsonObject, propertyName)) {
            return Optional.of(jsonObject.getJSONObject(propertyName));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<JSONArray> getOptionalPropertyAsArray(JSONObject jsonObject, String propertyName) {
        if (hasPropertyOfTypeJsonArray(jsonObject, propertyName)) {
            return Optional.of(jsonObject.getJSONArray(propertyName));
        } else {
            return Optional.empty();
        }
    }

    public static JSONObject getMandatoryProperty(JSONObject jsonObject, String propertyName) {
        if (!jsonObject.has(propertyName)) {
            throw new IllegalArgumentException(String.format(PROPERTY_NOT_FOUND_IN_VNFD, propertyName));
        }

        if (jsonObject.get(propertyName) instanceof JSONObject) {
            return jsonObject.getJSONObject(propertyName);
        } else {
            throw new IllegalArgumentException(String.format(PROPERTY_SHOULD_BE_A_MAP, propertyName));
        }
    }

    public static boolean hasPropertyOfTypeJsonObject(JSONObject jsonObject, String propertyName) {
        return jsonObject != null && jsonObject.has(propertyName) && jsonObject.get(propertyName) instanceof JSONObject;
    }

    public static boolean hasPropertyOfTypeJsonArray(JSONObject jsonObject, String propertyName) {
        return jsonObject != null && jsonObject.has(propertyName) && jsonObject.get(propertyName) instanceof JSONArray;
    }

    public static boolean hasPropertyOfTypeString(JSONObject jsonObject, String propertyName) {
        return jsonObject != null && jsonObject.has(propertyName) && jsonObject.get(propertyName) instanceof String;
    }

    public static JSONObject getTopologyTemplate(JSONObject vnfd) {
        if (vnfd.has(TOPOLOGY_TEMPLATE_KEY) && vnfd.getJSONObject(TOPOLOGY_TEMPLATE_KEY) != null) {
            return vnfd.getJSONObject(TOPOLOGY_TEMPLATE_KEY);
        }
        return null;
    }

    public static VnfmLcmInterface createInterface(String vnflcmInterface) {
        VnfmLcmInterface vnfmLcmInterface = new VnfmLcmInterface();
        checkInterfaceType(vnflcmInterface);
        vnfmLcmInterface.setType(vnflcmInterface);
        return vnfmLcmInterface;
    }

    private static void checkInterfaceType(String vnflcmInterface) {
        boolean exists = isValidLcmInterfaceType(vnflcmInterface);
        if (!exists) {
            throw new IllegalArgumentException(String.format(NOT_A_VALID_INTERFACE, vnflcmInterface));
        }
    }

    private static boolean isValidLcmInterfaceType(String vnflcmInterface) {
        return EnumUtils.isValidEnum(VnfmLcmInterface.Type.class, vnflcmInterface.toUpperCase(Locale.ENGLISH));
    }

    public static List<ArtifactsPropertiesDetail> getArtifacts(JSONObject nodeType) {
        List<ArtifactsPropertiesDetail> artifacts = new ArrayList<>();
        for (String nodeName : nodeType.keySet()) {
            if (nodeType.get(nodeName) instanceof JSONObject &&
                    hasPropertyOfTypeJsonObject(nodeType.getJSONObject(nodeName), ARTIFACTS_KEY)) {
                JSONObject artifactsJson = nodeType.getJSONObject(nodeName).getJSONObject(ARTIFACTS_KEY);
                for (String key : artifactsJson.keySet()) {
                    validateAndAddArtifacts(artifacts, artifactsJson, key);
                }
            } else {
                throw new IllegalArgumentException(ARTIFACTS_DETAILS_NOT_PRESENT_IN_THE_NODE_TYPE);
            }
        }
        return artifacts;
    }

    private static void validateAndAddArtifacts(final List<ArtifactsPropertiesDetail> artifacts,
                                                final JSONObject artifactsJson, final String key) {
        if (hasPropertyOfTypeJsonObject(artifactsJson, key)) {
            ArtifactsPropertiesDetail artifact = VnfdUtility.validateAndGetArtifactPropertiesDetails(key, artifactsJson);
            artifact.setId(key);
            artifacts.add(artifact);
        }
    }

    public static void addVnfmInterface(final List<VnfmLcmInterface> interfacesList, final JSONObject interfaceDetails) {
        JSONObject operationsList = interfaceDetails;
        if (interfaceDetails.has(OPERATIONS_KEY)) {
            operationsList = interfaceDetails.getJSONObject(OPERATIONS_KEY);
        }

        for (String operationName : operationsList.keySet()) {
            if (!TYPE_KEY.equalsIgnoreCase(operationName)) {
                VnfmLcmInterface interfaceToAdd = CommonUtility.createInterface(operationName);
                JSONObject operationDetails = operationsList.getJSONObject(operationName);
                CommonUtility.validateAndCreatePlaceholderInputs(interfaceToAdd, operationDetails);
                interfacesList.add(interfaceToAdd);
            }
        }
    }

    private static void validateAndCreatePlaceholderInputs(final VnfmLcmInterface interfaceToAdd,
                                                           final JSONObject operationDetails) {
        if (operationDetails.length() != 0) {
            JSONObject inputContents = operationDetails.getJSONObject(INPUTS_KEY);
            Inputs inputsToAdd = new Inputs();
            if (VALID_INPUT_KEYS.containsAll(inputContents.keySet())) {
                for (String key : inputContents.keySet()) {
                    addAdditionalParamsType(inputContents, inputsToAdd, key);
                    interfaceToAdd.setInputs(inputsToAdd);
                }
            }
        }
    }

    private static void addAdditionalParamsType(final JSONObject inputContents, final Inputs inputsToAdd,
                                                final String key) {
        if (key.equalsIgnoreCase(ADDITIONAL_PARAMETERS_KEY)) {
            String type = inputContents.getJSONObject(ADDITIONAL_PARAMETERS_KEY).get(TYPE_KEY).toString();
            inputsToAdd.setAdditionalParamsDataType(type);
        }
    }

    public static Map<String, Boolean> getValidFlavours(NodeProperties nodeProperties) {
        Map<String, Boolean> flavourIds = new HashMap<>();
        String defaultFlavour = nodeProperties.getFlavourId().getDefaultValue().toString();
        List<?> constraints = nodeProperties.getFlavourId().getConstraints();
        if (constraints != null && !constraints.isEmpty()) {
            for (Object key : constraints) {
                if (key.toString().equalsIgnoreCase(defaultFlavour)) {
                    flavourIds.put(key.toString(), Boolean.TRUE);
                } else {
                    flavourIds.put(key.toString(), Boolean.FALSE);
                }
            }
        } else {
            if (Strings.isBlank(defaultFlavour)) {
                throw new IllegalArgumentException("Flavour constraints array cannot be empty.");
            }
            flavourIds.put(defaultFlavour, true);
        }
        return flavourIds;
    }

    public static boolean isCnfChartPresent(Collection<String> artifacts) {
        return artifacts.stream().anyMatch(chart -> chart.startsWith(HELM_PACKAGE_KEY));
    }

    public static void validateNoMissedNames(Set<String> firstSetOfNames,
                                             Set<String> secondSetOfNames,
                                             String errorTemplate) {
        String missedVduNames = firstSetOfNames.stream()
                .filter(name -> !secondSetOfNames.contains(name))
                .collect(Collectors.joining(", "));

        if (StringUtils.isNotBlank(missedVduNames)) {
            throw new IllegalArgumentException(String.format(errorTemplate, missedVduNames));
        }
    }
}
