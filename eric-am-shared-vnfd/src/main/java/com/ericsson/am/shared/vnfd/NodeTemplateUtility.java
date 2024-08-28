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
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ASSOCIATED_VDU_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS;
import static com.ericsson.am.shared.vnfd.utils.Constants.DESCRIPTION_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.FILE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.MCIOP_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.OC_CONTAINER_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.REQUIREMENTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_COMPUTE_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_COMPUTE_NODES_NOT_ALLOWED_IN_REL4;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_CP_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_INITIALDELTA_TARGETS_NOT_IN_COMPUTE_NAMES;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_INSTANTIATION_LEVELS_TARGETS_NOT_IN_COMPUTE_NAMES;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_OS_CONTAINER_DEPLOYABLE_UNIT_NODE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_VIRTUAL_BLOCK_STORAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_CP_NODE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.ericsson.am.shared.vnfd.model.CustomInterfaceInputs;
import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.DeployableModule;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Entry;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Mciop;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.MciopArtifact;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCompute;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCp;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainer;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerArtifact;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerDeployableUnit;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduVirtualBlockStorage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VirtualCp;
import com.ericsson.am.shared.vnfd.model.policies.InitialDelta;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.model.policies.VduInstantiationLevels;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class NodeTemplateUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeTemplateUtility.class);

    public static final String UNABLE_TO_PARSE_VDU_COMPUTE_NODE = "Unable to parse VduCompute node";
    public static final String VDU_COMPUTE_NAME_IS_A_REQUIRED_FIELD = "VduCompute name is a required field";
    public static final String MIN_NUMBER_OF_INSTANCES_MUST_BE_0_OR_GREATER = "VduProfile min_number_of_instances must be 0 or greater.";
    public static final String MAX_NUMBER_OF_INSTANCES_MUST_BE_0_OR_GREATER = "VduProfile max_number_of_instances must be 0 or greater.";
    public static final String TYPE_NOT_DEFINED_ERROR_MESSAGE = "Invalid VNFD no type defined for node_template attribute %s";

    private NodeTemplateUtility() {
    }

    public static NodeTemplate createNodeTemplate(NodeType nodeType, JSONObject vnfd) {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(nodeType.getType());
        nodeTemplate.setInterfaces(nodeType.getInterfaces());
        if (checkVnfdHasTopologyTemplateAndNodeTemplate(vnfd)) {
            setStaticAdditionalParamsOnCustomInterface(vnfd, nodeType.getCustomInterface(), nodeType.getType());
            nodeTemplate.setVduCompute(createVduComputeNode(vnfd));
            populateNodeTemplate(nodeTemplate, vnfd);
            validateRel4NodeTemplate(nodeTemplate);
        }
        nodeTemplate.setCustomInterface(nodeType.getCustomInterface());
        return nodeTemplate;
    }

    public static void validateRel4NodeTemplate(NodeTemplate nodeTemplate) {
        if (isRel4NodeTemplate(nodeTemplate) && !CollectionUtils.isEmpty(nodeTemplate.getVduCompute())) {
            throw new IllegalArgumentException(VDU_COMPUTE_NODES_NOT_ALLOWED_IN_REL4);
        }
    }

    public static boolean isRel4NodeTemplate(NodeTemplate nodeTemplate) {
        return !CollectionUtils.isEmpty(nodeTemplate.getMciop());
    }

    public static boolean isDeployableModulesSupported(NodeTemplate nodeTemplate) {
        return !CollectionUtils.isEmpty(nodeTemplate.getDeploymentModules());
    }

    private static void setStaticAdditionalParamsOnCustomInterface(JSONObject vnfd,
            final Map<String, InterfaceTypeImpl> customInterface, final String type) {
        JSONObject topologyTemplate = CommonUtility.getTopologyTemplate(vnfd);
        JSONObject nodeTemplate = topologyTemplate.getJSONObject(NODE_TEMPLATES_KEY);

        JSONObject nodeType = getNodeTypeFromVnfd(nodeTemplate, type);

        if (nodeType != null && nodeType.get(TYPE_KEY).equals(type) && nodeType.has(INTERFACES_KEY)
                && nodeType.get(INTERFACES_KEY) != null) {
            JSONObject interfaces = nodeType.getJSONObject(INTERFACES_KEY);
            customInterface.forEach((key1, interfaceType) -> {
                try {
                    JSONObject nodeTemplateCustomInterfaceAsJson = interfaces.getJSONObject(key1);
                    setOperationSpecificParams(interfaceType, nodeTemplateCustomInterfaceAsJson);
                    setGlobalOperationParams(interfaceType, nodeTemplateCustomInterfaceAsJson);
                } catch (JSONException jsonException) {
                    LOGGER.warn("Interface type not found {}", key1, jsonException);
                }
            });
        }
    }

    private static JSONObject getNodeTypeFromVnfd(JSONObject nodeTemplate, String type) {
        for (String nodeTemplateKey : nodeTemplate.keySet()) {
            if (nodeTemplate.get(nodeTemplateKey) instanceof JSONObject) {
                JSONObject nodeType = checkAndGetNodeType(nodeTemplate.getJSONObject(nodeTemplateKey), type, nodeTemplateKey);
                if (nodeType != null) {
                    return nodeType;
                }
            }
        }
        return null;
    }

    private static JSONObject checkAndGetNodeType(JSONObject nodeTemplateAttribute, String type, String key) {
        if (hasPropertyOfTypeString(nodeTemplateAttribute, TYPE_KEY)) {
            if (nodeTemplateAttribute.getString(TYPE_KEY).equals(type)) {
                return nodeTemplateAttribute;
            }
        } else {
            throw new IllegalArgumentException(String.format(TYPE_NOT_DEFINED_ERROR_MESSAGE, key));
        }
        return null;
    }

    private static void setOperationSpecificParams(final InterfaceTypeImpl interfaceType,
            final JSONObject nodeTemplateCustomInterfaceAsJson) {
        final JSONObject customInterface;

        if (nodeTemplateCustomInterfaceAsJson.has(OPERATIONS_KEY)) {
            customInterface = nodeTemplateCustomInterfaceAsJson.getJSONObject(OPERATIONS_KEY);
        } else {
            customInterface = nodeTemplateCustomInterfaceAsJson;
        }

        interfaceType.getOperation().forEach((operationKey, value) -> {
            if (hasPropertyOfTypeJsonObject(customInterface, operationKey)) {
                JSONObject operation = customInterface.getJSONObject(operationKey);
                if (hasPropertyOfTypeJsonObject(operation, INPUTS_KEY)) {
                    CustomInterfaceInputs input = (CustomInterfaceInputs) value.getInput();
                    input.setStaticAdditionalParams(operation.getJSONObject(INPUTS_KEY).toMap());
                }
            }
        });
    }

    private static void setGlobalOperationParams(final InterfaceTypeImpl interfaceType,
            final JSONObject nodeTemplateCustomInterfaceAsJson) {
        if (nodeTemplateCustomInterfaceAsJson.has(INPUTS_KEY)
                && nodeTemplateCustomInterfaceAsJson.get(INPUTS_KEY) != null) {
            interfaceType.setInputs(nodeTemplateCustomInterfaceAsJson.getJSONObject(INPUTS_KEY).toMap());
        }
    }

    public static boolean checkVnfdHasTopologyTemplateAndNodeTemplate(JSONObject vnfd) {
        if (hasPropertyOfTypeJsonObject(vnfd, TOPOLOGY_TEMPLATE_KEY)) {
            JSONObject topologyTemplate = CommonUtility.getTopologyTemplate(vnfd);
            return hasPropertyOfTypeJsonObject(topologyTemplate, NODE_TEMPLATES_KEY);
        }
        return false;
    }

    private static List<VduCompute> createVduComputeNode(JSONObject vnfd) {
        List<VduCompute> vduComputeList = new ArrayList<>();
        JSONObject topologyTemplate = CommonUtility.getTopologyTemplate(vnfd);
        JSONObject nodeTemplatesAsJson = topologyTemplate.getJSONObject(NODE_TEMPLATES_KEY);
        Iterator<String> keys = nodeTemplatesAsJson.keys();
        VduCompute computeNodeTemp;
        ObjectMapper mapper = new ObjectMapper();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject jsonObject = nodeTemplatesAsJson.getJSONObject(key);
            if (jsonObject.get(TYPE_KEY).equals(VDU_COMPUTE_NODE)) {
                try {
                    computeNodeTemp = mapper.readValue(jsonObject.get(PROPERTIES_KEY).toString(), VduCompute.class);
                    computeNodeTemp.setVduComputeKey(key);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(UNABLE_TO_PARSE_VDU_COMPUTE_NODE, e);
                }
                vduComputeList.add(computeNodeTemp);
            }
        }

        return vduComputeList;
    }

    public static boolean hasAssociatedVdus(Mciop mciop) {
        return !Optional.ofNullable(mciop)
                .map(Mciop::getRequirements)
                .map(requirements -> requirements.get(ASSOCIATED_VDU_KEY))
                .orElse(Collections.emptyList())
                .isEmpty();
    }

    public static void validateVduComputeNode(VduCompute vduComputeNode) {
        if (vduComputeNode.getName() == null || vduComputeNode.getName().isEmpty()) {
            throw new IllegalArgumentException(VDU_COMPUTE_NAME_IS_A_REQUIRED_FIELD);
        }
        if (vduComputeNode.getVduProfile().getMinNumberOfInstances() == null
                || vduComputeNode.getVduProfile().getMinNumberOfInstances() < 0) {
            throw new IllegalArgumentException(MIN_NUMBER_OF_INSTANCES_MUST_BE_0_OR_GREATER);
        }
        if (vduComputeNode.getVduProfile().getMaxNumberOfInstances() == null
                || vduComputeNode.getVduProfile().getMaxNumberOfInstances() < 0) {
            throw new IllegalArgumentException(MAX_NUMBER_OF_INSTANCES_MUST_BE_0_OR_GREATER);
        }
    }

    public static void validatePoliciesModelTargets(NodeTemplate nodeTemplate, Policies policies) {
        String vduType;
        List<String> vduKeys;

        if (isRel4NodeTemplate(nodeTemplate)) {
            vduType = VDU_OS_CONTAINER_DEPLOYABLE_UNIT_NODE;
            vduKeys = nodeTemplate.getOsContainerDeployableUnit()
                    .stream()
                    .map(VduOsContainerDeployableUnit::getVduComputeKey)
                    .collect(Collectors.toList());
        } else {
            vduType = VDU_COMPUTE_NODE;
            vduKeys = nodeTemplate.getVduCompute()
                    .stream()
                    .map(VduCompute::getVduComputeKey)
                    .collect(Collectors.toList());
        }

        for (Map.Entry<String, InitialDelta> entry : policies.getAllInitialDelta().entrySet()) {
            List<String> initialDeltaTargets = Arrays.asList(entry.getValue().getTargets());
            List<String> unmatchedTargets = new ArrayList<>(initialDeltaTargets);
            unmatchedTargets.removeAll(vduKeys);
            if (!unmatchedTargets.isEmpty()) {
                throw new IllegalArgumentException(String.format(VDU_INITIALDELTA_TARGETS_NOT_IN_COMPUTE_NAMES, unmatchedTargets, vduType));
            }
        }
        for (Map.Entry<String, VduInstantiationLevels> entry : policies.getAllVduInstantiationLevels().entrySet()) {
            List<String> vduInstantiationLevelTargets = Arrays.asList(entry.getValue().getTargets());
            List<String> unmatchedTargets = new ArrayList<>(vduInstantiationLevelTargets);
            unmatchedTargets.removeAll(vduKeys);
            if (!unmatchedTargets.isEmpty()) {
                throw new IllegalArgumentException(String.format(VDU_INSTANTIATION_LEVELS_TARGETS_NOT_IN_COMPUTE_NAMES, unmatchedTargets, vduType));
            }
        }
    }

    private static void populateNodeTemplate(NodeTemplate nodeTemplate, JSONObject vnfd) {
        JSONObject topologyTemplate = CommonUtility.getTopologyTemplate(vnfd);

        List<Mciop> mciop =
                buildNodeTemplate(topologyTemplate, MCIOP_NODE, ((name, type) -> nodeTemplateJson ->
                buildMciop(nodeTemplateJson, name, type)));

        List<VduOsContainerDeployableUnit> vduOsContainerDeployableUnit =
                buildNodeTemplate(topologyTemplate, VDU_OS_CONTAINER_DEPLOYABLE_UNIT_NODE, ((name, type) -> nodeTemplateJson ->
                buildVduOsContainerDeployableUnit(nodeTemplateJson, name, type)));

        List<VduOsContainer> vduOsContainer = buildNodeTemplate(topologyTemplate, OC_CONTAINER_NODE, ((name, type) -> nodeTemplateJson ->
                buildVduOsContainer(nodeTemplateJson, name, type)));

        List<VduVirtualBlockStorage> vduVirtualBlockStorages =
                buildNodeTemplate(topologyTemplate, VDU_VIRTUAL_BLOCK_STORAGE, ((name, type) -> nodeTemplateJson ->
                buildVduVirtualBlockStorage(nodeTemplateJson, name, type)));

        List<VduCp> vduCps = buildNodeTemplate(topologyTemplate, VDU_CP_NODE, ((name, type) -> nodeTemplateJson ->
                buildVduCp(nodeTemplateJson, name)));

        List<VirtualCp> virtualCps = buildNodeTemplate(topologyTemplate, VIRTUAL_CP_NODE, ((name, type) -> nodeTemplateJson ->
              buildVirtualCp(nodeTemplateJson, name)));

        Map<String, DeployableModule> deploymentModules = buildNodeTemplate(topologyTemplate, DEPLOYABLE_MODULES, ((name, type) -> nodeTemplateJson ->
                buildDeployableModule(nodeTemplateJson, name, type)))
                .stream().collect(Collectors.toMap(DeployableModule::getNodeName, Function.identity()));

        nodeTemplate.setMciop(mciop);
        nodeTemplate.setOsContainerDeployableUnit(vduOsContainerDeployableUnit);
        nodeTemplate.setVduOsContainer(vduOsContainer);
        nodeTemplate.setVduVirtualBlockStorages(vduVirtualBlockStorages);
        nodeTemplate.setVduCps(vduCps);
        nodeTemplate.setVirtualCps(virtualCps);
        nodeTemplate.setDeploymentModules(deploymentModules);
    }

    private static <T> List<T> buildNodeTemplate(JSONObject topologyTemplate, String nodeTemplateType, BiFunction<String, String,
                                                 Function<JSONObject, T>> triFunction) {
        if (topologyTemplate == null) {
            return Collections.emptyList();
        }
        List<T> nodeTemplateList = new ArrayList<>();
        JSONObject nodeTemplates = topologyTemplate.getJSONObject(NODE_TEMPLATES_KEY);
        Iterator<String> nodeTemplateKeys = nodeTemplates.keys();

        while (nodeTemplateKeys.hasNext()) {
            String nodeTemplateName = nodeTemplateKeys.next();
            JSONObject nodeTemplate = nodeTemplates.getJSONObject(nodeTemplateName);
            String currentNodeTemplateType = nodeTemplate.getString(TYPE_KEY);
            if (currentNodeTemplateType.equals(nodeTemplateType)) {
                T node = triFunction.apply(nodeTemplateName, nodeTemplateType).apply(nodeTemplate);
                nodeTemplateList.add(node);
            }
        }

        return nodeTemplateList;
    }

    private static Mciop buildMciop(JSONObject nodeTemplate, String name, String type) {
        return new Mciop(name, type, buildRequirements(nodeTemplate), buildMciopArtifacts(nodeTemplate));
    }

    private static List<MciopArtifact> buildMciopArtifacts(JSONObject nodeTemplate) {
        List<MciopArtifact> mciopArtifacts = new ArrayList<>();
        JSONObject artifacts = nodeTemplate.getJSONObject(ARTIFACTS_KEY);
        Iterator<String> artifactsNames = artifacts.keys();

        while (artifactsNames.hasNext()) {
            String artifactName = artifactsNames.next();
            JSONObject artifactJson = artifacts.getJSONObject(artifactName);
            MciopArtifact mciopArtifact = new MciopArtifact(artifactName, artifactJson.getString(DESCRIPTION_KEY),
                                                            artifactJson.getString(TYPE_KEY), artifactJson.getString(FILE_KEY));
            mciopArtifacts.add(mciopArtifact);
        }

        return mciopArtifacts;
    }

    private static VduOsContainerDeployableUnit buildVduOsContainerDeployableUnit(JSONObject nodeTemplate, String nodeTemplateName,
                                                                                  String nodeTemplateType) {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject properties = nodeTemplate.getJSONObject(PROPERTIES_KEY);
        try {
            VduOsContainerDeployableUnit vduOsContainerDeployableUnit = objectMapper.readValue(properties.toString(),
                                                                                               VduOsContainerDeployableUnit.class);
            vduOsContainerDeployableUnit.setRequirements(buildRequirements(nodeTemplate));
            vduOsContainerDeployableUnit.setType(nodeTemplateType);
            vduOsContainerDeployableUnit.setVduComputeKey(nodeTemplateName);
            return vduOsContainerDeployableUnit;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Map<String, List<String>> buildRequirements(JSONObject nodeTemplate) {
        if (nodeTemplate.has(REQUIREMENTS_KEY)) {
            JSONArray jsonArray = nodeTemplate.getJSONArray(REQUIREMENTS_KEY);
            return IntStream.range(0, jsonArray.length())
                    .mapToObj(jsonArray::getJSONObject)
                    .map(jsonObject -> new Entry(jsonObject.keys().next(), jsonObject.getString(jsonObject.keys().next())))
                    .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
        }

        return Collections.emptyMap();
    }

    private static VduOsContainer buildVduOsContainer(JSONObject nodeTemplate, String nodeTemplateName, String nodeTemplateType) {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject properties = nodeTemplate.getJSONObject(PROPERTIES_KEY);
        try {
            VduOsContainer vduOsContainer = objectMapper.readValue(properties.toString(), VduOsContainer.class);
            vduOsContainer.setNodeName(nodeTemplateName);
            vduOsContainer.setType(nodeTemplateType);
            vduOsContainer.setArtifacts(buildVduOsContainerArtifacts(nodeTemplate));
            return vduOsContainer;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static DeployableModule buildDeployableModule(JSONObject nodeTemplate, String nodeTemplateName, String nodeTemplateType) {
        DeployableModule deployableModule = new DeployableModule();

        deployableModule.setType(nodeTemplateType);
        deployableModule.setNodeName(nodeTemplateName);

        JSONArray associatedArtifactsNode = nodeTemplate.getJSONObject(PROPERTIES_KEY).getJSONArray(DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS);
        final List<String> associatedArtifacts = IntStream.range(0, associatedArtifactsNode.length())
                .mapToObj(associatedArtifactsNode::getString)
                .collect(Collectors.toList());

        deployableModule.setAssociatedArtifacts(associatedArtifacts);
        return deployableModule;
    }

    private static List<VduOsContainerArtifact> buildVduOsContainerArtifacts(JSONObject nodeTemplate) {
        List<VduOsContainerArtifact> vduOsContainerArtifacts = new ArrayList<>();
        JSONObject artifacts = nodeTemplate.getJSONObject(ARTIFACTS_KEY);
        Iterator<String> artifactNames = artifacts.keys();
        while (artifactNames.hasNext()) {
            String artifactName = artifactNames.next();
            vduOsContainerArtifacts.add(buildVduOsContainerArtifact(artifacts, artifactName));
        }

        return vduOsContainerArtifacts;
    }

    private static VduOsContainerArtifact buildVduOsContainerArtifact(JSONObject artifacts, String name) {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject artifact = artifacts.getJSONObject(name);
        JSONObject artifactProperties = artifact.getJSONObject(PROPERTIES_KEY);
        try {
            VduOsContainerArtifact vduOsContainerArtifact = objectMapper.readValue(artifactProperties.toString(), VduOsContainerArtifact.class);
            vduOsContainerArtifact.setNodeName(name);
            vduOsContainerArtifact.setType(artifact.getString(TYPE_KEY));
            vduOsContainerArtifact.setFile(artifact.getString(FILE_KEY));
            return vduOsContainerArtifact;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static VduVirtualBlockStorage buildVduVirtualBlockStorage(JSONObject nodeTemplate, String nodeTemplateName, String nodeTemplateType) {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject properties = nodeTemplate.getJSONObject(PROPERTIES_KEY);

        try {
            VduVirtualBlockStorage vduVirtualBlockStorage = objectMapper.readValue(properties.toString(), VduVirtualBlockStorage.class);
            vduVirtualBlockStorage.setName(nodeTemplateName);
            vduVirtualBlockStorage.setType(nodeTemplateType);
            return vduVirtualBlockStorage;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static VduCp buildVduCp(JSONObject nodeTemplate, String nodeTemplateName) {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject properties = nodeTemplate.getJSONObject(PROPERTIES_KEY);

        try {
            VduCp vduCp = objectMapper.readValue(properties.toString(), VduCp.class);
            vduCp.setName(nodeTemplateName);
            vduCp.setRequirements(buildVduCpOrVirtualCpRequirements(nodeTemplate));

            return vduCp;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Map<String, String> buildVduCpOrVirtualCpRequirements(JSONObject nodeTemplate) {
        if (nodeTemplate.has(REQUIREMENTS_KEY)) {
            JSONArray jsonArray = nodeTemplate.getJSONArray(REQUIREMENTS_KEY);
            Map<String, String> requirements = new HashMap<>();
            IntStream.range(0, jsonArray.length())
                    .mapToObj(jsonArray::getJSONObject)
                    .map(JSONObject::toMap)
                    .forEach(map -> requirements.putAll(map.entrySet().stream()
                                                                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()))));

            return requirements;
        }

        return Collections.emptyMap();
    }

    private static VirtualCp buildVirtualCp(JSONObject nodeTemplate, String nodeTemplateName) {
        return new VirtualCp(nodeTemplateName, buildVduCpOrVirtualCpRequirements(nodeTemplate));
    }
}
