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

import static com.ericsson.am.shared.vnfd.NestedVnfdUtility.createNestedVnfdObjects;
import static com.ericsson.am.shared.vnfd.NestedVnfdUtility.isFlavourFile;
import static com.ericsson.am.shared.vnfd.NestedVnfdUtility.isNodeFile;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.NODE_TYPE_NAME_NESTED_VNFD;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.NODE_TYPE_VNFD_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.PARENT_VNFD_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_1;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_1_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_2;

import java.nio.file.Path;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.HelmChartType;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.ParentVnfd;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class NestedVnfdUtilityTest {
    @Test
    void testIsFlavourFileOnFlavourFile() {
        Path pathToFlavourFile = TestUtils.getResource(SAMPLE_FLAVOUR_1_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToFlavourFile);
        assertThat(isFlavourFile(jsonData)).isTrue();
    }

    @Test
    void testIsFlavourFileOnNodeFile() {
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        assertThat(isFlavourFile(jsonData)).isFalse();
    }

    @Test
    void testIsNodeFileOnNodeFile() {
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        assertThat(isNodeFile(jsonData)).isTrue();
    }

    @Test
    void testIsNodeFileOnFlavourFile() {
        Path pathToFlavourFile = TestUtils.getResource(SAMPLE_FLAVOUR_1_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToFlavourFile);
        assertThat(isNodeFile(jsonData)).isFalse();
    }

    @Test
    void testParsingParentVnfd() {
        Path pathToParentVnfd = TestUtils.getResource(PARENT_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        ParentVnfd parentVnfd = NestedVnfdUtility.createParentVnfd(jsonData);
        assertThat(parentVnfd.getImports()).hasSize(2);
    }

    @Test
    void testCreateNestedVnfd() {
        Path pathToParentVnfd =
                TestUtils.getResource("nestedVnfd/validNestedVnfDescriptorFiles/sample-multichart-descriptor.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        VnfDescriptorDetails vnfDescriptorDetails = new VnfDescriptorDetails();
        ParentVnfd vnfd = createNestedVnfdObjects(pathToParentVnfd, jsonData, vnfDescriptorDetails);
        assertThat(vnfd.getFlavours()).hasSize(2);
        assertThat(vnfd.getNode()).isNotNull();
        assertThat(vnfd.getNode().getNodeType().getType()).isEqualTo(NODE_TYPE_NAME_NESTED_VNFD);
        assertThat(vnfDescriptorDetails.getFlavours()).containsKey(SAMPLE_FLAVOUR_1);
        assertThat(vnfDescriptorDetails.getFlavours()).containsKey(SAMPLE_FLAVOUR_2);
        assertThat(vnfDescriptorDetails.getDefaultFlavour().getId()).isEqualTo(SAMPLE_FLAVOUR_1);
        assertThat(vnfDescriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(vnfDescriptorDetails.getImagesDetails().get(0).getPath()).isEqualTo("Files/images/docker.tar");
        assertThat(vnfDescriptorDetails.getImagesDetails().get(0).getPath()).isEqualTo("Files/images/docker.tar");
        assertThat(vnfDescriptorDetails.getHelmCharts().get(0).getPath()).isEqualTo("Definitions/OtherTemplates/spider-app-2.207.9.tgz");
        assertThat(vnfDescriptorDetails.getHelmCharts().get(1).getPath()).isEqualTo("Definitions/OtherTemplates/test-scale-chart-0.1.0.tgz");
        assertThat(vnfDescriptorDetails.getHelmCharts().get(2).getPath()).isEqualTo("Definitions/OtherTemplates/spider-app-crd.tgz");
        assertThat(vnfDescriptorDetails.getHelmCharts().get(0).getChartType()).isEqualTo(HelmChartType.CNF);
        assertThat(vnfDescriptorDetails.getHelmCharts().get(1).getChartType()).isEqualTo(HelmChartType.CNF);
        assertThat(vnfDescriptorDetails.getHelmCharts().get(2).getChartType()).isEqualTo(HelmChartType.CRD);
    }

    @Test
    void testCreateNestedVnfdWithoutNodeImport() {
        Path pathToParentVnfd =
                TestUtils.getResource("nestedVnfd/missingNode/sample-multichart-descriptor.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        VnfDescriptorDetails vnfDescriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> createNestedVnfdObjects(pathToParentVnfd,
                                                         jsonData,
                                                         vnfDescriptorDetails)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vnfd does not reference both node and flavour files.");
    }

    @Test
    void testCreatingParentVnfdMissingFlavourDefinedInNodeFile() {
        Path pathToParentVnfd = TestUtils.getResource("nestedVnfd/missingFlavour/sample-multichart-descriptor.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        VnfDescriptorDetails vnfDescriptorDetails = new VnfDescriptorDetails();
        assertThatThrownBy(() -> createNestedVnfdObjects(pathToParentVnfd,
                                                         jsonData,
                                                         vnfDescriptorDetails)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("flavour sample-flavour-2 not defined in Parent Vnfd");
    }
}
