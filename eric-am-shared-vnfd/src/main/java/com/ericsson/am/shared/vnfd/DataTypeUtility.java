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

import static com.ericsson.am.shared.vnfd.CommonUtility.buildDataTypeProperties;
import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryJsonAttributeAsJSONObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryJsonAttributeAsString;
import static com.ericsson.am.shared.vnfd.CommonUtility.getOptionalPropertyAsJsonObject;
import static com.ericsson.am.shared.vnfd.utils.Constants.CISM_CONTROLLED;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_DEFAULT_VALUE_NOT_MAP;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_DISABLED;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_ENABLED;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_PROPERTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ENTRY_SCHEMA_CONSTRAINT_CANT_BE_BLANK_FOR_EXTENSIONS_PROPERTIES;
import static com.ericsson.am.shared.vnfd.utils.Constants.ENTRY_SCHEMA_NULL_FOR_EXTENSIONS_PROPERTIES;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSION_KEYS_THAT_SHOULD_BE_OF_TYPE_MAP;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSION_CANT_BE_NULL;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSION_PROPERTIES_INVALID_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.FILE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_DEPLOYABLE_MODULES_ENTRY_SCHEMA_CONSTRAINTS_VALUE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_EXTENSION_DATA_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_VALUE_FOR_ASPECT_IN_VNF_CONTROLLED_SCALING_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_VALUE_FOR_KEY_IN_DEPLOYABLE_MODULES_DEFAULTS_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_VNF_CONTROLLED_SCALING_ENTRY_SCHEMA_CONSTRAINTS_VALUE;
import static com.ericsson.am.shared.vnfd.utils.Constants.MANUAL_CONTROLLED;
import static com.ericsson.am.shared.vnfd.utils.Constants.MAP;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_CANT_BE_NULL_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_NULL_FOR_DATATYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALID_VALUES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_DEFAULT_VALUE_BLANK_FOR_ASPECT_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_DEFAULT_VALUE_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_DEFAULT_VALUE_NOT_MAP;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_PROPERTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_PROPERTY_MISSING;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.EntrySchema;
import com.ericsson.am.shared.vnfd.model.Property;

public final class DataTypeUtility {

    private static final List<String> PRIMITIVE_DATA_TYPE = List.of("string", "boolean",
                                                                    "integer", "float",
                                                                    "timestamp", "list",
                                                                    "map");

    private DataTypeUtility() {
    }

    public static Map<String, DataType> buildDataTypesFromVnfd(JSONObject vnfd) {
        Optional<JSONObject> dataTypesJsonOpt = getOptionalPropertyAsJsonObject(vnfd, DATA_TYPES_KEY);
        if (dataTypesJsonOpt.isEmpty()) {
            return new HashMap<>();
        }
        JSONObject dataTypesJson = dataTypesJsonOpt.get();
        return buildDataTypes(dataTypesJson);
    }

    public static Map<String, DataType> buildDataTypes(final JSONObject dataTypesJson) {
        Map<String, DataType> allDataTypes = new HashMap<>();
        for (String dataTypeName : dataTypesJson.keySet()) {
            JSONObject jsonDataType = dataTypesJson.getJSONObject(dataTypeName);
            String derivedType = getMandatoryJsonAttributeAsString(jsonDataType, DERIVED_FROM_KEY, dataTypeName);

            DataType dataType = new DataType();
            dataType.setDerivedFrom(derivedType);
            if (FILE.equals(dataTypeName)) {
                dataType.setProperties(new HashMap<>());
            } else {
                JSONObject dataTypePropertiesJson = getMandatoryJsonAttributeAsJSONObject(
                        jsonDataType, PROPERTIES_KEY, dataTypeName);
                Map<String, Property> dataTypeProperties = buildDataTypeProperties(dataTypePropertiesJson);
                dataType.setProperties(dataTypeProperties);
            }
            allDataTypes.put(dataTypeName, dataType);
        }
        validateAndUpdateDataTypeReference(allDataTypes);
        return allDataTypes;
    }

    private static void validateAndUpdateDataTypeReference(Map<String, DataType> allDataTypes) {
        for (Map.Entry<String, DataType> entry : allDataTypes.entrySet()) {
            checkAndUpdateDataTypeProperties(allDataTypes, entry.getValue());
            if (entry.getValue().getDerivedFrom().equals(TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE)) {
                validateExtensionTypeValue(entry.getValue(), entry.getKey());
                validatePropertiesOfVnfInfoModifiableAttributes(entry.getValue().getProperties(), entry.getKey());
            }
        }
    }

    private static void validateExtensionTypeValue(DataType dataType, String dataTypeKey) {
        Property extensions = dataType.getProperties().get(EXTENSIONS_KEY);
        if (extensions == null) {
            throw new IllegalArgumentException(String.format(EXTENSION_CANT_BE_NULL, dataTypeKey));
        }
        if (extensions.getTypeValue() == null) {
            throw new IllegalArgumentException(String.format(DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE, extensions.getType()));
        }
    }

