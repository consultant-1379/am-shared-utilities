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

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import com.ericsson.am.shared.vnfd.model.LcmOperationsConfiguration;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryJsonAttributeAsString;
import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryProperty;
import static com.ericsson.am.shared.vnfd.CommonUtility.getPojo;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeString;
import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_NOT_PROVIDED_FOR_INPUTS_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_NOT_DEFINED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_VALUE_NOT_SUPPORTED;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INSTANTIATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INSTANTIATE_USECASE_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACE_TYPE_NOT_DEFINED;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_HEAL_CAUSES;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_INPUT_PROVIDED_FOR_INPUTS;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_LCM_OPERATIONS_HEAL;
import static com.ericsson.am.shared.vnfd.utils.Constants.LCM_OPERATIONS_CONFIGURATION;
import static com.ericsson.am.shared.vnfd.utils.Constants.LCM_OPERATIONS_MANDATORY;
import static com.ericsson.am.shared.vnfd.utils.Constants.MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_INTERFACES_NFV_VNFLCM_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_NODES_NFV_VNF_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_NOT_DEFINED_FOR_ADDITIONAL_PARAMETERS;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE;

public final class NodeTypeUtility {

    private static final Validator VALIDATOR;

    static {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }


    private NodeTypeUtility() {
    }

    public static NodeType buildNodeType(final JSONObject vnfd) {
        Map<String, DataType> dataTypes = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
        Map<String, InterfaceType> interfaceTypes = InterfaceTypeUtility.getInterfaceTypeFromVnfd(vnfd, dataTypes);
        return buildNodeType(vnfd, dataTypes, interfaceTypes);
    }

    public static NodeType buildNodeType(final JSONObject vnfd,
                                         final Map<String, DataType> allDataType,
                                         final Map<String, InterfaceType> allInterfaceTypes) {
        JSONObject nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);

        Pair<String, JSONObject> nodeTypeInfo = getNodeTypeFromVnfd(vnfd);
        String nodeTypeName = nodeTypeInfo.getLeft();
        JSONObject nodeTypeDetails = nodeTypeInfo.getRight();

        NodeProperties nodeProperties = VnfdUtility.validateAndGetNodeProperties(nodeTypes);
        List<VnfmLcmInterface> interfacesList = buildInterfacesList(nodeTypeDetails, allDataType);
        Map<String, InterfaceTypeImpl> customInterface = buildCustomInterfaces(nodeTypeDetails, allInterfaceTypes);

