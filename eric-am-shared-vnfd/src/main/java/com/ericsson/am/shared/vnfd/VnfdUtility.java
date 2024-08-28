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

import static com.ericsson.am.shared.vnfd.HelmChartUtility.parseHelmCharts;
import static com.ericsson.am.shared.vnfd.NodeTypeUtility.buildNodeType;
import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_TYPE_VALUE_MUST_BE_OF_TYPE_STRING;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACT_KEYS;
import static com.ericsson.am.shared.vnfd.utils.Constants.ASSOCIATED_VDU_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEFAULT_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DESTINATION_DESCRIPTOR_ID_MUST_NOT_BE_EMPTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DOCKER_IMAGES_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.FILE_NOT_PROVIDED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.FLAVOUR_ID_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACE_DETAILS_NOT_PRESENT_IN_THE_NODE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_PROPERTY_MISSING_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NON_SCALABLE_VDU_MISSING_IN_VDU_INSTANTIATION_LEVELS;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.POLICIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.POLICY_TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ROLLBACK;
import static com.ericsson.am.shared.vnfd.utils.Constants.SCALING_MAPPING_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_INTERFACES_NFV_VNFLCM_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCSA_ARTIFACTS_NFV_SW_IMAGE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNKNOWN_OPERATION;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_NOT_MAP_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_NOT_PRESENT_IN_THE_PATH_SPECIFIED;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_TOSCA_DEFINITION_VERSION_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE;
import static java.lang.String.format;

import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryProperty;
import static com.ericsson.am.shared.vnfd.CommonUtility.getOptionalPropertyAsArray;
import static com.ericsson.am.shared.vnfd.CommonUtility.getOptionalPropertyAsJsonObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.getPojo;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.NestedMapUtility.castToNestedMap;
import static com.ericsson.am.shared.vnfd.NestedMapUtility.replaceValuesAfterKey;
import static com.ericsson.am.shared.vnfd.NodeTypeUtility.validateNodeTypeDerivedFromValue;
import static com.ericsson.am.shared.vnfd.PolicyUtility.WILDCARD_DESTINATION_UUID;
import static com.ericsson.am.shared.vnfd.model.policies.PolicyKeyEnum.VNF_PACKAGE_CHANGE_POLICY_TYPE;
import static com.ericsson.am.shared.vnfd.model.policies.parser.VnfPackageChangeParser.getVnfPackageChangePolicy;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.ericsson.am.shared.vnfd.model.ArtifactsPropertiesDetail;
import com.ericsson.am.shared.vnfd.model.CustomInterfaceInputs;
import com.ericsson.am.shared.vnfd.model.CustomOperation;
import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.ericsson.am.shared.vnfd.model.ImageDetails;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.ScaleMapping;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import com.ericsson.am.shared.vnfd.validation.vnfd.VnfdValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmPackage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCompute;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDeltas;
import com.ericsson.am.shared.vnfd.model.policies.VduInstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeSelector;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeTriggerTosca1dot2;
import com.ericsson.am.shared.vnfd.utils.Constants;
import com.ericsson.am.shared.vnfd.validation.vducp.VduCpValidator;
import com.ericsson.am.shared.vnfd.validation.virtualcp.VirtualCpValidator;
import com.ericsson.am.shared.vnfd.validation.vnfd.VnfdValidators;

