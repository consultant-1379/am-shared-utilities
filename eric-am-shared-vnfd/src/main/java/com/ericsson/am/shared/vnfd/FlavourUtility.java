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

import com.ericsson.am.shared.vnfd.model.ArtifactsPropertiesDetail;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmPackage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Inputs;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.ParentVnfd;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.utils.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.EnumUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonArray;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_FOR_THE_FOLLOWING_FLAVOUR_IDS_S_ARE_INVALID;
import static com.ericsson.am.shared.vnfd.utils.Constants.FLAVOUR_ID_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_MISSING_IN_FLAVOUR_FILE;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_PRIORITY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_VALUES;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACE_DEFINED_IGNORING_INPUTS;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NOT_A_VALID_INTERFACE;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPES_MISSING_FROM_FLAVOUR;
import static com.ericsson.am.shared.vnfd.utils.Constants.TYPE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM;

public final class FlavourUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlavourUtility.class);

    private FlavourUtility() {
    }

    public static Flavour getDefaultFlavour(JSONObject vnfd, NodeType nodeType) {
        Flavour flavor = new Flavour();
        NodeProperties nodeProperties = nodeType.getNodeProperties();
        flavor.setId(nodeProperties.getFlavourId().getDefaultValue().toString());
        flavor.setNodeType(nodeType.getType());
        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(vnfd, nodeType);
        flavor.setTopologyTemplate(topologyTemplate);
        return flavor;
    }

    public static Flavour getFlavour(JSONObject flavourFile) {
        Flavour flavour = new Flavour();
        Optional<JSONObject> topologyTemplateOpt = VnfdUtility.getTopologyTemplate(flavourFile);
        if (topologyTemplateOpt.isPresent()) {
            JSONObject topologyTemplateJson = topologyTemplateOpt.get();
            JSONObject substitutionMappings = topologyTemplateJson.getJSONObject(SUBSTITUTION_MAPPINGS);
            String id = substitutionMappings.getJSONObject(PROPERTIES_KEY).get(FLAVOUR_ID_KEY).toString();
            String nodeType = substitutionMappings.getString(NODE_TYPE_KEY);
            flavour.setNodeType(nodeType);
            flavour.setId(id);
            Policies flavourPolicies = PolicyUtility.createAndValidatePolicies(flavourFile);
            TopologyTemplate topologyTemplate = createTopologyTemplate(flavour, topologyTemplateJson, flavourPolicies);
            flavour.setTopologyTemplate(topologyTemplate);
            return flavour;
        } else {
            throw new IllegalArgumentException(TOPOLOGY_TEMPLATE_MISSING);
        }
    }

    private static TopologyTemplate createTopologyTemplate(final Flavour flavour,
                                                           final JSONObject topologyTemplateJson,
                                                           final Policies flavourPolicies) {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setPolicies(flavourPolicies);
        topologyTemplate.setNodeTemplate(createNodeTemplate(topologyTemplateJson, flavour));
        return topologyTemplate;
    }

    public static void validateFlavours(final ParentVnfd parentVnfd) {
        List<String> invalidFlavourIds = new ArrayList<>();
        List<ArtifactsPropertiesDetail> artifacts = parentVnfd.getNode().getNodeType().getArtifacts();
        parentVnfd.getFlavours().forEach((id, value) -> validateFlavourArtifacts(id, value, artifacts,
                invalidFlavourIds));
        if (!CollectionUtils.isEmpty(invalidFlavourIds)) {
            throw new IllegalArgumentException(String.format(ARTIFACTS_FOR_THE_FOLLOWING_FLAVOUR_IDS_S_ARE_INVALID,
                    invalidFlavourIds.toString()));
        }
    }

    private static NodeTemplate createNodeTemplate(JSONObject topologyTemplate, Flavour flavour) {
        String flavourId = flavour.getId();
        String nodeType = flavour.getNodeType();
        NodeTemplate nodeTemplate = new NodeTemplate();
        if (VnfdUtility.nodeTemplateExists(topologyTemplate)) {
            JSONObject nodeTemplateDetails = topologyTemplate.getJSONObject(NODE_TEMPLATES_KEY);
            for (String key : nodeTemplateDetails.keySet()) {
                JSONObject nodeDetails = nodeTemplateDetails.getJSONObject(key);
                checkTypeExists(nodeDetails, flavourId);
                if (nodeDetails.get(TYPE_KEY).equals(nodeType)) {
                    generateInterfacesList(nodeTemplate, nodeDetails);
                }
            }
        } else {
            throw new IllegalArgumentException(String.format(NODE_TEMPLATES_MISSING, flavourId));
        }
        return nodeTemplate;
    }

    private static void generateInterfacesList(NodeTemplate nodeTemplate, JSONObject nodeDetails) {
        nodeTemplate.setType(nodeDetails.getString(Constants.TYPE_KEY));
        ArrayList<VnfmLcmInterface> interfacesList = new ArrayList<>();
        if (hasPropertyOfTypeJsonObject(nodeDetails, INTERFACES_KEY)) {
            JSONObject interfaces = nodeDetails.getJSONObject(INTERFACES_KEY).getJSONObject(VNFLCM);
            for (String vnflcmInterface : interfaces.keySet()) {
                VnfmLcmInterface interfaceToAdd = createInterface(vnflcmInterface);
                JSONObject vnflcmInterfaces = interfaces.getJSONObject(vnflcmInterface);
                addInputs(interfaceToAdd, vnflcmInterfaces);
                interfacesList.add(interfaceToAdd);
            }
            nodeTemplate.setInterfaces(interfacesList);
        }
    }

    private static void checkTypeExists(JSONObject nodeDetails, String flavourId) {
        if (!VnfdUtility.checkKeyExists(nodeDetails, Constants.TYPE_KEY)) {
            throw new IllegalArgumentException(String.format(TYPES_MISSING_FROM_FLAVOUR, flavourId));
        }
    }


    static VnfmLcmInterface createInterface(String vnflcmInterface) {
        VnfmLcmInterface vnfmLcmInterface = new VnfmLcmInterface();
        checkInterfaceType(vnflcmInterface);
        vnfmLcmInterface.setType(vnflcmInterface);
        return vnfmLcmInterface;
    }

    private static void addInputs(VnfmLcmInterface interfaceToAdd, JSONObject vnflcmInterfaces) {
        if (vnflcmInterfaces.length() != 0) {
            JSONObject inputContents = vnflcmInterfaces.getJSONObject(Constants.INPUTS_KEY);
            JSONArray helmPackages = getHelmPackagesOfInterface(inputContents);
            List<String> helmPackageIdentifiers = HelmChartUtility.getAllHelmArtifactsKeyFromInterface(helmPackages);
            List<Integer> helmPackagePriorities = new ArrayList<>();
            if (hasPropertyOfTypeJsonArray(inputContents, HELM_PACKAGES_PRIORITY)) {
                JSONArray helmPriorities = inputContents.getJSONArray(HELM_PACKAGES_PRIORITY);
                checkAmountPrioritiesMatchesAmountPackages(helmPackageIdentifiers, helmPriorities.length());
                for (Object priority : helmPriorities) {
                    checkAndAddPriorities(helmPackagePriorities, priority);
                }
            }
            List<HelmPackage> helmPackagesList = createHelmPackagesList(helmPackageIdentifiers, helmPackagePriorities);
            if (hasPropertyOfTypeJsonObject(inputContents, HELM_VALUES)) {
                JSONObject helmValues = inputContents.getJSONObject(HELM_VALUES);
                Map<String, Object> helmValuesKeys = helmValues.toMap();
                for (HelmPackage helmPackage : helmPackagesList) {
                    Map<String, Object> helmPackageHelmValues = new HashMap<>();
                    getHelmValuesForHelmPackage(helmValuesKeys, helmPackage, helmPackageHelmValues);
                    helmPackage.setHelmValues(helmPackageHelmValues);
                }
            }
            Inputs inputs = new Inputs();
            inputs.setHelmPackages(helmPackagesList);
            interfaceToAdd.setInputs(inputs);
        }
    }

    private static void getHelmValuesForHelmPackage(final Map<String, Object> helmValuesKeys,
                                                    final HelmPackage helmPackage,
                                                    final Map<String, Object> helmPackageHelmValues) {
        for (Map.Entry<String, Object> helmValue : helmValuesKeys.entrySet()) {
            if (helmValue.getKey().startsWith(helmPackage.getId())) {
                String newKey = helmValue.getKey().replace(helmPackage.getId() + ".", "");
                Object o = helmValue.getValue();
                helmPackageHelmValues.put(newKey, o);
            }
        }
    }

    private static List<HelmPackage> createHelmPackagesList(final List<String> helmPackageIdentifiers,
                                                            final List<Integer> helmPackagePriorities) {
        List<HelmPackage> helmPackagesList = new ArrayList<>();
        if (!helmPackagePriorities.isEmpty()) {
            for (int i = 0; i < helmPackagePriorities.size(); i++) {
                HelmPackage helmPackage = new HelmPackage(helmPackageIdentifiers.get(i), helmPackagePriorities.get(i));
                helmPackagesList.add(helmPackage);
            }
        } else {
            helmPackageIdentifiers.forEach(helmPackageId -> {
                HelmPackage helmPackage = new HelmPackage(helmPackageId, 0);
                helmPackagesList.add(helmPackage);
            });
        }
        return helmPackagesList;
    }

    private static void checkAmountPrioritiesMatchesAmountPackages(final List<String> helmPackageIdentifiers, final int amountPriorities) {
        if (amountPriorities != 0 && amountPriorities != helmPackageIdentifiers.size()) {
            throw new IllegalArgumentException("helm_packages_priority count does not match helm_packages count");
        }
    }

    private static void checkAndAddPriorities(final List<Integer> helmPackagePriorities, final Object priority) {
        if (!(priority instanceof Integer)) {
            throw new IllegalArgumentException("helm_packages_priority must be numeric");
        } else {
            helmPackagePriorities.add(Integer.valueOf(priority.toString()));
        }
    }

    private static JSONArray getHelmPackagesOfInterface(final JSONObject inputContents) {
        if (!hasPropertyOfTypeJsonArray(inputContents, HELM_PACKAGES_KEY)) {
            throw new IllegalArgumentException(HELM_PACKAGES_MISSING_IN_FLAVOUR_FILE);
        }
        JSONArray helmPackages = inputContents.getJSONArray(HELM_PACKAGES_KEY);
        if (helmPackages.length() == 0) {
            throw new IllegalArgumentException(HELM_PACKAGES_MISSING_IN_FLAVOUR_FILE);
        }
        return helmPackages;
    }

    private static void checkInterfaceType(String vnflcmInterface) {
        boolean exists = EnumUtils.isValidEnum(VnfmLcmInterface.Type.class, vnflcmInterface.toUpperCase(Locale.ENGLISH));
        if (!exists) {
            throw new IllegalArgumentException(String.format(NOT_A_VALID_INTERFACE, vnflcmInterface));
        }
    }

    private static boolean validateFlavourArtifacts(final String id, final Flavour flavour,
                                                    final List<ArtifactsPropertiesDetail> artifacts, List<String> invalidFlavourIds) {
        List<VnfmLcmInterface> interfaces = flavour.getTopologyTemplate().getNodeTemplate().getInterfaces();
        for (VnfmLcmInterface vnfmLcmInterface : interfaces) {
            VnfmLcmInterface.Type type = vnfmLcmInterface.getType();
            if (VnfmLcmInterface.Type.SCALE.equals(type) || VnfmLcmInterface.Type.TERMINATE.equals(type)) {
                LOGGER.warn(INTERFACE_DEFINED_IGNORING_INPUTS, id, type);
            } else {
                Inputs inputs = vnfmLcmInterface.getInputs();
                Optional<HelmPackage> first = inputs.getHelmPackages().stream()
                        .filter(helmPackage -> validateFlavourArtifact(artifacts, helmPackage)).findFirst();
                if (first.isPresent()) {
                    invalidFlavourIds.add(id);
                    return false;
                }
            }
        }
        return true;
    }

    static boolean validateFlavourArtifact(final List<ArtifactsPropertiesDetail> artifacts, final HelmPackage helmPackage) {
        return (artifacts.stream().noneMatch(artifact -> artifact.getId().equals(helmPackage.getId())));
    }
}
