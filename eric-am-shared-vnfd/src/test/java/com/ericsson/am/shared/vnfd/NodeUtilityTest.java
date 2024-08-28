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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.ericsson.am.shared.vnfd.NodeUtility.getNode;
import static com.ericsson.am.shared.vnfd.utils.Constants.CNF_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.DOCKER_IMAGES_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTY_NOT_FOUND_IN_VNFD;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.NODE_TYPE_NAME_NESTED_VNFD;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.NODE_TYPE_VNFD_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_1;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_2;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.ericsson.am.shared.vnfd.model.HelmChartType;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.DataType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.DataTypePropertiesDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Node;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class NodeUtilityTest {

    private static Stream<Arguments> providedData() {
        return Stream.of(
                Arguments.of("NodeFileWithEmptyNodeType", "nestedVnfd/node-with-empty-node-type.yaml", "None of the node details are present"),
                Arguments.of("NodeFileWithEmptyArtifacts",
                             "nestedVnfd/node-without-artifacts.yaml",
                             "Artifacts details not present in the node type"),
                Arguments.of("NodeFileWithEmptyInterfaces",
                             "nestedVnfd/node-with-empty-interfaces.yaml",
                             "Interface details not present in the node type")
        );
    }

    @Test
    void testGetNode() {
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonData, descriptorDetails);
        NodeProperties nodeProperties = node.getNodeType().getNodeProperties();
        NodeType nodeType = node.getNodeType();
        assertThat(nodeType.getType()).isEqualTo(NODE_TYPE_NAME_NESTED_VNFD);
        assertThat(nodeType.getInterfaces()).hasSize(4);
        assertThat(nodeProperties.getFlavourId()).isNotNull();
        assertThat(nodeProperties.getValidFlavourIds()).hasSize(2);
        assertThat(nodeProperties.getValidFlavourIds().get(SAMPLE_FLAVOUR_1).booleanValue()).isTrue();
        assertThat(nodeProperties.getValidFlavourIds().get(SAMPLE_FLAVOUR_2).booleanValue()).isFalse();
        assertThat(node.getNodeType().getArtifacts()).hasSize(4);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().hasSize(1);
        assertThat(descriptorDetails.getImagesDetails().get(0).getPath()).isEqualTo("Files/images/docker.tar");
        List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
        assertThat(helmCharts.get(0).getPath()).isEqualTo("Definitions/OtherTemplates/spider-app-2.207.9.tgz");
        assertThat(helmCharts.get(1).getPath()).isEqualTo("Definitions/OtherTemplates/test-scale-chart-0.1.0.tgz");
        assertThat(helmCharts.get(2).getPath()).isEqualTo("Definitions/OtherTemplates/spider-app-crd.tgz");
        assertThat(helmCharts.get(0).getChartType()).isEqualTo(HelmChartType.CNF);
        assertThat(helmCharts.get(1).getChartType()).isEqualTo(HelmChartType.CNF);
        assertThat(helmCharts.get(2).getChartType()).isEqualTo(HelmChartType.CRD);
        List<DataType> dataTypes = node.getDataTypes();
        assertThat(dataTypes).hasSize(1);
        DataType dataType = dataTypes.get(0);
        Optional<VnfmLcmInterface> instantiateInterface = nodeType.getInterfaces().stream()
                .filter(vnfmLcmInterface1 -> vnfmLcmInterface1.getType().equals(VnfmLcmInterface.Type.INSTANTIATE)).findFirst();
        assertThat(instantiateInterface).isPresent();
        Map<String, DataTypePropertiesDetails> additionalParams = instantiateInterface.get().getInputs().getAdditionalParams();
        assertThat(instantiateInterface.get().getInputs().getAdditionalParamsDataType()).isEqualTo(dataType.getType());
        assertThat(dataType.getPropertyList()).isEqualTo(additionalParams);
    }

    @Test
    void testGetNodeOnNodeFileWithoutDockerImage() {
        Path pathToNode = TestUtils.getResource("nestedVnfd/node-without-dockerimage.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> NodeUtility.getNode(jsonData, descriptorDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DOCKER_IMAGES_NOT_PRESENT);
    }

    @Test
    void testGetNodeOnNodeFileWithoutHelmCharts() {
        Path pathToNode = TestUtils.getResource("nestedVnfd/node-without-helm-charts.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> NodeUtility.getNode(jsonData, descriptorDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(HELM_CHARTS_NOT_PRESENT);
    }

    @Test
    void testGetNodeWithStandardVnfd() {
        Path pathToStandardVnfd = TestUtils.getResource("nestedVnfd/spider-app-label-verification-2.193.100.yaml")
                .toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToStandardVnfd);
        VnfDescriptorDetails vnfDescriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonData, vnfDescriptorDetails);
        assertThat(node.getDataTypes()).hasSize(2);
    }

    @Test
    void testGetNodeOnNodeFileWithoutNodeType() {
        Path pathToNode = TestUtils.getResource("nestedVnfd/node-without-nodetype.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> NodeUtility.getNode(jsonData, descriptorDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(PROPERTY_NOT_FOUND_IN_VNFD, NODE_TYPES_KEY));
    }

    @ParameterizedTest
    @MethodSource("providedData")
    void testGetNodeShouldThrowExpectedErrorMessage(String testName, String vnfdPath, String expectedMessage) {
        Path pathToNode = TestUtils.getResource(vnfdPath).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> NodeUtility.getNode(jsonData, descriptorDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testGetNodeWithHealInterface() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_heal.yaml").toAbsolutePath());
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = NodeUtility.getNode(jsonData, descriptorDetails);
        List<VnfmLcmInterface> interfaces = node.getNodeType().getInterfaces();
        assertThat(interfaces.stream()
                           .anyMatch(vnfmLcmInterface -> vnfmLcmInterface.getType() == VnfmLcmInterface.Type.HEAL))
                .isTrue();
    }

    @Test
    void testGetNodeOnNodeFileWithoutCnfHelmChart() {
        Path pathToNode = TestUtils.getResource("nestedVnfd/node-without-cnf-helm-charts.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> NodeUtility.getNode(jsonData, descriptorDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CNF_CHARTS_NOT_PRESENT);
    }
}
