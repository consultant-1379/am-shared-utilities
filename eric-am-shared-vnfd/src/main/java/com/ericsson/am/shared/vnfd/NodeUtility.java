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
import static com.ericsson.am.shared.vnfd.CommonUtility.isCnfChartPresent;
import static com.ericsson.am.shared.vnfd.model.HelmChartType.CNF;
import static com.ericsson.am.shared.vnfd.model.HelmChartType.CRD;
import static com.ericsson.am.shared.vnfd.utils.Constants.CNF_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.CRD_PACKAGE_PREFIX;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DOCKER_IMAGES_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCSA_ARTIFACTS_NFV_SW_IMAGE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_FILE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.model.ArtifactsPropertiesDetail;
import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.ericsson.am.shared.vnfd.model.HelmChartType;
import com.ericsson.am.shared.vnfd.model.ImageDetails;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.DataType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.DataTypePropertiesDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Node;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;

public final class NodeUtility {

    private NodeUtility() {
    }

    public static Node getNode(JSONObject vnfd, VnfDescriptorDetails descriptorDetails) {
        NodeTypeUtility.getNodeTypeFromVnfd(vnfd); // validates node type
        boolean isRel4Node = VnfdUtility.isRel4Vnfd(vnfd);
        JSONObject dataTypes = vnfd.getJSONObject(DATA_TYPES_KEY);
        JSONObject nodeTypes = vnfd.getJSONObject(NODE_TYPES_KEY);

        Node node = new Node();
        node.setNodeType(buildNodeType(nodeTypes, dataTypes, descriptorDetails, isRel4Node));
        node.setDataTypes(buildDataTypes(dataTypes));
        updateInterfacesWithDataTypesProperties(node);
        return node;
    }

    private static List<DataType> buildDataTypes(JSONObject dataTypes) {
        List<DataType> dataTypesList = new ArrayList<>();
        for (String key : dataTypes.keySet()) {
            if (hasPropertyOfTypeJsonObject(dataTypes, key) &&
                    hasPropertyOfTypeJsonObject(dataTypes.getJSONObject(key), PROPERTIES_KEY)) {
                Map<String, DataTypePropertiesDetails> properties = new LinkedHashMap<>();
                DataType dataType = new DataType();
                JSONObject propertiesJson = dataTypes.getJSONObject(key).getJSONObject(PROPERTIES_KEY);
                for (String propertyKey : propertiesJson.keySet()) {
                    JSONObject property = propertiesJson.getJSONObject(propertyKey);
                    DataTypePropertiesDetails propertiesDetails =
                            CommonUtility.getPojo(property, DataTypePropertiesDetails.class);
                    properties.put(propertyKey, propertiesDetails);
                }
                dataType.setPropertyList(properties);
                dataType.setType(key);
                dataTypesList.add(dataType);
            }
        }
        return dataTypesList;
    }

    private static void updateInterfacesWithDataTypesProperties(Node node) {
        List<VnfmLcmInterface> vnfmLcmInterfaces = node.getNodeType().getInterfaces();
        List<DataType> dataTypes = node.getDataTypes();
        for (VnfmLcmInterface vnfmLcmInterface : vnfmLcmInterfaces) {
            if (vnfmLcmInterface.getInputs() != null && !vnfmLcmInterface.getInputs().getAdditionalParamsDataType().isEmpty()) {
                String dataType = vnfmLcmInterface.getInputs().getAdditionalParamsDataType();
                Optional<DataType> optionalDataType = dataTypes.stream().filter(dataType1 -> dataType1.getType().equals(dataType)).findFirst();
                addAdditionalParams(vnfmLcmInterface, optionalDataType);
            }
        }
    }

    private static void addAdditionalParams(final VnfmLcmInterface vnfmLcmInterface, final Optional<DataType> optionalDataType) {
        if (optionalDataType.isPresent()) {
            Map<String, DataTypePropertiesDetails> additionalParamsInDataType = optionalDataType.get().getPropertyList();
            vnfmLcmInterface.getInputs().setAdditionalParams(additionalParamsInDataType);
        }
    }

    private static NodeType buildNodeType(JSONObject nodeTypes, JSONObject dataTypes,
                                          VnfDescriptorDetails descriptorDetails, boolean isRel4Node) {
        NodeProperties nodeProperties = VnfdUtility.validateAndGetNodeProperties(nodeTypes);

        NodeType nodeType = new NodeType();
        nodeType.setNodeProperties(nodeProperties);
        nodeType.setArtifacts(CommonUtility.getArtifacts(nodeTypes));

        if (isRel4Node) {
            descriptorDetails.setImagesDetails(Collections.emptyList());
        } else {
            ImageDetails imageDetails = new ImageDetails(getImageLocationFromArtifacts(nodeType.getArtifacts()), null);
            descriptorDetails.setImagesDetails(Collections.singletonList(imageDetails));
        }
        descriptorDetails.setHelmCharts(getHelmChartsFromArtifacts(nodeType.getArtifacts()));

        Set<String> nodeTypeKeys = nodeTypes.keySet();
        for (String type : nodeTypeKeys) {
            nodeType.setType(type);
            JSONObject nodeTypeDetails = nodeTypes.getJSONObject(type);
            VnfdUtility.validateNodeTypeInterfaces(nodeTypeDetails, dataTypes);
        }
        nodeType.setInterfaces(getNodeTypeInterfaces(nodeTypes));
        return nodeType;
    }

    private static ArrayList<VnfmLcmInterface> getNodeTypeInterfaces(JSONObject nodeTypes) {
        ArrayList<VnfmLcmInterface> interfacesList = new ArrayList<>();
        for (String nodeKey : nodeTypes.keySet()) {
            JSONObject nodeDetails = nodeTypes.getJSONObject(nodeKey);
            JSONObject interfaces = nodeDetails.getJSONObject(INTERFACES_KEY).getJSONObject(VNFLCM);
            CommonUtility.addVnfmInterface(interfacesList, interfaces);
        }
        return interfacesList;
    }

    private static String getImageLocationFromArtifacts(List<ArtifactsPropertiesDetail> artifacts) {
        String dockerFilePath = null;
        for (ArtifactsPropertiesDetail detail : artifacts) {
            if (detail.getType().equals(TOSCSA_ARTIFACTS_NFV_SW_IMAGE_TYPE)) {
                dockerFilePath = detail.getFile();
            }
        }
        if (StringUtils.isEmpty(dockerFilePath)) {
            throw new IllegalArgumentException(DOCKER_IMAGES_NOT_PRESENT);
        } else {
            return dockerFilePath;
        }
    }

    private static List<HelmChart> getHelmChartsFromArtifacts(List<ArtifactsPropertiesDetail> artifacts) {
        List<HelmChart> helmCharts = new ArrayList<>();
        List<String> artifactNames = artifacts.stream().map(ArtifactsPropertiesDetail::getId).collect(Collectors.toUnmodifiableList());

        for (ArtifactsPropertiesDetail detail : artifacts) {
            if (detail.getType().equals(TOSCA_FILE_TYPE)) {
                String helmChartPath = detail.getFile();
                HelmChartType chartType = detail.getId().startsWith(CRD_PACKAGE_PREFIX) ? CRD : CNF;
                helmCharts.add(new HelmChart(helmChartPath, chartType, detail.getId()));
            }
        }
        if (helmCharts.isEmpty()) {
            throw new IllegalArgumentException(HELM_CHARTS_NOT_PRESENT);
        }

        if (!isCnfChartPresent(artifactNames)) {
            throw new IllegalArgumentException(CNF_CHARTS_NOT_PRESENT);
        }

        return helmCharts;
    }
}
