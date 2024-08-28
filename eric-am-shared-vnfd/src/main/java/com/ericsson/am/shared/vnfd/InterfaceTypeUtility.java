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

import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryProperty;
import static com.ericsson.am.shared.vnfd.CommonUtility.getOptionalPropertyAsJsonObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.getPropertyAsString;
import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DESCRIPTION_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.METADATA_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VERSION_KEY;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.model.CustomInterfaceInputs;
import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.DataTypeImpl;
import com.ericsson.am.shared.vnfd.model.Input;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.CustomOperation;

public final class InterfaceTypeUtility {

    private InterfaceTypeUtility() {
    }

    public static Map<String, InterfaceType> getInterfaceTypeFromVnfd(JSONObject vnfd, Map<String, DataType> dataType) {
        Optional<JSONObject> interfaceTypes = getOptionalPropertyAsJsonObject(vnfd, INTERFACE_TYPES_KEY);
        Map<String, InterfaceType> interfaceTypeMap = new HashMap<>();
        if (interfaceTypes.isEmpty()) {
            return interfaceTypeMap;
        }

        for (String interfaceTypeKey : interfaceTypes.get().keySet()) {
            InterfaceType interfaceType = buildInterfaceType(dataType, interfaceTypes.get(), interfaceTypeKey);
            interfaceTypeMap.put(interfaceTypeKey, interfaceType);
        }
        return interfaceTypeMap;
    }

    public static Map<String, Map<String, Object>> buildInterfacesOperationsInputsMap(Map<String, InterfaceTypeImpl> customInterfaces) {
        return customInterfaces.entrySet().stream()
                .map(customInterface -> new AbstractMap.SimpleEntry<>(customInterface.getKey(),
                        getOperationsInputsFromCustomInterface(customInterface.getValue())))
                .flatMap(operationsInputs -> operationsInputs.getValue().entrySet().stream()
                        .map(operationInput -> new AbstractMap.SimpleEntry<>(
                                buildInterfaceOperationName(operationsInputs.getKey(), operationInput.getKey()),
                                operationInput.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, Map<String, Object>> getOperationsInputsFromCustomInterface(InterfaceTypeImpl interfaceType) {
        return Optional.ofNullable(interfaceType)
                .map(InterfaceType::getOperation)
                .stream()
                .flatMap(operations -> operations.entrySet().stream())
                .map(operation -> new AbstractMap.SimpleEntry<>(operation.getKey(), Optional.ofNullable(operation.getValue())
                        .map(CustomOperation::getInput)
                        .filter(CustomInterfaceInputs.class::isInstance)
                        .map(input -> (CustomInterfaceInputs) input)
                        .map(CustomInterfaceInputs::getStaticAdditionalParams)
                        .orElse(Collections.emptyMap())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String buildInterfaceOperationName(String interfaceKey, String operationKey) {
        return String.join(".", interfaceKey, operationKey);
    }

    private static InterfaceType buildInterfaceType(Map<String, DataType> dataType, JSONObject interfaceTypeJson, String key) {
        JSONObject interfaceTypes = interfaceTypeJson.getJSONObject(key);

        InterfaceType interfaceType = new InterfaceType();
        interfaceType.setDerivedFrom(CommonUtility.getMandatoryJsonAttributeAsString(interfaceTypes, DERIVED_FROM_KEY, key));
        interfaceType.setDescription(CommonUtility.getOptionalJsonAttributeAsString(interfaceTypes, DESCRIPTION_KEY));
        interfaceType.setVersion(CommonUtility.getOptionalJsonAttributeAsString(interfaceTypes, VERSION_KEY));

        JSONObject operations = CommonUtility.getOptionalJsonAttribute(interfaceTypes, OPERATIONS_KEY);
        Map<String, CustomOperation> allOperations =
                getAllOperations(Objects.requireNonNullElse(operations, interfaceTypes), dataType);
        interfaceType.setOperation(allOperations);
        return interfaceType;
    }

    private static Map<String, CustomOperation> getAllOperations(JSONObject operations, Map<String, DataType> dataType) {
        //validate this in test, i.e. when operation definition is not present it should be valid
        Set<String> notOperationKeys = new HashSet<>(Arrays.asList(DERIVED_FROM_KEY, VERSION_KEY, METADATA_KEY, DESCRIPTION_KEY, INPUTS_KEY));
        return operations.keySet()
                .stream()
                .filter(operationKey -> !notOperationKeys.contains(operationKey))
                .collect(Collectors.toMap(operationKey -> operationKey, operationKey -> buildCustomOperation(dataType, operationKey, operations)));
    }

    private static CustomOperation buildCustomOperation(Map<String, DataType> dataType, String operationKey, JSONObject operations) {
        CustomOperation customOperation = new CustomOperation();
        if (!operations.isNull(operationKey)) {
            JSONObject operation = operations.getJSONObject(operationKey);
            customOperation.setDescription(CommonUtility.getOptionalJsonAttributeAsString(operation, DESCRIPTION_KEY));
            Input inputs = getInputs(
                    CommonUtility.getMandatoryJsonAttributeAsJSONObject(operation, INPUTS_KEY, operationKey), dataType, operationKey);
            customOperation.setInput(inputs);
        }
        return customOperation;
    }

    private static Input getInputs(JSONObject inputs, Map<String, DataType> allDataTypes, String operationName) {

        JSONObject inputsAdditionalParameters = getMandatoryProperty(inputs, ADDITIONAL_PARAMETERS_KEY);

        String dataTypeName = getPropertyAsString(operationName, inputsAdditionalParameters, TYPE_KEY);

        DataTypeImpl dataType = buildDataType(allDataTypes, dataTypeName);

        return new CustomInterfaceInputs(dataType);
    }

    private static DataTypeImpl buildDataType(Map<String, DataType> allDataTypes, String dataTypeName) {
        DataType dataType = allDataTypes.get(dataTypeName);
        if (Objects.isNull(dataType)) {
            throw new IllegalArgumentException(String.format(DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE,
                    dataTypeName));
        }
        DataTypeImpl dataTypeImplObject = new DataTypeImpl();
        dataTypeImplObject.setDataTypeName(dataTypeName);
        dataTypeImplObject.setDerivedFrom(dataType.getDerivedFrom());
        dataTypeImplObject.setProperties(dataType.getProperties());

        return dataTypeImplObject;
    }
}
