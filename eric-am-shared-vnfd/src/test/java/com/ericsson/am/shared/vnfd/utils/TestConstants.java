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
package com.ericsson.am.shared.vnfd.utils;

public final class TestConstants {

    private TestConstants() {
    }

    public static final String EXAMPLE = "example";
    public static final String SEARCH_ENGINE = "Search-Engine";
    public static final String NODE_TYPE_VNFD_YAML = "nestedVnfd/validNestedVnfDescriptorFiles/node_type_vnfd.yaml";
    public static final String SAMPLE_FLAVOUR_1_YAML = "nestedVnfd/validNestedVnfDescriptorFiles/sample-flavour1-vnfd.yaml";
    public static final String SAMPLE_FLAVOUR_2_YAML = "nestedVnfd/validNestedVnfDescriptorFiles/sample-flavour2-vnfd.yaml";
    public static final String NODE_TYPE_NAME_NESTED_VNFD = "Ericsson.SAMPLE-VNF.1_25_CXS101289_R81E08.cxp9025898_4r81e08";
    public static final String SAMPLE_FLAVOUR_1 = "sample-flavour-1";
    public static final String SAMPLE_FLAVOUR_2 = "sample-flavour-2";
    public static final String SAMPLE_FLAVOUR_1_MISSING_ARTIFACTS_YAML = "nestedVnfd/flavour1-without-artifacts.yaml";
    public static final String SAMPLE_FLAVOUR_2_MISSING_ARTIFACTS_YAML = "nestedVnfd/flavour2-without-artifacts.yaml";
    public static final String PARENT_VNFD_YAML = "nestedVnfd/validNestedVnfDescriptorFiles/sample-multichart-descriptor.yaml";
    public static final String SAMPLE_COMPUTE_NODETEST = "vnfd/valid_vnfd_with_topology_template_and_vdu_compute_nodes.yaml";
    public static final String SAMPLE_MCIOP_OS_CONTAINER_DEPLOYABLE_UNIT_OS_CONTAINER_STORAGE = "vnfd"
            + "/valid_vnfd_with_mciop_with_os_container_deployable_unit_with_os_container_with_storage.yaml";
    public static final String SAMPLE_DEPLOYABLE_MODULES = "vnfd"
            + "/validation/deployableModules/valid_vnfd_with_deployable_modules.yaml";
    public static final String SAMPLE_COMPUTE_NODETEST_MISSING_TOPOLOGY_TEMPLATE = "vnfd/vnfd_with_missing_topology_template.yaml";
    public static final String SAMPLE_VNFD_WITH_INVALID_POLICY_MODEL_AND_COMPUTE_NODES
            = "vnfd/invalid_vnfd_with_updated_policy_model_and_compute_nodes.yaml";
    public static final String SAMPLE_VNFD_WITH_UPDATED_POLICY_MODEL_AND_COMPUTE_NODES
            = "vnfd/valid_vnfd_with_updated_policy_model_and_compute_nodes.yaml";
    public static final String SAMPLE_VNFD_WITH_REL4_AND_LEGACY_POLICY_MODEL_AND_COMPUTE_NODES
            = "vnfd/invalid_vnfd_rel4_with_mciop_and_compute_scaling_target.yaml";
    public static final String SAMPLE_VNFD_WITH_REL4_POLICY_MODEL
            = "vnfd/valid_vnfd_rel4_with_mciop_scaling_target.yaml";
    public static final String SAMPLE_COMPUTE_NODETEST_WITH_INVALID_VALUES_IN_VDU_PROFILE
            = "vnfd/valid_vnfd_with_topology_template_and_invalid_values_in_vdu_profile.yaml";
    public static final String SAMPLE_VNFD_WITH_INVALID_VDU_INSTANTIATION_LEVEL_TARGETS
            = "vnfd/invalid_vnfd_with_vdu_instantiation_level_target_missing_from_compute_nodes.yaml";
    public static final String INVALID_TARGET = "Invalid-Target";
    public static final String VALID_VNFD_WITH_POLICIES = "vnfd/vnfd_with_policies.yaml";
    public static final String MODIFIABLE_ATTRIBUTE_EXTENSION_KEY = "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions";
    public static final String VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES = "vnfd/valid_vnfd_with_VnfInfoModifiableAttributes_in_data_type.yaml";
    public static final String SAMPLE_VNFD_WITH_VIRTUAL_LINK_IN_VDU_CP = "vnfd/invalid_vnfd_with_internal_virtual_link_in_VduCp.yaml";
    public static final String SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES = "vnfd/vnfd_with_network_data_types_support.yaml";
    public static final String SAMPLE_VNFD_WITH_VIRTUAL_CP = "vnfd/vnfd_with_virtualCp.yaml";
}
