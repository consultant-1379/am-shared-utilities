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
import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.ericsson.am.shared.vnfd.model.HelmChartType;
import com.ericsson.am.shared.vnfd.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.ericsson.am.shared.vnfd.CommonUtility.getMandatoryProperty;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonArray;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonObject;
import static com.ericsson.am.shared.vnfd.CommonUtility.isCnfChartPresent;
import static com.ericsson.am.shared.vnfd.VnfdUtility.collectAndGetArtifactsPropertiesDetails;
import static com.ericsson.am.shared.vnfd.VnfdUtility.getVnfdInterfaceDetails;
import static com.ericsson.am.shared.vnfd.VnfdUtility.nodeTemplateExists;
import static com.ericsson.am.shared.vnfd.VnfdUtility.validateAndGetArtifactPropertiesDetails;
import static com.ericsson.am.shared.vnfd.VnfdUtility.validateFileAttributeProvided;
import static com.ericsson.am.shared.vnfd.VnfdUtility.validateNodeTypeInterfaces;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum.INSTANTIATE;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.CNF_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.CRD_PACKAGE_PREFIX;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEFAULT_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_PACKAGE_PREFIX;
import static com.ericsson.am.shared.vnfd.utils.Constants.INPUTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_NODE_DETAILS_PROVIDED;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TEMPLATES_KEY;
import static java.lang.String.format;


public final class HelmChartUtility {

    private static final String CANNOT_DETECT_HELM_CHART_TYPE_MESSAGE = "Cannot detect helm chart type.";
    private HelmChartUtility() {
    }