        NodeType nodeType = new NodeType();
        nodeType.setType(nodeTypeName);
        nodeType.setNodeProperties(nodeProperties);
        nodeType.setArtifacts(CommonUtility.getArtifacts(nodeTypes));
        nodeType.setInterfaces(interfacesList);
        nodeType.setCustomInterface(customInterface);
        return nodeType;
    }

    public static void validateNodeTypeDerivedFromValue(final JSONObject nodeType, final String nodeTypeName) {
        if (!hasPropertyOfTypeString(nodeType, DERIVED_FROM_KEY)) {
            throw new IllegalArgumentException(String.format(DERIVED_FROM_NOT_DEFINED_ERROR_MESSAGE, nodeTypeName));
        }
        String derivedFromValue = getMandatoryJsonAttributeAsString(nodeType, DERIVED_FROM_KEY, NODE_TYPES_KEY);
        if (!derivedFromValue.equals(TOSCA_NODES_NFV_VNF_TYPE)) {
            throw new IllegalArgumentException(DERIVED_FROM_VALUE_NOT_SUPPORTED);
        }
    }

    public static boolean isOperationDefinedInVnfLcmInterface(final JSONObject vnfd, final VnfmLcmInterface.Type operation) {
        NodeType nodeType = buildNodeType(vnfd);
        return nodeType.getInterfaces().stream().anyMatch(vnfmLcmInterface -> vnfmLcmInterface.getType() == operation);
    }

    public static void validateVnflcmInterface(final JSONObject vnflcmInterface,
                                               final Map<String, DataType> allDataType) {
        String interfaceTypeName = getTypeNameFromNodeTypeInterface(vnflcmInterface);
        if (!interfaceTypeName.equals(TOSCA_INTERFACES_NFV_VNFLCM_TYPE)) {
            throw new IllegalArgumentException(VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE);
        }

        JSONObject interfaceOperations = vnflcmInterface;
        if (vnflcmInterface.has(OPERATIONS_KEY)) {
            interfaceOperations = vnflcmInterface.getJSONObject(OPERATIONS_KEY);
        }

        validateInstantiateOperationIsPresent(interfaceOperations);
        validateAdditionalPropertiesForOperations(interfaceOperations, allDataType);
    }


    public static boolean nodeTypeHasInterfaceDerivedFromToscaInterfacesNfvChangeCurrentVnfPackage(JSONObject vnfd) {
        Map<String, DataType> dataTypes = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
        Map<String, InterfaceType> interfaceTypes = InterfaceTypeUtility.getInterfaceTypeFromVnfd(vnfd, dataTypes);
        NodeType nodeType = buildNodeType(vnfd, dataTypes, interfaceTypes);
        for (InterfaceTypeImpl customInterface : nodeType.getCustomInterface().values()) {
            String customInterfaceTypeName = customInterface.getInterfaceType();
            if (interfaceTypes.containsKey(customInterfaceTypeName)) {
                InterfaceType interfaceType = interfaceTypes.get(customInterfaceTypeName);
                if (interfaceType.getDerivedFrom().equals(TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Pair<String, JSONObject> getNodeTypeFromVnfd(final JSONObject vnfd) {
        JSONObject nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);
        validateVnfdContainsSingleNodeType(nodeTypes);

        String nodeTypeName = nodeTypes.keys().next();
        JSONObject nodeType = nodeTypes.getJSONObject(nodeTypeName);
        validateNodeTypeDerivedFromValue(nodeType, nodeTypeName);
        return Pair.of(nodeTypeName, nodeType);
    }

    private static List<VnfmLcmInterface> buildInterfacesList(final JSONObject nodeType,
                                                              final Map<String, DataType> dataTypes) {
        List<VnfmLcmInterface> interfaceList = new ArrayList<>();
        JSONObject interfaces = getMandatoryProperty(nodeType, INTERFACES_KEY);
        if (!interfaces.has(VNFLCM)) {
            throw new IllegalArgumentException(VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE);
        }

        JSONObject vnflcmInterface = getMandatoryProperty(interfaces, VNFLCM);
        validateVnflcmInterface(vnflcmInterface, dataTypes);

        CommonUtility.addVnfmInterface(interfaceList, vnflcmInterface);
        return interfaceList;
    }

    private static Map<String, InterfaceTypeImpl> buildCustomInterfaces(final JSONObject nodeType,
                                                                        final Map<String, InterfaceType> interfaceTypes) {
        Map<String, InterfaceTypeImpl> customInterfaces = new HashMap<>();
        JSONObject interfaces = getMandatoryProperty(nodeType, INTERFACES_KEY);
        for (String interfaceName : interfaces.keySet()) {
            JSONObject interfaceDetails = interfaces.getJSONObject(interfaceName);
            String interfaceTypeName = getTypeNameFromNodeTypeInterface(interfaceDetails);

            if (!interfaceTypeName.equals(TOSCA_INTERFACES_NFV_VNFLCM_TYPE)) {
                validateInterfaceTypeIsPresent(interfaceTypes, interfaceTypeName);
                InterfaceType interfaceType = interfaceTypes.get(interfaceTypeName);
                InterfaceTypeImpl interfaceTypeImpl = buildInterfaceTypeImpl(interfaceType, interfaceTypeName);
                customInterfaces.put(interfaceName, interfaceTypeImpl);
            }
        }
        return customInterfaces;
    }

    private static InterfaceTypeImpl buildInterfaceTypeImpl(final InterfaceType interfaceType,
                                                            final String interfaceTypeName) {
        InterfaceTypeImpl interfaceTypeImpl = new InterfaceTypeImpl();
        interfaceTypeImpl.setInterfaceType(interfaceTypeName);
        interfaceTypeImpl.setDerivedFrom(interfaceType.getDerivedFrom());
        interfaceTypeImpl.setOperation(interfaceType.getOperation());
        return interfaceTypeImpl;
    }

    private static String getTypeNameFromNodeTypeInterface(final JSONObject interfaceDetails) {
        String vnfdSectionName = String.format("node type interface %s", interfaceDetails);
        return getMandatoryJsonAttributeAsString(interfaceDetails, TYPE_KEY, vnfdSectionName);
    }

    private static void validateInterfaceTypeIsPresent(final Map<String, InterfaceType> interfaceTypes,
                                                       final String interfaceTypeName) {
        if (!interfaceTypes.containsKey(interfaceTypeName)) {
            throw new IllegalArgumentException(String.format(INTERFACE_TYPE_NOT_DEFINED, interfaceTypeName));
        }
    }

    private static void validateVnfdContainsSingleNodeType(final JSONObject nodeTypes) {
        if (nodeTypes.keySet().isEmpty()) {
            throw new IllegalArgumentException(NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE);
        }
        if (nodeTypes.keySet().size() > 1) {
            throw new IllegalArgumentException(MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE);
        }
    }

    private static void validateInstantiateOperationIsPresent(final JSONObject interfaceOperations) {
        boolean isInstantiateKeyPresent = interfaceOperations.keySet().contains(INSTANTIATE_KEY);
        if (!isInstantiateKeyPresent) {
            throw new IllegalArgumentException(INSTANTIATE_USECASE_NOT_PRESENT_ERROR_MESSAGE);
        }
    }

    private static void validateAdditionalPropertiesForOperations(final JSONObject operationsList,
                                                                  final Map<String, DataType> allDataType) {
        for (String operationName : operationsList.keySet()) {
            boolean operationIsExtended = operationsList.get(operationName) instanceof JSONObject
                    && !operationsList.getJSONObject(operationName).toMap().isEmpty();

            if (operationIsExtended) {
                JSONObject operationDetails = operationsList.getJSONObject(operationName);
                validateInputsForOperation(operationDetails, operationName, allDataType);
            }
        }
    }

    private static void validateInputsForOperation(final JSONObject operationDetails, final String operationName,
                                                   final Map<String, DataType> allDataType) {
        if (hasPropertyOfTypeJsonObject(operationDetails, INPUTS_KEY)) {
            JSONObject operationInputs = operationDetails.getJSONObject(INPUTS_KEY);
            validateAdditionalParametersForOperation(operationInputs, operationName, allDataType);
        } else {
            throw new IllegalArgumentException(String.format(INVALID_INPUT_PROVIDED_FOR_INPUTS, operationName));
        }
    }

    private static void validateAdditionalParametersForOperation(final JSONObject operationInputs,
                                                                 final String operationName,
                                                                 final Map<String, DataType> allDataType) {
        if (hasPropertyOfTypeJsonObject(operationInputs, ADDITIONAL_PARAMETERS_KEY)) {
            JSONObject additionalParameterType = operationInputs.getJSONObject(ADDITIONAL_PARAMETERS_KEY);
            if (hasPropertyOfTypeString(additionalParameterType, TYPE_KEY)) {
                validateDataTypeIsDefined(allDataType, additionalParameterType);
            } else {
                throw new IllegalArgumentException(String.format(TYPE_NOT_DEFINED_FOR_ADDITIONAL_PARAMETERS, operationName));
            }
        } else {
            throw new IllegalArgumentException(String.format(ADDITIONAL_PARAMETERS_NOT_PROVIDED_FOR_INPUTS_ERROR_MESSAGE, operationName));
        }
    }

    private static void validateDataTypeIsDefined(final Map<String, DataType> allDataType,
                                                  final JSONObject additionalParameterType) {
        String additionalParametersTypeName = additionalParameterType.getString(TYPE_KEY);
        if (!allDataType.containsKey(additionalParametersTypeName)) {
            throw new IllegalArgumentException(
                    String.format(DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE, additionalParametersTypeName));
        }
    }


    public static void validateHealConfiguration(final JSONObject vnfd) {
        JSONObject nodeType = getNodeTypeFromVnfd(vnfd).getRight();
        LcmOperationsConfiguration lcmOperationsConfiguration = buildLcmOperationConfiguration(nodeType);
        boolean noHealCausesDefined = lcmOperationsConfiguration.getDefaultConfiguration()
                .getHealConfiguration().getCauses().isEmpty();
        if (noHealCausesDefined) {
            throw new IllegalArgumentException("No heal causes defined in Vnfd");
        }
    }

    // used in Orchestrator service when HEAL request is triggered
    public static void validateHealCauseIsSupported(final JSONObject vnfd, final String cause) {
        JSONObject nodeType = getNodeTypeFromVnfd(vnfd).getRight();
        LcmOperationsConfiguration lcmOperationsConfiguration = buildLcmOperationConfiguration(nodeType);
        List<String> healCauses = lcmOperationsConfiguration.getDefaultConfiguration().getHealConfiguration().getCauses();
        if (CollectionUtils.isEmpty(healCauses) || !StringUtils.containsIgnoreCase(healCauses.toString(), cause)) {
            throw new IllegalArgumentException(String.format(INVALID_HEAL_CAUSES, cause));
        }
    }

    private static LcmOperationsConfiguration buildLcmOperationConfiguration(final JSONObject nodeType) {
        JSONObject properties = getMandatoryProperty(nodeType, PROPERTIES_KEY);
        validateNodePropertiesHaveLcmOperationConfiguration(properties);
        JSONObject lcmOperationConfiguration = properties.getJSONObject(LCM_OPERATIONS_CONFIGURATION);

        LcmOperationsConfiguration lcmOperationsConfiguration
                = getPojo(lcmOperationConfiguration, LcmOperationsConfiguration.class);
        final Set<ConstraintViolation<LcmOperationsConfiguration>> violations
                = VALIDATOR.validate(lcmOperationsConfiguration);
        if (!CollectionUtils.isEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }

        validateHealConfigurationIsPresent(lcmOperationsConfiguration);
        return lcmOperationsConfiguration;
    }

    private static void validateNodePropertiesHaveLcmOperationConfiguration(final JSONObject nodeProperties) {
        if (!hasPropertyOfTypeJsonObject(nodeProperties, LCM_OPERATIONS_CONFIGURATION)) {
            throw new IllegalArgumentException(LCM_OPERATIONS_MANDATORY);
        }
    }

    private static void validateHealConfigurationIsPresent(LcmOperationsConfiguration lcmOperationsConfiguration) {
        boolean isHealConfigurationPresent = lcmOperationsConfiguration != null
                && lcmOperationsConfiguration.getDefaultConfiguration() != null
                && lcmOperationsConfiguration.getDefaultConfiguration().getHealConfiguration() != null
                && lcmOperationsConfiguration.getDefaultConfiguration().getHealConfiguration().getCauses() != null;
        if (!isHealConfigurationPresent) {
            throw new IllegalArgumentException(INVALID_LCM_OPERATIONS_HEAL);
        }
    }
}