    private static void checkAndUpdateDataTypeProperties(Map<String, DataType> allDataTypes, DataType datType) {
        for (Map.Entry<String, Property> properties : datType.getProperties().entrySet()) {
            Property property = properties.getValue();
            if (!PRIMITIVE_DATA_TYPE.contains(property.getType())) {
                DataType dataType = allDataTypes.get(property.getType());
                if (dataType != null) {
                    property.setTypeValue(dataType);
                }
            }
        }
    }

    private static void validatePropertiesOfVnfInfoModifiableAttributes(Map<String, Property> allProperties,
                                                                        String dataTypeKey) {
        if (allProperties != null && !allProperties.isEmpty()) {
            validateExtensionsDataType(allProperties.get(EXTENSIONS_KEY), dataTypeKey);
        } else {
            throw new IllegalArgumentException(String.format(PROPERTIES_NULL_FOR_DATATYPE_ERROR_MESSAGE, dataTypeKey));
        }
    }

    private static void validateExtensionsDataType(Property extensionsDataType, String dataTypeKey) {
        if (extensionsDataType == null) {
            throw new IllegalArgumentException(String.format(EXTENSION_CANT_BE_NULL, dataTypeKey));
        }
        if (!extensionsDataType.getTypeValue().getDerivedFrom().equals(TOSCA_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE)) {
            throw new IllegalArgumentException(String.format(INVALID_EXTENSION_DATA_TYPE_ERROR_MESSAGE, dataTypeKey));
        }
        validateAndCheckForVnfControlledScalingInExtensionDefinitions(extensionsDataType.getTypeValue(),
                                                                      extensionsDataType.getType());
    }

    private static void validateAndCheckForVnfControlledScalingInExtensionDefinitions(DataType extensionDataType,
                                                                                      String key) {
        Map<String, Property> allProperties = extensionDataType.getProperties();
        if (allProperties == null || allProperties.isEmpty()) {
            throw new IllegalArgumentException(String.format(PROPERTIES_CANT_BE_NULL_ERROR_MESSAGE, key));
        }
        boolean vnfControlledScalingOrDeployableModulesAreNotMap = allProperties.keySet()
                .stream()
                .filter(EXTENSION_KEYS_THAT_SHOULD_BE_OF_TYPE_MAP::contains)
                .anyMatch(extensionKey -> !MAP.equals(allProperties.get(extensionKey).getType()));
        if (vnfControlledScalingOrDeployableModulesAreNotMap) {
            throw new IllegalArgumentException(String.format(EXTENSION_PROPERTIES_INVALID_TYPE_ERROR_MESSAGE, key));
        }
        validateAndCheckForVnfControlledScalingInExtensionDefinitions(allProperties.get(VNF_CONTROLLED_SCALING_PROPERTY), key);
        validateAndCheckForDeployableModulesInExtensionsDefinitions(allProperties.get(DEPLOYABLE_MODULES_PROPERTY), key);
    }

    private static void validateAndCheckForVnfControlledScalingInExtensionDefinitions(Property vnfControlledScaling, String key) {
        if (vnfControlledScaling == null) {
            throw new IllegalArgumentException(String.format(VNF_CONTROLLED_SCALING_PROPERTY_MISSING, key));
        } else {
            if (Strings.isBlank(vnfControlledScaling.getDefaultValue())) {
                throw new IllegalArgumentException(String.format(VNF_CONTROLLED_SCALING_DEFAULT_VALUE_MISSING, key));
            }
            validateDefaultValueForExtensionProperty(vnfControlledScaling,
                                                     List.of(CISM_CONTROLLED, MANUAL_CONTROLLED),
                                                     VNF_CONTROLLED_SCALING_DEFAULT_VALUE_NOT_MAP,
                                                     VNF_CONTROLLED_SCALING_DEFAULT_VALUE_BLANK_FOR_ASPECT_FORMAT,
                                                     INVALID_VALUE_FOR_ASPECT_IN_VNF_CONTROLLED_SCALING_FORMAT);
            validateEntrySchemaConstraintsForExtensionsProperty(
                    vnfControlledScaling,
                    key,
                    List.of(CISM_CONTROLLED, MANUAL_CONTROLLED),
                    INVALID_VNF_CONTROLLED_SCALING_ENTRY_SCHEMA_CONSTRAINTS_VALUE
            );
        }
    }

