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

import static com.ericsson.am.shared.vnfd.FlavourUtility.getFlavour;
import static com.ericsson.am.shared.vnfd.FlavourUtility.validateFlavourArtifact;
import static com.ericsson.am.shared.vnfd.FlavourUtility.validateFlavours;
import static com.ericsson.am.shared.vnfd.NodeUtility.getNode;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOPOLOGY_TEMPLATE_MISSING;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.NODE_TYPE_NAME_NESTED_VNFD;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.NODE_TYPE_VNFD_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.PARENT_VNFD_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_1;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_1_MISSING_ARTIFACTS_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_1_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_2;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_2_MISSING_ARTIFACTS_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_FLAVOUR_2_YAML;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ericsson.am.shared.vnfd.model.ArtifactsPropertiesDetail;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmPackage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Inputs;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Node;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.ParentVnfd;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class FlavourUtilityTest {

    @Test
    void testGetFlavour() {
        Path pathToNode = TestUtils.getResource(SAMPLE_FLAVOUR_1_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        Flavour flavour = getFlavour(jsonData);
        assertThat(flavour.getId()).isEqualTo(SAMPLE_FLAVOUR_1);
        TopologyTemplate topologyTemplate = flavour.getTopologyTemplate();
        NodeTemplate nodeTemplate = topologyTemplate.getNodeTemplate();
        assertThat(nodeTemplate.getType()).isEqualTo(NODE_TYPE_NAME_NESTED_VNFD);
        List<VnfmLcmInterface> interfaceList = nodeTemplate.getInterfaces();
        assertThat(interfaceList).hasSize(3);
        VnfmLcmInterface instantiateInterface = interfaceList.get(0);
        assertThat(instantiateInterface.getType()).isEqualTo(VnfmLcmInterface.Type.INSTANTIATE);
        Inputs instantiateInputs = instantiateInterface.getInputs();
        List<HelmPackage> helmPackages = instantiateInputs.getHelmPackages();
        assertThat(helmPackages).hasSize(2);
        HelmPackage helmPackage1 = helmPackages.get(0);
        assertThat(helmPackage1.getId()).isEqualTo("helm_package1");
        assertThat(helmPackage1.getPriority()).isEqualTo(1);
        Map<String, Object> package1HelmValues = helmPackage1.getHelmValues();
        assertThat(package1HelmValues)
                .hasSize(4)
                .containsKey("tags.vnfc1")
                .containsEntry("tags.vnfc1", "true");
        assertThat(helmPackages.get(1).getId()).isEqualTo("helm_package2");
        assertThat(helmPackages.get(1).getPriority()).isZero();
        Policies policies = topologyTemplate.getPolicies();
        String scalingInfo =
                "Policies[allHelmParamsMappings={},allInitialDelta={vnfc1"
                        + ".test-cnf=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType"
                        + "[numberOfInstances=1]],targets={test-cnf},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc3=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc3},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc4=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc4},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc1=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc1},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc2=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc2},type=tosca.policies.nfv.VduInitialDelta]},allInstantiationLevels={},"
                        + "allScalingAspectDelta={Payload_ScalingAspectDeltas2=ScalingAspectDeltas[allInitialDelta={vnfc3=InitialDelta[properties"
                        + "=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],targets={test-cnf-vnfc3},type=tosca"
                        + ".policies.nfv.VduInitialDelta], vnfc2=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType"
                        + "[numberOfInstances=1]],targets={test-cnf-vnfc2},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect1,deltas={delta_1=VduLevelDataType[numberOfInstances=4]}],"
                        + "targets={test-cnf-vnfc3,test-cnf-vnfc2},type=tosca.policies.nfv.VduScalingAspectDeltas], "
                        + "Payload_ScalingAspectDeltas1=ScalingAspectDeltas[allInitialDelta={vnfc3=InitialDelta[properties"
                        + "=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],targets={test-cnf-vnfc3},type=tosca"
                        + ".policies.nfv.VduInitialDelta], vnfc4=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType"
                        + "[numberOfInstances=1]],targets={test-cnf-vnfc4},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect2,deltas={delta_1=VduLevelDataType[numberOfInstances=4], "
                        + "delta_2=VduLevelDataType[numberOfInstances=1], delta_3=VduLevelDataType[numberOfInstances=9], "
                        + "delta_4=VduLevelDataType[numberOfInstances=3]}],targets={test-cnf-vnfc4,test-cnf-vnfc3},type=tosca.policies.nfv"
                        + ".VduScalingAspectDeltas], Payload_ScalingAspectDeltas4=ScalingAspectDeltas[allInitialDelta={vnfc1"
                        + ".test-cnf=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc3=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc3},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc4=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc4},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc1=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc1},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc2=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc2},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect4,deltas={delta_1=VduLevelDataType[numberOfInstances=1]}],"
                        + "targets={test-cnf,test-cnf-vnfc1,test-cnf-vnfc3,test-cnf-vnfc2,test-cnf-vnfc4},type=tosca.policies.nfv"
                        + ".VduScalingAspectDeltas], Payload_ScalingAspectDeltas3=ScalingAspectDeltas[allInitialDelta={vnfc1"
                        + ".test-cnf=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc1=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc1},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect3,deltas={delta_1=VduLevelDataType[numberOfInstances=2]}],"
                        + "targets={test-cnf,test-cnf-vnfc1},type=tosca.policies.nfv.VduScalingAspectDeltas]},"
                        + "allScalingAspects={ScalingAspects1=ScalingAspects[properties=ScalingAspectProperties[allAspects={Aspect4"
                        + "=ScalingAspectDataType[allScalingAspectDelta={Payload_ScalingAspectDeltas4=ScalingAspectDeltas[allInitialDelta={vnfc1"
                        + ".test-cnf=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc3=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc3},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc4=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc4},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc1=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc1},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc2=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc2},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect4,deltas={delta_1=VduLevelDataType[numberOfInstances=1]}],"
                        + "targets={test-cnf,test-cnf-vnfc1,test-cnf-vnfc3,test-cnf-vnfc2,test-cnf-vnfc4},type=tosca.policies.nfv"
                        + ".VduScalingAspectDeltas]},description=Scale level 0-6 maps to 4-10 for test-cnf VNFC instances, maps to 3-9 for "
                        + "test-cnf-vnfc1 VNFC instances, maps to 5-11 for test-cnf-vnfc2 VNFC instances, maps to 1-7 for test-cnf-vnfc3 VNFC "
                        + "instances and maps to 2-8 for test-cnf-vnfc4 VNFC instances (1 instance per scale step)\n"
                        + ",enabled=true,maxScaleLevel=6,name=Aspect4,stepDeltas=[delta_1]], "
                        + "Aspect1=ScalingAspectDataType[allScalingAspectDelta={Payload_ScalingAspectDeltas2=ScalingAspectDeltas[allInitialDelta"
                        + "={vnfc3=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc3},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc2=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc2},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect1,deltas={delta_1=VduLevelDataType[numberOfInstances=4]}],"
                        + "targets={test-cnf-vnfc3,test-cnf-vnfc2},type=tosca.policies.nfv.VduScalingAspectDeltas]},description=Scale level 0-10 "
                        + "maps to 1-41 for test-cnf-vnfc3 VNFC instances and also maps to 5-45 for test-cnf-vnfc2 VNFC instances (4 instance per "
                        + "scale step)\n"
                        + ",enabled=true,maxScaleLevel=10,name=Aspect1,stepDeltas=[delta_1]], "
                        + "Aspect2=ScalingAspectDataType[allScalingAspectDelta={Payload_ScalingAspectDeltas1=ScalingAspectDeltas[allInitialDelta"
                        + "={vnfc3=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc3},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc4=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc4},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect2,deltas={delta_1=VduLevelDataType[numberOfInstances=4], "
                        + "delta_2=VduLevelDataType[numberOfInstances=1], delta_3=VduLevelDataType[numberOfInstances=9], "
                        + "delta_4=VduLevelDataType[numberOfInstances=3]}],targets={test-cnf-vnfc4,test-cnf-vnfc3},type=tosca.policies.nfv"
                        + ".VduScalingAspectDeltas]},description=Scale level 0-7 maps to 6-28 for test-cnf-vnfc4 VNFC instances and maps to 5-27 "
                        + "for test-cnf-vnfc3 VNFC instances (4 instance in first scale level, 1 instance in second scale level, 9 instance in "
                        + "third scale level and 3 instance in all the next scale levels)\n"
                        + ",enabled=true,maxScaleLevel=7,name=Aspect2,stepDeltas=[delta_1, delta_2, delta_3, delta_4]], "
                        + "Aspect3=ScalingAspectDataType[allScalingAspectDelta={Payload_ScalingAspectDeltas3=ScalingAspectDeltas[allInitialDelta"
                        + "={vnfc1.test-cnf=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf},type=tosca.policies.nfv.VduInitialDelta], "
                        + "vnfc1=InitialDelta[properties=VduInitialDeltaProperties[initialDelta=VduLevelDataType[numberOfInstances=1]],"
                        + "targets={test-cnf-vnfc1},type=tosca.policies.nfv.VduInitialDelta]},"
                        + "properties=VduScalingAspectDeltasProperties[aspect=Aspect3,deltas={delta_1=VduLevelDataType[numberOfInstances=2]}],"
                        + "targets={test-cnf,test-cnf-vnfc1},type=tosca.policies.nfv.VduScalingAspectDeltas]},description=Scale level 0-12 maps to "
                        + "4-28 for test-cnf VNFC instances and also maps to 3-27 for test-cnf-vnfc1 VNFC instances (2 instance per scale step)\n"
                        + ",enabled=true,maxScaleLevel=12,name=Aspect3,stepDeltas=[delta_1]]}],type=tosca.policies.nfv.ScalingAspects]},"
                        + "allVduInstantiationLevels={},allVnfPackageChangePolicy={}]";
        assertThat(policies).hasToString(scalingInfo);
    }

    @Test
    void testGetFlavourWithSeveralHelmPackagesAndCheckPriorityOrder() {
        Path pathToNode = TestUtils.getResource("nestedVnfd/flavour-with-several-helm-packages.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        Flavour flavour = getFlavour(jsonData);
        assertThat(flavour.getId()).isEqualTo(SAMPLE_FLAVOUR_2);
        TopologyTemplate topologyTemplate = flavour.getTopologyTemplate();
        NodeTemplate nodeTemplate = topologyTemplate.getNodeTemplate();
        assertThat(nodeTemplate.getType()).isEqualTo(NODE_TYPE_NAME_NESTED_VNFD);
        List<VnfmLcmInterface> interfaceList = nodeTemplate.getInterfaces();
        VnfmLcmInterface instantiateInterface = interfaceList.get(0);
        Inputs instantiateInputs = instantiateInterface.getInputs();
        List<HelmPackage> helmPackages = instantiateInputs.getHelmPackages();
        assertThat(helmPackages).hasSize(5);
        for (int i = 0; i < 5; i++) {
            HelmPackage helmPackage = helmPackages.get(i);
            int packageNameInteger = i + 1;
            assertThat(helmPackage.getId()).isEqualTo("helm_package" + packageNameInteger);
            assertThat(helmPackage.getPriority()).isEqualTo(packageNameInteger);
        }
    }

    @Test
    void testGetFlavourWithOneHelmPackageAndNoPriority() {
        Path pathToNode = TestUtils.getResource(SAMPLE_FLAVOUR_2_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        Flavour flavour = getFlavour(jsonData);
        assertThat(flavour.getId()).isEqualTo(SAMPLE_FLAVOUR_2);
        TopologyTemplate topologyTemplate = flavour.getTopologyTemplate();
        NodeTemplate nodeTemplate = topologyTemplate.getNodeTemplate();
        assertThat(nodeTemplate.getType()).isEqualTo(NODE_TYPE_NAME_NESTED_VNFD);
        List<VnfmLcmInterface> interfaceList = nodeTemplate.getInterfaces();
        assertThat(interfaceList).hasSize(2);
        VnfmLcmInterface instantiateInterface = interfaceList.get(0);
        assertThat(instantiateInterface.getType()).isEqualTo(VnfmLcmInterface.Type.INSTANTIATE);
        Inputs instantiateInputs = instantiateInterface.getInputs();
        List<HelmPackage> helmPackages = instantiateInputs.getHelmPackages();
        assertThat(helmPackages).hasSize(1);
        assertThat(helmPackages.get(0).getPriority()).isZero();
    }

    @ParameterizedTest
    @MethodSource("providedData")
    void testGetFlavourShouldThrownValidErrorMessage(String testNameString, String vnfdPath, String expectedMessage)  {
        Path pathToNode = TestUtils.getResource(vnfdPath).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        assertThatThrownBy(() -> FlavourUtility.getFlavour(jsonData)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> providedData() {
        return Stream.of(
                Arguments.of("FlavourWithoutNodeType", "nestedVnfd/flavour-without-node-type.yaml", "types missing from flavour: sample-flavour-1"),
                Arguments.of("FlavourWithoutTopologyTemplate", "nestedVnfd/flavour-without-topologytemplate.yaml", TOPOLOGY_TEMPLATE_MISSING),
                Arguments.of("FlavourWithoutNodeTemplate", "nestedVnfd/flavour-without-node-template.yaml",
                             "node_templates missing from sample-flavour-1"),
                Arguments.of("FlavourWithMismatchingPriorityAndPackageCounts", "nestedVnfd/flavour-with-priority-package-count-mismatch.yaml",
                             "helm_packages_priority count does not match helm_packages count"),
                Arguments.of("GetFlavourWithNonNumericPriority", "nestedVnfd/flavour-with-non-numeric-priorities.yaml",
                             "helm_packages_priority must be numeric"),
                Arguments.of("FlavourWithInvalidInterface", "nestedVnfd/flavour-with-invalid-interface.yaml",
                             "Interface made-up-interface is not a valid interface"),
                Arguments.of("FlavourWithNoHelmPackages", "nestedVnfd/flavour-without-helm-packages.yaml",
                             "helm_packages missing in flavour file"),
                Arguments.of("FlavourWithEmptyHelmPackages", "nestedVnfd/flavour-with-empty-helm-packages.yaml",
                             "helm_packages missing in flavour file")
        );
    }

    @Test
    void testValidateFlavourArtifactNotFound() {
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonData, descriptorDetails);
        List<ArtifactsPropertiesDetail> artifacts = node.getNodeType().getArtifacts();
        HelmPackage helmPackage = new HelmPackage("not_found", 1);
        assertThat(validateFlavourArtifact(artifacts, helmPackage)).isTrue();
    }

    @Test
    void testValidateFlavourArtifact() {
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonData, descriptorDetails);
        List<ArtifactsPropertiesDetail> artifacts = node.getNodeType().getArtifacts();
        HelmPackage helmPackage = new HelmPackage("helm_package2", 1);
        assertThat(validateFlavourArtifact(artifacts, helmPackage)).isFalse();
    }

    @Test
    void testValidateFlavourArtifacts() {
        Path pathToParentVnfd = TestUtils.getResource(PARENT_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        ParentVnfd parentVnfd = NestedVnfdUtility.createParentVnfd(jsonData);
        Path pathToFlavour = TestUtils.getResource(SAMPLE_FLAVOUR_1_YAML).toAbsolutePath();
        JSONObject jsonDataFlavour = VnfdUtility.validateYamlCanBeParsed(pathToFlavour);
        Flavour flavour = getFlavour(jsonDataFlavour);
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonDataNode = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonDataNode, descriptorDetails);
        Map<String, Flavour> flavourMap = new LinkedHashMap<>();
        flavourMap.put(flavour.getId(), flavour);
        parentVnfd.setFlavours(flavourMap);
        parentVnfd.setNode(node);
        validateFlavours(parentVnfd);
    }

    @Test
    void testValidateFlavourMissingArtifacts() {
        Path pathToParentVnfd = TestUtils.getResource(PARENT_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        ParentVnfd parentVnfd = NestedVnfdUtility.createParentVnfd(jsonData);
        Path pathToFlavour = TestUtils.getResource(SAMPLE_FLAVOUR_1_MISSING_ARTIFACTS_YAML).toAbsolutePath();
        JSONObject jsonDataFlavour = VnfdUtility.validateYamlCanBeParsed(pathToFlavour);
        Flavour flavour = getFlavour(jsonDataFlavour);
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonDataNode = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonDataNode, descriptorDetails);
        Map<String, Flavour> flavourMap = new LinkedHashMap<>();
        flavourMap.put(flavour.getId(), flavour);
        parentVnfd.setFlavours(flavourMap);
        parentVnfd.setNode(node);
        assertThatThrownBy(() -> validateFlavours(parentVnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Artifacts for the following flavour ids [sample-flavour-1] are invalid");
    }

    @Test
    void testValidateFlavourMissingMultipleArtifacts() {
        Path pathToParentVnfd = TestUtils.getResource(PARENT_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToParentVnfd);
        ParentVnfd parentVnfd = NestedVnfdUtility.createParentVnfd(jsonData);
        Path pathToFlavour = TestUtils.getResource(SAMPLE_FLAVOUR_1_MISSING_ARTIFACTS_YAML).toAbsolutePath();
        JSONObject jsonDataFlavour = VnfdUtility.validateYamlCanBeParsed(pathToFlavour);
        Flavour flavour = getFlavour(jsonDataFlavour);
        Path pathToFlavour1 = TestUtils.getResource(SAMPLE_FLAVOUR_2_MISSING_ARTIFACTS_YAML).toAbsolutePath();
        JSONObject jsonDataFlavour1 = VnfdUtility.validateYamlCanBeParsed(pathToFlavour1);
        Flavour flavour1 = getFlavour(jsonDataFlavour1);
        Path pathToNode = TestUtils.getResource(NODE_TYPE_VNFD_YAML).toAbsolutePath();
        JSONObject jsonDataNode = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        VnfDescriptorDetails descriptorDetails = new VnfDescriptorDetails();
        Node node = getNode(jsonDataNode, descriptorDetails);
        Map<String, Flavour> flavourMap = new LinkedHashMap<>();
        flavourMap.put(flavour.getId(), flavour);
        flavourMap.put(flavour1.getId(), flavour1);
        parentVnfd.setFlavours(flavourMap);
        parentVnfd.setNode(node);
        assertThatThrownBy(() -> validateFlavours(parentVnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageMatching("Artifacts for the following flavour ids \\[(sample-flavour-1, |sample-flavour-2, )"
                                            + "(sample-flavour-1|sample-flavour-2)] are invalid");
    }
}