public final class VnfdUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(VnfdUtility.class);

    private static final Validator VALIDATOR;

    private static final LoaderOptions NO_DUPLICATE_OPTIONS = new LoaderOptions();


    private VnfdUtility() {
    }

    static {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
        NO_DUPLICATE_OPTIONS.setAllowDuplicateKeys(false);
    }

    public static JSONObject validateYamlCanBeParsed(final Path vnfdPath) {
        Map<String, Object> map = validateYamlAndConvertToMap(vnfdPath);
        return convertYamlMapToJsonObject(map);
    }

    public static Map<String, Object> validateYamlAndConvertToMap(final Path vnfdPath) {
        final Yaml yaml = new Yaml(new SafeConstructor(NO_DUPLICATE_OPTIONS),
                new Representer(new DumperOptions()),
                new DumperOptions(),
                NO_DUPLICATE_OPTIONS);
        Object yamlObject;
        try (InputStream file = Files.newInputStream(vnfdPath)) {
            yamlObject = yaml.load(file);
        } catch (final NoSuchFileException fe) {
            throw new IllegalArgumentException(VNFD_NOT_PRESENT_IN_THE_PATH_SPECIFIED, fe);
        } catch (final Exception pe) {
            throw new IllegalArgumentException(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE, pe);
        }
        return getMapFromYamlObject(yamlObject);
    }

    public static JSONObject validateYamlAndConvertToJsonObject(String vnfd) {
        final Yaml yaml = new Yaml(new SafeConstructor(NO_DUPLICATE_OPTIONS),
                new Representer(new DumperOptions()),
                new DumperOptions(),
                NO_DUPLICATE_OPTIONS);
        Object yamlObject;
        try {
            yamlObject = yaml.load(vnfd);
        } catch (Exception e) {
            throw new IllegalArgumentException(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE, e);
        }
        if (yamlObject == null) {
            throw new IllegalArgumentException(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE);
        }
        Map<String, Object> mapFromYamlObject = getMapFromYamlObject(yamlObject);
        return convertYamlMapToJsonObject(mapFromYamlObject);
    }

    private static JSONObject convertYamlMapToJsonObject(Map<String, Object> mapFromYamlObject) {
        JSONObject jsonObject = new JSONObject(mapFromYamlObject);
        List<String> artifactKeys = getArtifactKeys(mapFromYamlObject);
        if (!artifactKeys.isEmpty() && (!jsonObject.has(ARTIFACT_KEYS))) {
            jsonObject.putOnce(ARTIFACT_KEYS, new JSONArray(artifactKeys));
        }
        return jsonObject;
    }

    public static void validateNodeType(JSONObject vnfd) {
        if (hasPropertyOfTypeJsonObject(vnfd, NODE_TYPES_KEY)) {
            JSONObject nodeTypes = vnfd.getJSONObject(NODE_TYPES_KEY);
            if (nodeTypes.keySet().isEmpty()) {
                throw new IllegalArgumentException(NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE);
            }
            if (nodeTypes.keySet().size() > 1) {
                throw new IllegalArgumentException(MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE);
            }
        } else {
            throw new IllegalArgumentException(NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE);
        }
    }

    private static NodeProperties validateConstrainViolationInNodeProperties(final JSONObject nodePropertiesData,
                                                                             final String nodeName) {

        if (hasPropertyOfTypeJsonObject(nodePropertiesData, PROPERTIES_KEY)) {
            NodeProperties properties = getPojo(nodePropertiesData.getJSONObject(PROPERTIES_KEY), NodeProperties.class);

            final Set<ConstraintViolation<NodeProperties>> violations = VALIDATOR.validate(properties);
            if (violations != null && !violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return properties;
        } else {
            throw new IllegalArgumentException(NODE_PROPERTY_MISSING_ERROR_MESSAGE + nodeName);
        }
    }

    public static VnfDescriptorDetails buildVnfDescriptorDetails(JSONObject vnfd) {
        List<String> artifactKeys = getArtifactKeys(vnfd);
        Optional<JSONObject> topologyTemplateJson = getOptionalPropertyAsJsonObject(vnfd, TOPOLOGY_TEMPLATE_KEY);
        if (topologyTemplateJson.isPresent()) {
            VnfdValidators.validateOrThrow(vnfd);
        }

        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        JSONObject dataTypes = getMandatoryProperty(vnfd, DATA_TYPES_KEY);
        JSONObject nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);

        List<HelmChart> helmCharts = parseHelmCharts(topologyTemplateJson, nodeTypes, dataTypes, artifactKeys);

        Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
        Map<String, InterfaceType> allInterfaceType = InterfaceTypeUtility.getInterfaceTypeFromVnfd(vnfd, allDataType);

        NodeType nodeType = NodeTypeUtility.buildNodeType(vnfd, allDataType, allInterfaceType);
        Flavour defaultFlavour = FlavourUtility.getDefaultFlavour(vnfd, nodeType);
        TopologyTemplate topologyTemplate = defaultFlavour.getTopologyTemplate();
        if (isRel4Vnfd(vnfd)) {
            validateVduInstantiationLevelsAreSpecifiedForAllNonScalableVdus(topologyTemplate);
        }

        VduCpValidator.validate(topologyTemplate);
        VirtualCpValidator.validate(topologyTemplate);

        PolicyUtility.validateVnfPackageChangePolicy(topologyTemplate);
        ChangeVnfPackagePatternUtility.validateRollbackPatterns(helmCharts, topologyTemplate.getNodeTemplate());
        List<ImageDetails> imagePath = getImagePath(nodeTypes, dataTypes);

        descriptorDetails.setImagesDetails(imagePath);
        descriptorDetails.setDefaultFlavour(defaultFlavour);
        descriptorDetails.setAllDataTypes(allDataType);
        descriptorDetails.setAllInterfaceTypes(allInterfaceType);
        descriptorDetails.setHelmCharts(helmCharts);
        return descriptorDetails;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getArtifactKeys(JSONObject vnfd) {
        List<String> artifactKeys = new ArrayList<>();
        if (vnfd.has(ARTIFACT_KEYS) && vnfd.getJSONArray(ARTIFACT_KEYS) != null) {
            List<String> artifactKeysByKey = vnfd.getJSONArray(ARTIFACT_KEYS).toList()
                    .stream().map(Object::toString).collect(Collectors.toList());
            artifactKeys.addAll(artifactKeysByKey);
        }
        return artifactKeys;
    }

    public static List<ImageDetails> getImagePath(JSONObject nodeTypes, JSONObject dataTypes) {
        for (String nodeTypeName : nodeTypes.keySet()) {
            JSONObject nodeTypeDetails = getMandatoryProperty(nodeTypes, nodeTypeName);
            validateNodeTypeInterfaces(nodeTypeDetails, dataTypes);
            JSONObject artifacts = getMandatoryProperty(nodeTypeDetails, ARTIFACTS_KEY);

            String imageLocation = getImageLocationFromArtifacts(artifacts);

            if (!imageLocation.isEmpty()) {
                return Collections.singletonList(new ImageDetails(imageLocation));
            }
        }
        throw new IllegalArgumentException(DOCKER_IMAGES_NOT_PRESENT);
    }

    public static String getScalingMappingFileArtifactPathFromVNFD(JSONObject vnfd) {

        final JSONObject nodeTypeJsonObject = getMandatoryProperty(vnfd, NODE_TYPES_KEY);
        final JSONObject artifactsJsonObject = getMandatoryProperty(nodeTypeJsonObject.getJSONObject(nodeTypeJsonObject.keySet()
                .iterator()
                .next()), ARTIFACTS_KEY);
        if (hasPropertyOfTypeJsonObject(artifactsJsonObject, SCALING_MAPPING_KEY)) {
            return getPojo(artifactsJsonObject.getJSONObject(SCALING_MAPPING_KEY), ArtifactsPropertiesDetail.class).getFile();
        }

        return null;
    }



    public static boolean isRel4Vnfd(JSONObject vndf) {
        Optional<JSONObject> topologyTemplates = CommonUtility.getOptionalPropertyAsJsonObject(vndf, TOPOLOGY_TEMPLATE_KEY);
        if (topologyTemplates.isPresent() && nodeTemplateExists(topologyTemplates.get())) {
            JSONObject nodeTemplates = topologyTemplates.get().getJSONObject(NODE_TEMPLATES_KEY);

            return nodeTemplatesHasNodeType(nodeTemplates);
        }

        return false;
    }

    private static void validateVduInstantiationLevelsAreSpecifiedForAllNonScalableVdus(TopologyTemplate topologyTemplate) {
        final NodeTemplate nodeTemplate = topologyTemplate.getNodeTemplate();
        final Policies policies = topologyTemplate.getPolicies();

        Set<String> vdus = nodeTemplate.getOsContainerDeployableUnit().stream()
                .map(VduCompute::getVduComputeKey)
                .collect(Collectors.toSet());

        Set<String> scalableVdus = getScalableVdusNames(policies);
        vdus.removeAll(scalableVdus);

        Map<String, VduInstantiationLevels> vduInstantiationLevels = policies.getAllVduInstantiationLevels();
        List<String> vdusFromVduInstantiationLevels = vduInstantiationLevels
                .values().stream()
                .map(VduInstantiationLevels::getTargets)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        vdusFromVduInstantiationLevels.forEach(vdus::remove); // non scalable VDU-s which not listed in VduInstantiationLevels

        if (vdus.isEmpty()) {
            return;
        }

        throw new IllegalArgumentException(format(NON_SCALABLE_VDU_MISSING_IN_VDU_INSTANTIATION_LEVELS, vdus));
    }

    private static Set<String> getScalableVdusNames(final Policies policies) {
        return policies.getAllScalingAspectDelta()
                .values().stream()
                .map(ScalingAspectDeltas::getTargets)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }

    private static boolean nodeTemplatesHasNodeType(JSONObject nodeTemplates) {
        for (String key : nodeTemplates.keySet()) {
            JSONObject nodeDetails = nodeTemplates.getJSONObject(key);

            if (nodeDetails.has(TYPE_KEY) && Constants.MCIOP_NODE.equals(nodeDetails.get(TYPE_KEY))) {
                return true;
            }
        }
        return false;
    }

    public static boolean nodeTemplateExists(final JSONObject topologyTemplate) {
        return hasPropertyOfTypeJsonObject(topologyTemplate, NODE_TEMPLATES_KEY);
    }

    public static void validateInterfaces(final JSONObject interfaces, final JSONObject dataTypesDetails) {
        Optional<JSONObject> vnflcmInterfaceOpt = getVnflcmInterface(interfaces);
        if (vnflcmInterfaceOpt.isEmpty()) {
            throw new IllegalArgumentException(VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE);
        }
        JSONObject vnflcmInterface = vnflcmInterfaceOpt.get();

        Map<String, DataType> dataTypes;
        if (dataTypesDetails == null) {
            dataTypes = new HashMap<>();
        } else {
            dataTypes = DataTypeUtility.buildDataTypes(dataTypesDetails);
        }
        NodeTypeUtility.validateVnflcmInterface(vnflcmInterface, dataTypes);
    }

    private static Optional<JSONObject> getVnflcmInterface(JSONObject interfaces) {
        JSONObject tempInterface;

        for (String key : interfaces.keySet()) {
            if (!(interfaces.get(key) instanceof JSONObject)) {
                continue;
            }

            tempInterface = interfaces.getJSONObject(key);
            if (isVnflcmTypeInterface(tempInterface)) {
                return Optional.of(tempInterface);
            }
        }
        return Optional.empty();
    }

    private static boolean isVnflcmTypeInterface(JSONObject vnfInterface) {
        return vnfInterface.has(TYPE_KEY) && vnfInterface.get(TYPE_KEY).equals(TOSCA_INTERFACES_NFV_VNFLCM_TYPE);
    }

    public static JSONObject getVnfdInterfaceDetails(JSONObject interfaceDetails) {
        JSONObject vnfdInterfaceDetails = interfaceDetails;
        if (interfaceDetails.has(OPERATIONS_KEY)) {
            vnfdInterfaceDetails = interfaceDetails.getJSONObject(OPERATIONS_KEY);
        }
        return vnfdInterfaceDetails;
    }



    public static ArtifactsPropertiesDetail collectAndGetArtifactsPropertiesDetails(final JSONObject artifacts, final String key) {
        ArtifactsPropertiesDetail artifactsProperties = validateAndGetArtifactPropertiesDetails(key, artifacts);
        //Do not move below validation to Pojo class as artifacts can have different attributes without the file
        //We would only be checking the file attribute for helm and image attribute
        validateFileAttributeProvided(artifactsProperties, key);
        return artifactsProperties;
    }

    public static void validateFileAttributeProvided(ArtifactsPropertiesDetail artifactsProperties, String key) {
        if (StringUtils.isEmpty(artifactsProperties.getFile())) {
            throw new IllegalArgumentException(FILE_NOT_PROVIDED_ERROR_MESSAGE + key);
        }
    }

    private static String getImageLocationFromArtifacts(final JSONObject artifacts) {
        String dockerFilePath = null;
        for (String key : artifacts.keySet()) {
            if (artifacts.get(key) instanceof JSONObject) {
                ArtifactsPropertiesDetail artifactsProperties = validateAndGetArtifactPropertiesDetails(key, artifacts);
                if (artifactsProperties.getType().equals(TOSCSA_ARTIFACTS_NFV_SW_IMAGE_TYPE)) {
                    dockerFilePath = artifactsProperties.getFile();
                }
            }
        }
        if (StringUtils.isEmpty(dockerFilePath)) {
            throw new IllegalArgumentException(DOCKER_IMAGES_NOT_PRESENT);
        } else {
            return dockerFilePath;
        }
    }

    static ArtifactsPropertiesDetail validateAndGetArtifactPropertiesDetails(final String key, final JSONObject artifacts) {
        ArtifactsPropertiesDetail artifactsProperties = getPojo(artifacts.
                getJSONObject(key), ArtifactsPropertiesDetail.class);
        final Set<ConstraintViolation<ArtifactsPropertiesDetail>> violations = VALIDATOR.
                validate(artifactsProperties);
        if (violations != null && !violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return artifactsProperties;
    }

    static Optional<JSONObject> getTopologyTemplate(JSONObject vnfd) {
        if (hasPropertyOfTypeJsonObject(vnfd, TOPOLOGY_TEMPLATE_KEY)) {
            return Optional.of(vnfd.getJSONObject(TOPOLOGY_TEMPLATE_KEY));
        }
        return Optional.empty();
    }

    public static NodeProperties validateAndGetNodeProperties(JSONObject nodeTypes) {
        NodeProperties properties = null;
        for (String nodeTypeName : nodeTypes.keySet()) {
            if (nodeTypes.get(nodeTypeName) instanceof JSONObject) {
                JSONObject nodeType = nodeTypes.getJSONObject(nodeTypeName);
                validateNodeTypeDerivedFromValue(nodeType, nodeTypeName);
                properties = validateConstrainViolationInNodeProperties(nodeType, nodeTypeName);
                properties.setValidFlavourIds(CommonUtility.getValidFlavours(properties));
            }
        }
        return properties;
    }

    static boolean checkKeyExists(final JSONObject jsonObject, String keyToCheck) {
        return jsonObject != null && jsonObject.keySet().contains(keyToCheck);
    }

    public static boolean isNestedVnfd(JSONObject vnfd) {
        Optional<JSONObject> topologyTemplateOpt = getTopologyTemplate(vnfd);
        if (topologyTemplateOpt.isEmpty()) {
            return false;
        }
        JSONObject topologyTemplate = topologyTemplateOpt.get();

        if (hasPropertyOfTypeJsonObject(topologyTemplate, NODE_TEMPLATES_KEY)) {
            JSONObject nodeTemplate = topologyTemplate.getJSONObject(NODE_TEMPLATES_KEY);
            for (String key : nodeTemplate.keySet()) {
                if (hasPropertyOfTypeJsonObject(nodeTemplate.getJSONObject(key), PROPERTIES_KEY) &&
                        nodeTemplate.getJSONObject(key).getJSONObject(PROPERTIES_KEY)
                                .keySet().contains(FLAVOUR_ID_KEY)) {
                    return true;
                }
            }
        }
        return false;
    }

    static void validateNodeTypeInterfaces(JSONObject nodeTypeDetails, JSONObject dataType) {
        if (hasPropertyOfTypeJsonObject(nodeTypeDetails, INTERFACES_KEY)) {
            validateInterfaces(nodeTypeDetails.getJSONObject(INTERFACES_KEY), dataType);
        } else {
            throw new IllegalArgumentException(INTERFACE_DETAILS_NOT_PRESENT_IN_THE_NODE_TYPE);
        }
    }

    public static JSONObject getAdditionalParametersFromVnfd(JSONObject vnfd, String operationType, String destinationDescriptorId) {

        Objects.requireNonNull(operationType, "Operation type must not be empty");
        List<String> supportedOperationsNames = Arrays.stream(LCMOperationsEnum.values()).map(LCMOperationsEnum::getOperation)
                .collect(Collectors.toList());
        if (!supportedOperationsNames.contains(operationType)) {
            throw new IllegalArgumentException(format(UNKNOWN_OPERATION, operationType));
        }
        Optional<String> additionalParametersDataTypeName;
        if (operationType.equals(ROLLBACK)) {
            if (StringUtils.isEmpty(destinationDescriptorId)) {
                throw new IllegalArgumentException(DESTINATION_DESCRIPTOR_ID_MUST_NOT_BE_EMPTY);
            }
            additionalParametersDataTypeName = getAdditionalParameterTypeFromPoliciesInterfaceType(vnfd, destinationDescriptorId);
        } else {
            additionalParametersDataTypeName = getAdditionalParameterTypeFromNodeTypeInterfaces(vnfd, operationType);
        }
        if (additionalParametersDataTypeName.isEmpty()) {
            // additional parameters are not defined in VNFD
            // so an empty list is returned
            return new JSONObject("{}");
        }
        return getAdditionalParametersFromDataType(vnfd, additionalParametersDataTypeName.get());
    }

    private static JSONObject getAdditionalParametersFromDataType(final JSONObject vnfd, final String dataTypeName) {
        try {
            JSONObject dataTypes = getMandatoryProperty(vnfd, DATA_TYPES_KEY);
            JSONObject additionalParametersDataType = getMandatoryProperty(dataTypes, dataTypeName);
            return getMandatoryProperty(additionalParametersDataType, PROPERTIES_KEY);
        } catch (IllegalArgumentException vpe) {
            throw new IllegalArgumentException(format(
                    "Failed to parse additional parameters from %s data type: %s", dataTypeName, vpe.getMessage()), vpe);
        }
    }

    public static Optional<String> getAdditionalParameterTypeFromPoliciesInterfaceType(
            final JSONObject vnfd, final String destinationDescriptorId) {
        // additional parameters type for rollback can be loaded from interface_types section of VNFD
        // using interface name and operation name, example:
        //
        // interface_types:
        //  ericsson.interfaces.nfv.EricssonChangeCurrentVnfPackage:
        //    rollback-operation-from-multi-b-to-multi-a:
        //      description: operation for change from b to a
        //      inputs:
        //        additional_parameters:
        //          type: ericsson.datatypes.nfv.VnfChangeToVersion1AdditionalParameters
        //  ...
        Pair<String, String> interfaceNameAndOperationName =
                getChangePackageInterfaceNameAndOperationName(vnfd, destinationDescriptorId);
        String interfaceName = interfaceNameAndOperationName.getLeft();
        String operationName = interfaceNameAndOperationName.getRight();

        if (StringUtils.isNotEmpty(interfaceName) && StringUtils.isNotEmpty(operationName)) {
            String interfaceTypeName = getInterfaceTypeName(vnfd, interfaceName);

            Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
            Map<String, InterfaceType> allInterfaceTypes = InterfaceTypeUtility.getInterfaceTypeFromVnfd(vnfd, allDataType);

            boolean canLoadAdditionalParameterTypeName = StringUtils.isNotEmpty(interfaceTypeName)
                    && !CollectionUtils.isEmpty(allInterfaceTypes);
            if (canLoadAdditionalParameterTypeName) {
                return getAdditionalParametersTypeName(allInterfaceTypes, interfaceTypeName, operationName);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> getAdditionalParametersTypeName(final Map<String, InterfaceType> allInterfaceType,
                                                                    final String changePackageInterfaceTypeName,
                                                                    final String changePackagePolicyOperation) {
        if (!allInterfaceType.containsKey(changePackageInterfaceTypeName)) {
            throw new IllegalArgumentException(format(
                    "Could not find interface matching %s", changePackageInterfaceTypeName));
        }
        InterfaceType changePackageInterfaceType = allInterfaceType.get(changePackageInterfaceTypeName);
        Map<String, CustomOperation> operations = changePackageInterfaceType.getOperation();

        if (!operations.containsKey(changePackagePolicyOperation)) {
            throw new IllegalArgumentException(format("Interface %s does not contain operation with name %s",
                    changePackageInterfaceType, changePackagePolicyOperation));
        }
        CustomInterfaceInputs customInterfaceInputs = (CustomInterfaceInputs) operations
                .get(changePackagePolicyOperation).getInput();
        return Optional.of(customInterfaceInputs.getAdditionalParams().getDataTypeName());
    }

    private static Pair<String, String> getChangePackageInterfaceNameAndOperationName(
            final JSONObject vnfd, final String destinationDescriptorId) {
        String vnfdVersion = vnfd.getString(VNFD_TOSCA_DEFINITION_VERSION_KEY);
        final JSONObject topologyTemplate = getMandatoryProperty(vnfd, TOPOLOGY_TEMPLATE_KEY);
        final Optional<JSONArray> allPoliciesOptional = getOptionalPropertyAsArray(topologyTemplate, POLICIES_KEY);
        if (allPoliciesOptional.isEmpty()) {
            return Pair.of(null, null);
        }

        Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy =
                getAllVnfChangePackagePolicies(allPoliciesOptional.get(), vnfdVersion);
        if (allVnfPackageChangePolicy.isEmpty()) {
            throw new IllegalArgumentException(format(
                    "Cannot parse additional parameters for rollback operation with destination descriptor id %s " +
                            "because no tosca.policies.nfv.VnfPackageChange policies are been defined in VNFD",
                    destinationDescriptorId));
        }

        Pair<String, String> result = getChangePackageInterfaceNameAndOperationNameFromTriggers(
                allVnfPackageChangePolicy, destinationDescriptorId);

        if (areTriggersNotDefined(result)) {
            return getChangePackageInterfaceNameAndOperationNameIfTriggersAreNotDefined(vnfd, destinationDescriptorId, allVnfPackageChangePolicy);
        }
        return result;
    }

    private static boolean areTriggersNotDefined(Pair<String, String> result) {
        return StringUtils.isEmpty(result.getLeft()) || StringUtils.isEmpty(result.getRight());
    }

    private static Pair<String, String> getChangePackageInterfaceNameAndOperationNameFromTriggers(
            final Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy,
            final String destinationDescriptorId) {

        Optional<VnfPackageChangePolicyTosca1dot2> rollbackToDestinationDescriptorIdPolicy =
                getVnfPackageChangePolicyWithMatchingTargetDescriptor(allVnfPackageChangePolicy, destinationDescriptorId);
        Optional<VnfPackageChangePolicyTosca1dot2> rollbackToAnyPackagePolicy =
                getVnfPackageChangePolicyWithMatchingTargetDescriptor(allVnfPackageChangePolicy, WILDCARD_DESTINATION_UUID);

        if (rollbackToDestinationDescriptorIdPolicy.isPresent()) {
            return getChangePackageInterfaceNameAndOperationNameFromPolicyTriggers(
                    rollbackToDestinationDescriptorIdPolicy.get());
        } else if (rollbackToAnyPackagePolicy.isPresent()) {
            return getChangePackageInterfaceNameAndOperationNameFromPolicyTriggers(
                    rollbackToAnyPackagePolicy.get());
        } else {
            throw new IllegalArgumentException(format(
                    "Could not find a matching rollback policy for destination descriptor id %s",
                    destinationDescriptorId));
        }
    }

    private static Pair<String, String> getChangePackageInterfaceNameAndOperationNameFromPolicyTriggers(
            final VnfPackageChangePolicyTosca1dot2 policy) {

        if (policy.getTriggers() == null) {
            return Pair.of(null, null);
        }

        Map<String, VnfPackageChangeTriggerTosca1dot2> triggersMap = policy.getTriggers().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rollback policy triggers list must not be empty"));
        VnfPackageChangeTriggerTosca1dot2 triggers = triggersMap.values().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rollback policy triggers must not be empty"));

        // split action into interface name and operation name
        // example: EricssonChangeCurrentVnfPackage.rollback-operation-from-multi-b-to-multi-a

        String changePackageInterfaceName = StringUtils.substringBefore(triggers.getAction(), ".");
        String operationName = StringUtils.substringAfter(triggers.getAction(), ".");
        return Pair.of(changePackageInterfaceName, operationName);
    }

    private static Pair<String, String> getChangePackageInterfaceNameAndOperationNameIfTriggersAreNotDefined(
            JSONObject vnfd, String destinationDescriptorId,
            Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {

        String operationName = getRollbackPolicyNameByDestinationDescriptorId(allVnfPackageChangePolicy, destinationDescriptorId)
                .orElseThrow(() -> new IllegalArgumentException(format(
                        "Could not find matching rollback policy for target descriptor id %s", destinationDescriptorId)));

        JSONObject interfaces = getAllInterfaces(vnfd);
        String interfaceTypeName = "ericsson.interfaces.nfv.ChangeCurrentVnfPackage";

        String rollbackInterfaceName = getInterfaceNameByInterfaceType(interfaces, interfaceTypeName)
                .orElseThrow(() -> new IllegalArgumentException(format(
                        "Could not interface definition for interface type %s", interfaceTypeName)));

        return Pair.of(rollbackInterfaceName, operationName);
    }

    private static Optional<String> getRollbackPolicyNameByDestinationDescriptorId(
            Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy,
            String destinationDescriptorId) {
        List<String> rollbackPolicies = allVnfPackageChangePolicy.keySet()
                .stream()
                .filter(policyName -> policyName.contains("rollback"))
                .collect(Collectors.toList());

        return rollbackPolicies.stream().filter(policy -> allVnfPackageChangePolicy
                        .get(policy)
                        .getProperties()
                        .getVnfPackageChangeSelectors()
                        .stream()
                        .map(VnfPackageChangeSelector::getDestinationDescriptorId)
                        .collect(Collectors.toSet())
                        .contains(destinationDescriptorId))
                .findFirst();
    }

    private static Optional<String> getInterfaceNameByInterfaceType(JSONObject interfaces, String interfaceType) {
        for (String interfaceName : interfaces.keySet()) {
            JSONObject anInterface = interfaces.getJSONObject(interfaceName);
            if (anInterface.has(TYPE_KEY)) {
                String anInterfaceType = anInterface.getString(TYPE_KEY);
                if (interfaceType.equals(anInterfaceType)) {
                    return Optional.of(interfaceName);
                }
            }
        }
        return Optional.empty();
    }

    private static String getInterfaceTypeName(final JSONObject vnfd, final String interfaceName) {
        JSONObject anInterface = getInterfaceFromVnfd(vnfd, interfaceName);
        return anInterface.getString(TYPE_KEY);
    }

    private static Map<String, VnfPackageChangePolicyTosca1dot2> getAllVnfChangePackagePolicies(
            final JSONArray allPolicies, String vnfdVersion) {
        final Map<String, VnfPackageChangePolicyTosca1dot2> result = new HashMap<>();
        for (int i = 0; i < allPolicies.length(); i++) {
            JSONObject policy = allPolicies.getJSONObject(i);
            // need to get content of the policy under its name as allPolicies.getJSONObject(i) returns the entire object
            String policyName = policy.keys().next();
            JSONObject policyContent = policy.getJSONObject(policyName);

            boolean isVnfPackageChangePolicy = policyContent.has(POLICY_TYPE_KEY) &&
                    VNF_PACKAGE_CHANGE_POLICY_TYPE.getVnfdKey().equals(policyContent.getString(POLICY_TYPE_KEY));
            if (isVnfPackageChangePolicy) {
                VnfPackageChangePolicyTosca1dot2 parsedPolicy = getVnfPackageChangePolicy(VALIDATOR, vnfdVersion, policyContent);
                result.put(policyName, parsedPolicy);
            }
        }
        return result;
    }

    private static Optional<VnfPackageChangePolicyTosca1dot2> getVnfPackageChangePolicyWithMatchingTargetDescriptor(
            final Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy,
            final String destinationDescriptorId) {
        return allVnfPackageChangePolicy.values().stream().filter(policy -> policy.getProperties()
                .getVnfPackageChangeSelectors()
                .stream().map(VnfPackageChangeSelector::getDestinationDescriptorId)
                .collect(Collectors.toSet())
                .contains(destinationDescriptorId)).findFirst();
    }

    private static JSONObject getInterfaceFromVnfd(final JSONObject vnfd, String interfaceKey) {
        JSONObject nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);
        return getInterfaceDetails(interfaceKey, nodeTypes);
    }

    private static JSONObject getInterfaceDetails(String interfaceKey, JSONObject nodeTypes) {
        for (String nodeKey : nodeTypes.keySet()) {
            if (nodeTypes.get(nodeKey) instanceof JSONObject) {
                JSONObject nodeTypeDetails = nodeTypes.getJSONObject(nodeKey);
                if (hasPropertyOfTypeJsonObject(nodeTypeDetails, INTERFACES_KEY)) {
                    return nodeTypeDetails.getJSONObject(INTERFACES_KEY).getJSONObject(interfaceKey);
                }
            }
        }
        return new JSONObject();
    }

    private static JSONObject getInterfaceDetailsFromVnfLcm(JSONObject nodeTypes) {
        for (String nodeKey : nodeTypes.keySet()) {
            if (nodeTypes.get(nodeKey) instanceof JSONObject) {
                var nodeDetails = nodeTypes.getJSONObject(nodeKey);
                if (hasPropertyOfTypeJsonObject(nodeDetails, INTERFACES_KEY) &&
                        hasPropertyOfTypeJsonObject(nodeDetails.getJSONObject(INTERFACES_KEY), VNFLCM)) {
                    return nodeDetails.getJSONObject(INTERFACES_KEY).getJSONObject(VNFLCM);
                }
            }
        }
        return new JSONObject();
    }

    public static JSONObject getInterfaceFromNodeTemplate(final JSONObject vnfd) {
        Optional<JSONObject> topologyTemplateObject = getOptionalPropertyAsJsonObject(vnfd, TOPOLOGY_TEMPLATE_KEY);
        if (topologyTemplateObject.isPresent() && topologyTemplateObject.get().has(NODE_TEMPLATES_KEY)) {
            JSONObject nodeTypes = topologyTemplateObject.get().getJSONObject(NODE_TEMPLATES_KEY);
            return getInterfaceDetailsFromVnfLcm(nodeTypes);
        }
        return new JSONObject();
    }

    public static JSONObject getInterfaceFromNodeType(final JSONObject vnfd) {
        Optional<JSONObject> nodeTypeObject = getOptionalPropertyAsJsonObject(vnfd, NODE_TYPES_KEY);
        if (nodeTypeObject.isPresent()) {
            return getInterfaceDetailsFromVnfLcm(nodeTypeObject.get());
        }
        return new JSONObject();
    }

    public static List<VnfmLcmInterface> getVnflcmInterfaces(JSONObject vnfd) {
        NodeType nodeType = buildNodeType(vnfd);
        List<VnfmLcmInterface> interfaces = nodeType.getInterfaces();
        setPackagesForEachInterface(vnfd, interfaces);
        return interfaces;
    }

    private static void setPackagesForEachInterface(final JSONObject vnfd, final List<VnfmLcmInterface> interfaces) {
        JSONObject vnflcmNodeTemplate = getInterfaceFromNodeTemplate(vnfd);
        JSONObject vnflcmNodeType = getInterfaceFromNodeType(vnfd);

        JSONObject vnflcmJsonObjectNodeTemplate = getVnflcmInterfaceAsJsonObject(vnflcmNodeTemplate);
        JSONObject vnflcmJsonObjectNodeType = getVnflcmInterfaceAsJsonObject(vnflcmNodeType);

        interfaces.stream().filter(lcmInterface -> lcmInterface.getInputs() != null)
                .forEach(lcmInterface -> {
                    final String operationType = lcmInterface.getType().getLabel();
                    List<HelmPackage> helmPackages =
                            VnfdUtility.getAllHelmPackagesFromLcmOperation(vnflcmJsonObjectNodeTemplate, operationType);
                    if (helmPackages.isEmpty()) {
                        helmPackages =
                                VnfdUtility.getAllHelmPackagesFromLcmOperation(vnflcmJsonObjectNodeType, operationType);
                    }
                    lcmInterface.getInputs().setHelmPackages(helmPackages);
                });
    }

    public static JSONObject getVnflcmInterfaceAsJsonObject(JSONObject vnflcm) {
        if (vnflcm.has(OPERATIONS_KEY)) {
            return vnflcm.getJSONObject(OPERATIONS_KEY);
        }
        return vnflcm;
    }

    public static List<HelmPackage> getAllHelmPackagesFromLcmOperation(JSONObject vnflcmInterface,
                                                                       String operationType) {
        if (!vnflcmInterface.has(operationType)) {
            return Collections.emptyList();
        }

        JSONObject vnflcmOperation = vnflcmInterface.getJSONObject(operationType);

        if (!vnflcmOperation.has(INPUTS_KEY) || !vnflcmOperation.getJSONObject(INPUTS_KEY).has(HELM_PACKAGES_KEY)) {
            return Collections.emptyList();
        }

        Object helmPackagesObject = vnflcmOperation.getJSONObject(INPUTS_KEY)
                .get(HELM_PACKAGES_KEY);

        List<HelmPackage> allHelmArtifacts = new ArrayList<>();
        if (helmPackagesObject instanceof JSONArray) {
            JSONArray helmPackages = (JSONArray) helmPackagesObject;
            for (int i = 0; i < helmPackages.length(); ++i) {
                HelmPackage helmPackage = new HelmPackage();
                helmPackage.setId(helmPackages.getJSONObject(i).getJSONArray("get_artifact").getString(1));
                allHelmArtifacts.add(helmPackage);
            }
        }

        return allHelmArtifacts;
    }

    private static JSONObject getAllInterfaces(final JSONObject vnfd) {
        JSONObject nodeTypes = getMandatoryProperty(vnfd, NODE_TYPES_KEY);
        String nodeTypeName = nodeTypes.keys().next();
        JSONObject nodeType = nodeTypes.getJSONObject(nodeTypeName);
        return getMandatoryProperty(nodeType, INTERFACES_KEY);
    }

    private static Optional<String> getAdditionalParameterTypeFromNodeTypeInterfaces(final JSONObject vnfd, final String operationType) {
        JSONObject interfaces = getInterfaceFromVnfd(vnfd, VNFLCM);

        if (interfaces.has(OPERATIONS_KEY)) {
            interfaces = interfaces.getJSONObject(OPERATIONS_KEY);
        }

        if (hasPropertyOfTypeJsonObject(interfaces, operationType)) {
            JSONObject requiredInterface = (JSONObject) interfaces.get(operationType);
            if (hasPropertyOfTypeJsonObject(requiredInterface, INPUTS_KEY)) {
                JSONObject inputs = (JSONObject) requiredInterface.get(INPUTS_KEY);
                return validateInputsAndGetInterfaceTypeName(inputs);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> validateInputsAndGetInterfaceTypeName(final JSONObject inputs) {
        try {
            if (hasPropertyOfTypeJsonObject(inputs, ADDITIONAL_PARAMETERS_KEY)) {
                return Optional.of((String) ((JSONObject) inputs.get(ADDITIONAL_PARAMETERS_KEY)).get(TYPE_KEY));
            }
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(format(ADDITIONAL_PARAMETERS_TYPE_VALUE_MUST_BE_OF_TYPE_STRING,
                    ex.getMessage()), ex);
        }
        return Optional.empty();
    }

    private static Map<String, Object> getMapFromYamlObject(Object config) {
        validateYamlObjectIsMap(config);

        Map<String, Object> configMap = castToNestedMap(config);
        return replaceValuesAfterKey(configMap, DEFAULT_KEY, null, JSONObject.NULL);
    }

    private static void validateYamlObjectIsMap(Object config) {
        if (!(config instanceof Map)) {
            LOGGER.error(VNFD_NOT_MAP_FORMAT);
            throw new IllegalArgumentException(VNFD_NOT_MAP_FORMAT);
        }
    }

    public static void validateAspects(JSONObject vnfd, final Path csarBasePath) {
        LOGGER.info("Started validating Aspects in vnfd");
        NodeType nodeType = NodeTypeUtility.buildNodeType(vnfd);
        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(vnfd, nodeType);
        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeType, vnfd);
        JSONObject nodeTypes = vnfd.getJSONObject(NODE_TYPES_KEY);

        Optional<ArtifactsPropertiesDetail> scalingMapping = CommonUtility.getArtifacts(nodeTypes).stream()
                .filter(artifact -> SCALING_MAPPING_KEY.equals(artifact.getId()))
                .findFirst();

        if (nodeTemplate.getDeploymentModules() != null && !nodeTemplate.getDeploymentModules().keySet().isEmpty()) {
            scalingMapping.ifPresent(artifact ->
                    validateAspectWithScalingMappingFile(csarBasePath, topologyTemplate,
                            nodeTemplate, artifact));
            if (scalingMapping.isEmpty() && VnfdUtility.isRel4Vnfd(vnfd)) {
                validateAspectWithOutScalingMappingFile(topologyTemplate, nodeTemplate);
            }
        }
    }

    private static void validateAspectWithScalingMappingFile(Path csarBasePath, TopologyTemplate topologyTemplate, NodeTemplate nodeTemplate,
                                                             ArtifactsPropertiesDetail artifact) {

        Path scaleMappingFilePath = csarBasePath.resolve(Paths.get(artifact.getFile()));
        LOGGER.info("Scaling Mapping file found {}", scaleMappingFilePath);
        Map<String, ScaleMapping> scaleMappingMap = ScalingMapUtility.getScalingMap(scaleMappingFilePath);

        if (nodeTemplate.getDeploymentModules() != null && !nodeTemplate.getDeploymentModules().values().isEmpty()) {
            Map<String, List<String>> mapAspectTargets = createMapAspectTargets(topologyTemplate.getPolicies().getAllScalingAspectDelta());
            Map<String, List<String>> mapAspectHelmPackage = new HashMap<>();

            mapAspectTargets.forEach((key, value) ->
                    value.forEach(target ->
                            Optional.ofNullable(scaleMappingMap.get(target))
                                    .map(ScaleMapping::getMciopName)
                                    .ifPresent(helmPackage -> mapAspectHelmPackage.computeIfAbsent(key, k -> new ArrayList<>()).add(helmPackage))));

            isAspectContainDifferentPackages(mapAspectHelmPackage);
        }
    }

    private static void validateAspectWithOutScalingMappingFile(TopologyTemplate topologyTemplate, NodeTemplate nodeTemplate) {
        Map<String, ScalingAspectDeltas> scalingAspectDelta = topologyTemplate.getPolicies().getAllScalingAspectDelta();
        Map<String, List<String>> mapHelmPackageTargets = createMapHelmPackageTargets(nodeTemplate);
        Map<String, List<String>> mapAspectTargets = createMapAspectTargets(scalingAspectDelta);
        Map<String, List<String>> mapTargetHelmPackages = reverseMap(mapHelmPackageTargets);
        Map<String, List<String>> mapAspectHelmPackage = mergeMapWithHierarchy(mapAspectTargets, mapTargetHelmPackages);
        isAspectContainDifferentPackages(mapAspectHelmPackage);
    }

    private static void isAspectContainDifferentPackages(Map<String, List<String>> mapAspectHelmPackage) {
        mapAspectHelmPackage.forEach((key, value) -> {
            if (value.stream().distinct().count() > 1) {
                throw new VnfdValidationException("invalid vnfd, as one of the VDUs is under the two DMs");
            }
        });
    }

    private static Map<String, List<String>> createMapAspectTargets(Map<String, ScalingAspectDeltas> scalingAspectDelta) {
        Map<String, List<String>> mapAspectTargets = new HashMap<>();

        scalingAspectDelta.entrySet().stream()
                .filter(entry -> Optional.ofNullable(entry.getValue())
                        .map(ScalingAspectDeltas::getTargets)
                        .isPresent())
                .forEach(entry -> mapAspectTargets.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).
                        addAll(Arrays.asList(entry.getValue().getTargets())));

        return mapAspectTargets;
    }

    private static Map<String, List<String>> mergeMapWithHierarchy(Map<String, List<String>> mapHigh, Map<String, List<String>> mapLow) {
        Map<String, List<String>> mapResult = new HashMap<>();

        mapHigh.forEach((highKey, value) ->
                value.forEach(target -> {
                    if (mapLow.containsKey(target)) {
                        List<String> lowList = mapLow.get(target);
                        mapResult.put(highKey, lowList);
                    }
                }));

        return mapResult;
    }

    public static Map<String, List<String>> createMapHelmPackageTargets(NodeTemplate nodeTemplate) {
        HashMap<String, List<String>> mapHelmPackageTargets = new HashMap<>();

        if (nodeTemplate.getMciop() != null) {
            nodeTemplate.getMciop().stream()
                    .filter(NodeTemplateUtility::hasAssociatedVdus)
                    .forEach(helmPackage -> mapHelmPackageTargets.put(helmPackage.getName(),
                            new ArrayList<>(helmPackage.getRequirements().get(ASSOCIATED_VDU_KEY))));
        }

        return mapHelmPackageTargets;
    }

    private static Map<String, List<String>> reverseMap(Map<String, List<String>> originalMap) {
        Map<String, List<String>> mapReverse = new HashMap<>();

        originalMap.forEach((aspect, value) ->
                value.forEach(target ->
                        mapReverse.computeIfAbsent(target, k -> new ArrayList<>()).add(aspect)
                ));

        return mapReverse;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getArtifactKeys(Map<String, Object> map) {
        List<String> artifactKeyList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ("node_types".equals(key)) {
                Map<String, Object> nodes = (Map<String, Object>) value;
                if (nodes != null) {
                    List<Object> nodeValues = new ArrayList<>(nodes.values());
                    artifactKeyList = getArtifactKeyList(nodeValues);
                }
            }
        }
        return artifactKeyList;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getArtifactKeyList(List<Object> nodeValues) {
        List<String> artifactKeyList = new ArrayList<>();
        if (nodeValues != null) {
            artifactKeyList = getArtifactKeyListFromNode(nodeValues);
        }
        return artifactKeyList;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getArtifactKeyListFromNode(List<Object> nodeValues) {
        List<String> artifactKeyList = new ArrayList<>();
        for (Object nodeValueObject : nodeValues) {
            Map<String, Object> nodeElements = (Map<String, Object>) nodeValueObject;
            if (nodeElements != null) {
                artifactKeyList = getArtifactKeyList(nodeElements);
                if (!artifactKeyList.isEmpty()) {
                    break;
                }
            }
        }
        return artifactKeyList;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getArtifactKeyList(Map<String, Object> nodeElements) {
        List<String> artifactKeyList = new ArrayList<>();
        if (nodeElements.containsKey("artifacts")) {
            Map<String, Object> artifactsMap = (Map<String, Object>) nodeElements.get("artifacts");
            if (artifactsMap != null) {
                artifactKeyList = new ArrayList<>(artifactsMap.keySet());
            }
        }
        return artifactKeyList;
    }
}