    private static void validateAndCheckForDeployableModulesInExtensionsDefinitions(Property deployableModules, String key) {
        if (deployableModules != null) {
            validateDefaultValueForExtensionProperty(deployableModules,
                                                     List.of(DEPLOYABLE_MODULES_ENABLED, DEPLOYABLE_MODULES_DISABLED),
                                                     DEPLOYABLE_MODULES_DEFAULT_VALUE_NOT_MAP,
                                                     DEPLOYABLE_MODULES_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT,
                                                     INVALID_VALUE_FOR_KEY_IN_DEPLOYABLE_MODULES_DEFAULTS_FORMAT);
            validateEntrySchemaConstraintsForExtensionsProperty(
                    deployableModules,
                    key,
                    List.of(DEPLOYABLE_MODULES_ENABLED, DEPLOYABLE_MODULES_DISABLED),
                    INVALID_DEPLOYABLE_MODULES_ENTRY_SCHEMA_CONSTRAINTS_VALUE
            );
        }
    }

    private static void validateDefaultValueForExtensionProperty(Property extensionsProperty,
                                                                 Collection<String> actualValidValues,
                                                                 String defaultValueObjectIsNotOfMapTypeMessage,
                                                                 String defaultValueIsBlankMessage,
                                                                 String invalidValueProvidedInDefaults) {

        try {
            JSONObject defaultValue = new JSONObject(extensionsProperty.getDefaultValue());
            Iterator<String> iterator = defaultValue.keys();
            while (iterator.hasNext()) {
                String defaultValueKey = iterator.next();
                String defaultValueForAspect = defaultValue.getString(defaultValueKey);
                validateValuesOfDefaultSection(defaultValueKey,
                                               defaultValueForAspect,
                                               actualValidValues,
                                               defaultValueIsBlankMessage,
                                               invalidValueProvidedInDefaults);
            }
        } catch (JSONException je) {
            throw new IllegalArgumentException(defaultValueObjectIsNotOfMapTypeMessage, je);
        }
    }

    private static void validateValuesOfDefaultSection(String defaultKey,
                                                       String defaultValue,
                                                       Collection<String> actualValidValues,
                                                       String defaultValueIsBlankMessage,
                                                       String invalidValueProvidedInDefaults) {
        if (Strings.isBlank(defaultValue)) {
            throw new IllegalArgumentException(String.format(defaultValueIsBlankMessage,
                                                             defaultKey));
        } else if (!actualValidValues.contains(defaultValue)) {
            throw new IllegalArgumentException(String.format(invalidValueProvidedInDefaults,
                                                             defaultKey, defaultValue));
        }
    }

    private static void validateEntrySchemaConstraintsForExtensionsProperty(Property vnfControlledScaling,
                                                                            String dataTypeKey,
                                                                            Collection<String> actualValidValues,
                                                                            String invalidEntrySchemaMessage) {
        EntrySchema entrySchema = vnfControlledScaling.getEntrySchema();
        if (entrySchema == null) {
            throw new IllegalArgumentException(String.format(ENTRY_SCHEMA_NULL_FOR_EXTENSIONS_PROPERTIES,
                                                             dataTypeKey));
        }
        validateConstraintInVnfControlledScalingProperty(entrySchema, dataTypeKey, actualValidValues, invalidEntrySchemaMessage);
    }

    private static void validateConstraintInVnfControlledScalingProperty(EntrySchema entrySchema,
                                                                         String dataTypeKey,
                                                                         Collection<String> actualValidValues,
                                                                         String invalidEntrySchemaMessage) {
        String constraints = entrySchema.getConstraints();
        if (Strings.isBlank(constraints)) {
            throw new IllegalArgumentException(String.format(
                    ENTRY_SCHEMA_CONSTRAINT_CANT_BE_BLANK_FOR_EXTENSIONS_PROPERTIES, dataTypeKey));
        }
        try {
            JSONArray constraint = new JSONArray(constraints);
            if (constraint.length() != 1) {
                throw new IllegalArgumentException(invalidEntrySchemaMessage);
            }
            JSONObject validValues = (JSONObject) constraint.get(0);
            if (!validValues.has(VALID_VALUES_KEY)) {
                throw new IllegalArgumentException(invalidEntrySchemaMessage);
            }
            JSONArray allValidValuesFromVnfd = validValues.getJSONArray(VALID_VALUES_KEY);
            if (allValidValuesFromVnfd.length() > 2 || allValidValuesFromVnfd.length() < 1) {
                throw new IllegalArgumentException(invalidEntrySchemaMessage);
            }
            validateValidValuesConstraintsForExtensionProperty(actualValidValues, allValidValuesFromVnfd, invalidEntrySchemaMessage);
        } catch (JSONException je) {
            throw new IllegalArgumentException(invalidEntrySchemaMessage, je);
        }
    }

    private static void validateValidValuesConstraintsForExtensionProperty(Collection<String> actualValidValues,
                                                                           JSONArray allValidValuesFromVnfd,
                                                                           String exceptionMessage) {
        for (int validValueIndex = 0; validValueIndex < allValidValuesFromVnfd.length(); validValueIndex++) {
            String value = allValidValuesFromVnfd.getString(validValueIndex);
            if (!actualValidValues.contains(value)) {
                throw new IllegalArgumentException(exceptionMessage);
            }
        }
    }
}