    public static HelmChartType getChartType(String chartName) {
        if (chartName == null) {
            throw new IllegalArgumentException(CANNOT_DETECT_HELM_CHART_TYPE_MESSAGE);
        }
        return Arrays.stream(HelmChartType.values())
                .filter(chartType -> chartName.startsWith(chartType.getChartTypePrefix()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(CANNOT_DETECT_HELM_CHART_TYPE_MESSAGE));
    }

    public static List<HelmChart> parseHelmCharts(final Optional<JSONObject> topologyTemplate,
                                                  final JSONObject nodeTypes,
                                                  final JSONObject dataTypes,
                                                  final List<String> artifactKeys) {
        List<HelmChart> allHelmCharts = new ArrayList<>();

        if (topologyTemplate.isPresent() && nodeTemplateExists(topologyTemplate.get())) {
            JSONObject nodeTemplateDetails = topologyTemplate.get().getJSONObject(NODE_TEMPLATES_KEY);
            JSONArray helmPackagesToInstall = getHelmPackagesFromNodeTemplate(nodeTemplateDetails);

            allHelmCharts = convertHelmChartArrayToList(nodeTypes, helmPackagesToInstall);
        }
        if (allHelmCharts.isEmpty()) {
            allHelmCharts = parseHelmChartFromNodeTypes(nodeTypes, dataTypes);
            if (allHelmCharts.isEmpty()) {
                allHelmCharts = getHelmChartsFromArtifactsBlock(nodeTypes, dataTypes, artifactKeys);
            }
        }
        return allHelmCharts;
    }

    public static List<HelmChart> getHelmChartsFromArtifactsBlock(final JSONObject nodeTypes,
                                                                  final JSONObject dataTypes,
                                                                  final List<String> artifactKeys) {
        List<HelmChart> helmCharts;
        for (String nodeTypeName : nodeTypes.keySet()) {
            JSONObject nodeType = getMandatoryProperty(nodeTypes, nodeTypeName);
            validateNodeTypeInterfaces(nodeType, dataTypes);
            JSONObject artifacts = getMandatoryProperty(nodeType, ARTIFACTS_KEY);
            helmCharts = getHelmChartLocations(artifacts, nodeTypes);
            if (!helmCharts.isEmpty()) {
                helmCharts.sort(Comparator.comparing(i -> artifactKeys.indexOf(i.getChartKey())));
                return helmCharts;
            }
        }
        throw new IllegalArgumentException(INVALID_NODE_DETAILS_PROVIDED);
    }

    private static List<HelmChart> parseHelmChartFromNodeTypes(JSONObject nodeTypes, JSONObject dataTypes) {
        for (String nodeTypeName : nodeTypes.keySet()) {
            JSONObject nodeType = getMandatoryProperty(nodeTypes, nodeTypeName);
            validateNodeTypeInterfaces(nodeType, dataTypes);

            List<HelmChart> helmChartFromNodeTypes = getHelmChartFromNodeTypes(nodeTypes);

            if (!helmChartFromNodeTypes.isEmpty()) {
                return helmChartFromNodeTypes;
            }
        }
        return new ArrayList<>();
    }

    private static List<HelmChart> getHelmChartFromNodeTypes(final JSONObject nodeTypes) {
        JSONArray helmPackagesToInstall = getHelmPackagesFromNodeTemplate(nodeTypes);
        return convertHelmChartArrayToList(nodeTypes, helmPackagesToInstall);
    }

    public static List<String> getAllHelmArtifactsKeyFromInterface(JSONArray helmPackages) {
        List<String> allHelmArtifactsKey = new ArrayList<>();
        //Vnfd parsing
        //helm_packages: [get_artifact: [SELF, helm_package1], get_artifact: [SELF, helm_package2]]
        for (int i = 0; i < helmPackages.length(); i++) {
            //.getString(1) is used to fetch the second element in [SELF, helm_package1]
            allHelmArtifactsKey.add(helmPackages.getJSONObject(i).getJSONArray("get_artifact").getString(1));
        }
        return allHelmArtifactsKey;
    }

    private static List<HelmChart> convertHelmChartArrayToList(final JSONObject nodeType,
                                                               final JSONArray helmPackagesToInstall) {
        List<HelmChart> helmCharts = new ArrayList<>();
        for (String key : nodeType.keySet()) {
            JSONObject nodeTypeDetails = nodeType.getJSONObject(key);
            if (hasPropertyOfTypeJsonObject(nodeTypeDetails, ARTIFACTS_KEY)) {
                JSONObject artifacts = nodeTypeDetails.getJSONObject(ARTIFACTS_KEY);
                if (helmPackagesToInstall != null) {
                    List<String> allHelmArtifactsKey = getAllHelmArtifactsKeyFromInterface(helmPackagesToInstall);
                    helmCharts.addAll(getMultipleHelmCharts(allHelmArtifactsKey, artifacts));
                }
            }
        }
        return helmCharts;
    }

    private static JSONArray getHelmPackagesFromNodeTemplate(final JSONObject nodeTemplateDetails) {
        JSONArray helmPackagesToInstall = null;
        for (String key : nodeTemplateDetails.keySet()) {
            JSONObject nodeDetails = nodeTemplateDetails.getJSONObject(key);
            if (helmPackagesToInstall == null) {
                helmPackagesToInstall = getHelmChartsFromNodeTemplate(nodeDetails);
            }
        }
        return helmPackagesToInstall;
    }

    private static JSONArray getHelmChartsFromNodeTemplate(
            JSONObject nodeDetails) {
        JSONArray helmPackagesToInstall = null;
        if (hasPropertyOfTypeJsonObject(nodeDetails, INTERFACES_KEY)) {
            JSONObject interfaces = nodeDetails.getJSONObject(INTERFACES_KEY);

            for (String interfaceKey : interfaces.keySet()) {
                helmPackagesToInstall = getAllHelmPackagesFromInstantiateOperation(
                        interfaces.getJSONObject(interfaceKey));
                if (helmPackagesToInstall != null) {
                    break;
                }
            }
        }
        return helmPackagesToInstall;
    }

    private static List<HelmChart> fetchingHelmPackageFromOldDescriptor(final JSONObject artifacts) {
        List<HelmChart> helmCharts = new ArrayList<>();

        for (String key : artifacts.keySet()) {
            if (artifacts.get(key) instanceof JSONObject) {
                ArtifactsPropertiesDetail artifactsProperties = validateAndGetArtifactPropertiesDetails(key, artifacts);
                if (key.startsWith(HELM_PACKAGE_PREFIX) || key.startsWith(CRD_PACKAGE_PREFIX)) {
                    //Do not move below validation to Pojo class as artifacts can have different attributes without the file
                    //We would only be checking the file attribute for helm and image attribute
                    validateFileAttributeProvided(artifactsProperties, key);
                    HelmChart helmChart = new HelmChart(artifactsProperties.getFile(), getChartType(key), key);
                    validateArtifactsHelmChart(artifacts, helmChart);
                    helmCharts.add(helmChart);
                }
            }
        }

        if (helmCharts.isEmpty()) {
            throw new IllegalArgumentException(HELM_CHARTS_NOT_PRESENT);
        }
        return helmCharts;
    }

    private static void validateArtifactsHelmChart(JSONObject artifacts, HelmChart helmPath) {
        if (helmPath == null || StringUtils.isEmpty(helmPath.getPath())) {
            throw new IllegalArgumentException(HELM_CHARTS_NOT_PRESENT);
        }

        if (!isCnfChartPresent(artifacts.keySet())) {
            throw new IllegalArgumentException(CNF_CHARTS_NOT_PRESENT);
        }
    }

    private static List<HelmChart> getMultipleHelmCharts(List<String> allHelmArtifactsKey, final JSONObject artifacts) {
        List<HelmChart> allHelmCharts = new ArrayList<>();
        HelmChart helmChart;
        boolean helmArtifactsPresent;

        for (String helmArtifactsKey : allHelmArtifactsKey) {
            helmArtifactsPresent = false;

            for (String key : artifacts.keySet()) {
                if (artifacts.get(key) instanceof JSONObject && helmArtifactsKey.equals(key)) {
                    final ArtifactsPropertiesDetail artifactsPropertiesDetail =
                            collectAndGetArtifactsPropertiesDetails(artifacts, key);

                    HelmChartType chartType = getChartType(key);
                    helmChart = new HelmChart(artifactsPropertiesDetail.getFile(), chartType, key);

                    allHelmCharts.add(helmChart);

                    helmArtifactsPresent = true;
                }
            }
            if (!helmArtifactsPresent) {
                throw new IllegalArgumentException(format(Constants.HELM_PACKAGE_MISSING_IN_ARTIFACTS, helmArtifactsKey));
            }
        }

        if (!isCnfChartPresent(allHelmArtifactsKey)) {
            throw new IllegalArgumentException(CNF_CHARTS_NOT_PRESENT);
        }

        return allHelmCharts;
    }

    private static JSONArray getHelmChartsArray(JSONObject useCase) {
        JSONObject inputs = useCase.getJSONObject(INPUTS_KEY);
        if (hasPropertyOfTypeJsonArray(inputs, HELM_PACKAGES_KEY)) {
            return inputs.getJSONArray(HELM_PACKAGES_KEY);
        } else if (checkForHelmPackageInDefaultKey(inputs)) {
            return inputs.getJSONObject(HELM_PACKAGES_KEY).getJSONArray(DEFAULT_KEY);
        } else {
            return null;
        }
    }

    private static List<HelmChart> getHelmChartLocations(final JSONObject artifacts, JSONObject nodeTypes) {
        List<HelmChart> allHelmCharts = new ArrayList<>();
        JSONArray helmPackagesToInstall = null;
        for (String key : nodeTypes.keySet()) {
            JSONObject nodeTypeDetails = nodeTypes.getJSONObject(key);
            JSONObject interfaces = nodeTypeDetails.getJSONObject(INTERFACES_KEY);
            for (String interfaceKey : interfaces.keySet()) {
                if (helmPackagesToInstall == null) {
                    helmPackagesToInstall = getAllHelmPackagesFromInstantiateOperation(
                            interfaces.getJSONObject(interfaceKey)
                    );
                }
            }
            if (helmPackagesToInstall != null) {
                break;
            }
        }
        if (helmPackagesToInstall != null) {
            List<String> allHelmArtifactsKey = getAllHelmArtifactsKeyFromInterface(helmPackagesToInstall);
            allHelmCharts.addAll(getMultipleHelmCharts(allHelmArtifactsKey, artifacts));
        } else {
            allHelmCharts.addAll(fetchingHelmPackageFromOldDescriptor(artifacts));
        }
        if (allHelmCharts.isEmpty()) {
            throw new IllegalArgumentException("No helm chats details present");
        }
        return allHelmCharts;
    }

    private static JSONArray getAllHelmPackagesFromInstantiateOperation(JSONObject interfaceDetails) {
        JSONObject vnfdInterfaceDetails = getVnfdInterfaceDetails(interfaceDetails);

        for (String useCaseKey : vnfdInterfaceDetails.keySet()) {
            if (useCaseKey.equals(INSTANTIATE.getOperation())) {
                JSONObject useCase = vnfdInterfaceDetails.getJSONObject(useCaseKey);
                if (!useCase.has(INPUTS_KEY) || useCase.getJSONObject(INPUTS_KEY) == null) {
                    break;
                }
                return getHelmChartsArray(useCase);
            }
        }
        return null;
    }

    private static boolean checkForHelmPackageInDefaultKey(final JSONObject inputs) {
        return hasPropertyOfTypeJsonObject(inputs, HELM_PACKAGES_KEY)
                && hasPropertyOfTypeJsonArray(inputs.getJSONObject(HELM_PACKAGES_KEY), DEFAULT_KEY);
    }
}