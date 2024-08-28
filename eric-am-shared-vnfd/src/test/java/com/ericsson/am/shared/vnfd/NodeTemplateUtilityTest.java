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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.ericsson.am.shared.vnfd.NodeTypeUtility.buildNodeType;
import static com.ericsson.am.shared.vnfd.TestData.buildExpectedMciopNode;
import static com.ericsson.am.shared.vnfd.TestData.buildExpectedOsContainer;
import static com.ericsson.am.shared.vnfd.TestData.buildExpectedOsContainerDeployableUnit;
import static com.ericsson.am.shared.vnfd.TestData.buildExpectedVirtualBlockStorage;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.EXAMPLE;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.INVALID_TARGET;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_COMPUTE_NODETEST;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_COMPUTE_NODETEST_MISSING_TOPOLOGY_TEMPLATE;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_COMPUTE_NODETEST_WITH_INVALID_VALUES_IN_VDU_PROFILE;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_DEPLOYABLE_MODULES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_INVALID_POLICY_MODEL_AND_COMPUTE_NODES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_INVALID_VDU_INSTANTIATION_LEVEL_TARGETS;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_REL4_AND_LEGACY_POLICY_MODEL_AND_COMPUTE_NODES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_REL4_POLICY_MODEL;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_UPDATED_POLICY_MODEL_AND_COMPUTE_NODES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_VIRTUAL_CP;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SEARCH_ENGINE;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Mciop;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCompute;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCp;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainer;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerDeployableUnit;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduProfile;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduVirtualBlockStorage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VirtualCp;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class NodeTemplateUtilityTest {

    @Test
    void testCreateNodeTemplate() {
        Path pathToNode = TestUtils.getResource(SAMPLE_COMPUTE_NODETEST).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);
        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
        assertEquals(2, nodeTemplate.getVduCompute().size());
    }

    @Test
    void testCreateNodeTemplateNoTopologyTemplate() {
        Path pathToNode = TestUtils.getResource(SAMPLE_COMPUTE_NODETEST_MISSING_TOPOLOGY_TEMPLATE).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);
        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
        assert (nodeTemplate.getVduCompute() == null);
        assert (!nodeTemplate.getInterfaces().isEmpty());
        assert (nodeTemplate.getType().equals("Ericsson.SGSN-MME.1_20_CXS101289_R81E08.cxp9025898_4r81e08"));
    }

    @Test
    void testCreateNodeTemplateVduProfileWithInvalidType() {
        Path pathToNode = TestUtils.getResource(SAMPLE_COMPUTE_NODETEST_WITH_INVALID_VALUES_IN_VDU_PROFILE).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd));
        assertEquals("Unable to parse VduCompute node", exception.getMessage());
    }

    @Test
    void testCreateNodeTemplateRel4AndLegacyNodes() {
        Path pathToNode = TestUtils.getResource(SAMPLE_VNFD_WITH_REL4_AND_LEGACY_POLICY_MODEL_AND_COMPUTE_NODES).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd));

        assertEquals("tosca.nodes.nfv.Vdu.Compute node is not allowed to be present in rel4 VNFD",
                     exception.getMessage());
    }

    @Test
    void testValidateVduComputeNodeNoName() {
        VduCompute vduCompute = buildVduComputeNode("", 2, 10);
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                NodeTemplateUtility.validateVduComputeNode(vduCompute));
        assertEquals("VduCompute name is a required field", exception.getMessage());
    }

    @Test
    void testValidateVduComputeNodeInvalidMinNumberOfInstances() {
        VduCompute vduCompute = buildVduComputeNode(SEARCH_ENGINE, -1, 10);
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                NodeTemplateUtility.validateVduComputeNode(vduCompute));
        assertEquals("VduProfile min_number_of_instances must be 0 or greater.", exception.getMessage());
    }

    @Test
    void testValidateVduComputeNodeInvalidMaxNumberOfInstances() {
        VduCompute vduCompute = buildVduComputeNode(SEARCH_ENGINE, 2, -1);
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                NodeTemplateUtility.validateVduComputeNode(vduCompute));
        assertEquals("VduProfile max_number_of_instances must be 0 or greater.", exception.getMessage());
    }

    @Test
    void testValidatePoliciesModelTargets() {
        Path pathToNode = TestUtils.getResource(SAMPLE_VNFD_WITH_UPDATED_POLICY_MODEL_AND_COMPUTE_NODES).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);

        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
        Policies policies = PolicyUtility.createAndValidatePolicies(vnfd);
        NodeTemplateUtility.validatePoliciesModelTargets(nodeTemplate, policies);
    }

    @Test
    void testValidateRel4PoliciesModelTargets() {
        Path pathToNode = TestUtils.getResource(SAMPLE_VNFD_WITH_REL4_POLICY_MODEL).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);

        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
        Policies policies = PolicyUtility.createAndValidatePolicies(vnfd);
        NodeTemplateUtility.validatePoliciesModelTargets(nodeTemplate, policies);
    }

    @Test
    void testScalingAspectTargetIncludesNameNotDefinedInComputeNode() {
        Path pathToNode = TestUtils.getResource(SAMPLE_VNFD_WITH_INVALID_POLICY_MODEL_AND_COMPUTE_NODES).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);
        List<String> expectedInvalidTargets = Collections.singletonList(SEARCH_ENGINE);

        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
        Policies policies = PolicyUtility.createAndValidatePolicies(vnfd);

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                NodeTemplateUtility.validatePoliciesModelTargets(nodeTemplate, policies));

        assertEquals("tosca.policies.nfv.VduInitialDelta has target name/s "
                             + expectedInvalidTargets + " which is not defined as a tosca.nodes.nfv.Vdu.Compute node.",
                     exception.getMessage());
    }

    @Test
    void testVduInstantiationLevelTargetIncludesNameNotDefinedInComputeNode() {
        Path pathToNode = TestUtils.getResource(SAMPLE_VNFD_WITH_INVALID_VDU_INSTANTIATION_LEVEL_TARGETS).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);
        List<String> expectedInvalidTargets = Collections.singletonList(INVALID_TARGET);

        NodeTemplate nodeTemplate = NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
        Policies policies = PolicyUtility.createAndValidatePolicies(vnfd);

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                NodeTemplateUtility.validatePoliciesModelTargets(nodeTemplate, policies));

        assertEquals("tosca.policies.nfv.VduInstantiationLevels has target name/s "
                             + expectedInvalidTargets + " which is not defined as a tosca.nodes.nfv.Vdu.Compute node.",
                     exception.getMessage());
    }

    @Test
    void testCreateNodeTemplateWithMciopAndOsContainerDeployableUnitAndOsContainerAndStorage() {
        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE);

        assertEquals(2, nodeTemplate.getMciop().size());
        assertEquals(2, nodeTemplate.getOsContainerDeployableUnit().size());
        assertEquals(3, nodeTemplate.getVduOsContainer().size());
        assertEquals(2, nodeTemplate.getVduVirtualBlockStorages().size());
    }

    @Test
    void testCreateNodeTemplateWithDeployableModules() {
        NodeTemplate nodeTemplate = createNodeTemplate(SAMPLE_DEPLOYABLE_MODULES);

        assertEquals(3, nodeTemplate.getDeploymentModules().size());
    }

    @Test
    void testCreateNodeTemplateWithoutMciopAndOsContainerDeployableUnitAndOsContainerAndStorage() {
        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_COMPUTE_NODETEST);

        assertEquals(0, nodeTemplate.getMciop().size());
        assertEquals(0, nodeTemplate.getOsContainerDeployableUnit().size());
        assertEquals(0, nodeTemplate.getVduOsContainer().size());
        assertEquals(0, nodeTemplate.getVduVirtualBlockStorages().size());
    }

    @Test
    void testCreateNodeTemplateMciopNode() {
        List<Mciop> expected = buildExpectedMciopNode();

        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE);
        List<Mciop> actual = nodeTemplate.getMciop();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void testCreateNodeTemplateOsContainerDeployableUnitNode() {
        List<VduOsContainerDeployableUnit> expected = buildExpectedOsContainerDeployableUnit();

        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE);
        List<VduOsContainerDeployableUnit> actual = nodeTemplate.getOsContainerDeployableUnit();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void testCreateNodeTemplateOsContainerNode() {
        List<VduOsContainer> expected = buildExpectedOsContainer();

        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE);
        List<VduOsContainer> actual = nodeTemplate.getVduOsContainer();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void testCreateNodeTemplateVirtualBlockStorageNode() {
        List<VduVirtualBlockStorage> expected = buildExpectedVirtualBlockStorage();

        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE);
        List<VduVirtualBlockStorage> actual = nodeTemplate.getVduVirtualBlockStorages();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void testCreateNodeTemplateVduCp() {
        List<VduCp> expected = buildExpectedVduCpList();

        NodeTemplate nodeTemplate = createNodeTemplate(
                SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES);
        List<VduCp> actual = nodeTemplate.getVduCps();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void testCreateNodeTemplateVirtualCp() {
        List<VirtualCp> expected = buildExpectedVirtualCpList();

        NodeTemplate nodeTemplate = createNodeTemplate(
              SAMPLE_VNFD_WITH_VIRTUAL_CP);
        List<VirtualCp> actual = nodeTemplate.getVirtualCps();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    private NodeTemplate createNodeTemplate(String path) {
        Path pathToNode = TestUtils.getResource(path).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeType = getNodeTypeObject(vnfd);

        return NodeTemplateUtility.createNodeTemplate(nodeType, vnfd);
    }


    private NodeType getNodeTypeObject(final JSONObject vnfd) {
        Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
        Map<String, InterfaceType> allInterfaceType = InterfaceTypeUtility.getInterfaceTypeFromVnfd(vnfd, allDataType);
        return buildNodeType(vnfd, allDataType, allInterfaceType);
    }

    private VduCompute buildVduComputeNode(String name, int min, int max) {
        VduCompute vduCompute = new VduCompute();
        vduCompute.setDescription(EXAMPLE);
        vduCompute.setName(name);
        vduCompute.setVduComputeKey(name);
        VduProfile vduProfile = new VduProfile();
        vduProfile.setMinNumberOfInstances(min);
        vduProfile.setMaxNumberOfInstances(max);
        vduCompute.setVduProfile(vduProfile);
        return vduCompute;
    }

    private List<VirtualCp> buildExpectedVirtualCpList() {
        VirtualCp virtualCp1 = new VirtualCp("test-cnf-vnfc1_virtual_cp", Map.of("target", "test-cnf-vnfc1"));
        VirtualCp virtualCp2 = new VirtualCp("eric-pm-bulk-reporter_virtual_cp", Map.of("target", "eric-pm-bulk-reporter"));
        return Arrays.asList(virtualCp1, virtualCp2);
    }


    private List<VduCp> buildExpectedVduCpList() {
        VduCp vduCp1 = new VduCp("Search-Engine-DB_vdu_cp_normal", 1, Map.of("virtual_binding", "Search-Engine-DB"));
        VduCp vduCp2 = new VduCp("Search-Engine_vdu_cp_macvlan", 1, Map.of("virtual_binding", "Search-Engine"));
        return Arrays.asList(vduCp1, vduCp2);
    }
}