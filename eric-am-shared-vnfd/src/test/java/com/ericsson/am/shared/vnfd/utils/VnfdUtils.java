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

public final class VnfdUtils {

    private VnfdUtils() {
    }

    public static final String VNFD_INTERFACE_PARSING_VNFD_1_2_WITHOUT_VNFLCM_IN_NODE_TYPES =
            "vnfd/interfaceParsing/vnfd_1_2_without_vnflcm_in_node_types.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_EMPTY_VNFLCM_IN_NODE_TYPES =
            "vnfd/interfaceParsing/vnfd_1_2_with_empty_vnflcm_in_node_types.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_EMPTY_INTERFACE_NODE_TEMPLATE =
            "vnfd/interfaceParsing/vnfd_1_2_with_empty_interface_node_template.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_OUT_INTERFACE_NODE_TEMPLATE =
            "vnfd/interfaceParsing/vnfd_1_2_with_out_interface_node_template.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_OUT_OPERATIONS =
            "vnfd/interfaceParsing/vnfd_1_3_with_out_operations.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_EMPTY_INPUTS_AND_HELM_PACKAGES =
            "vnfd/interfaceParsing/vnfd_1_3_with_empty_inputs_and_helm_packages.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_OUT_INSTANTIATE_CCVP =
            "vnfd/interfaceParsing/vnfd_1_3_with_out_instantiate_ccvp.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_WITH_MIXED_HELM_ARTIFACTS_IN_INTERFACES =
            "vnfd/interfaceParsing/vnfd_with_mixed_helm_artifacts_in_interfaces.yaml";
    public static final String VNFD_VNFD_WITH_VIRTUAL_CP_YAML = "vnfd/vnfd_with_virtualCp.yaml";
    public static final String VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE_AND_NOT_DEFINED_HELM_PACKAGES =
            "vnfd/interfaceParsing/valid_vnfd_without_node_template_and_not_defined_helm_packages.yaml";
    public static final String VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE_HELM_PACKAGES =
            "vnfd/interfaceParsing/valid_vnfd_without_node_template_helm_packages.yaml";
    public static final String VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE =
            "vnfd/interfaceParsing/valid_vnfd_without_node_template.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_NODE_TEMPLATE =
            "vnfd/interfaceParsing/vnfd_1_3_with_node_template_full.yaml";
    public static final String VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_NODE_TEMPLATE =
            "vnfd/interfaceParsing/vnfd_1_2_with_node_template_full.yaml";
    public static final String VNFD_INVALID_DM_VNFD_WITH_OUT_INSTANTIATE =
            "vnfd/invalid_dm_vnfd_with_out_instantiate.yaml";
    public static final String VNFD_INVALID_DM_VNFD_WITH_OUT_VNFLCM =
            "vnfd/invalid_dm_vnfd_with_out_vnflcm.yaml";
    public static final String VNFD_VALID_VNFD_WITH_ROLLBACK_POLICIES =
            "vnfd/valid_vnfd_with_rollback_policies.yaml";
    public static final String VNFD_VALID_VNFD_WITH_INSTANTIATE_HELM_PACKAGE_AS_JSON_OBJECT =
            "vnfd/valid_vnfd_with_instantiate_helm_package_as_json_object.yaml";
    public static final String VNFD_VALID_VNFD_WITH_MULTIPLE_ROLLBACK_OPERATION =
            "vnfd/valid_vnfd_with_multiple_rollback_operation.yaml";
    public static final String VNFD_VALID_VNFD_WITH_NODE_TYPE_DEFINITION_IN_NODE_TEMPLATES =
            "vnfd/valid_vnfd_with_node_type_definition_in_node_templates.yaml";

    public static final String ADDITIONAL_PARAMETERS_TYPE_KEY_MUST_BE_OF_TYPE_STRING
            = "Additional parameters type key must be of type String error message.";
    public static final String VALID_VNFD_FILE =
            "vnfd/valid_vnfd.yaml";
    public static final String VALID_NOT_DM_VNFD_FILE =
            "vnfd/valid_not_dm_vnfd.yaml";
    public static final String ALL_OPERATION_EMPTY_HELM_CHARTS_VNFD =
            "vnfd/invalid_all_empty_operation_helm_charts.yaml";
    public static final String VALID_DM_VNFD_FILE =
            "vnfd/valid_dm_vnfd.yaml";
    public static final String INVALID_COUNT_HELM_CHARTS_VNFD_FILE_CCVP =
            "vnfd/invalid_dm_vnfd_ccvp.yaml";
    public static final String INVALID_COUNT_HELM_CHARTS_VNFD_FILE_INSTANTIATE =
            "vnfd/invalid_dm_vnfd_instantiate.yaml";
    public static final String INVALID_COUNT_HELM_CHARTS_VNFD_FILE_SCALE =
            "vnfd/invalid_dm_vnfd_scale.yaml";
    public static final String INVALID_COUNT_HELM_CHARTS_VNFD_FILE_SCALE_SAME_HELM =
            "vnfd/invalid_dm_vnfd_scale_same_helm.yaml";
    public static final String INVALID_COUNT_HELM_CHARTS_VNFD_FILE_TERMINATE =
            "vnfd/invalid_dm_vnfd_terminate.yaml";

    public static final String INVALID_HEAL_VNFD_WITH_MISSING_TYPE
            = "vnfd/invalid_heal_vnfd_with_missing_lcm_operations_type.yaml";
    public static final String INVALID_HEAL_VNFD_MISSING_DEFAULT_CONFIG
            = "vnfd/invalid_heal_vnfd_with_missing_default_configuration.yaml";
    public static final String INVALID_HEAL_VNFD_MISSING_HEAL_CONFIG
            = "vnfd/invalid_heal_vnfd_with_missing_heal_config.yaml";
    public static final String INVALID_HEAL_VNFD_WITH_MISSING_LCM_OPERATIONS
            = "vnfd/invalid_heal_vnfd_with_missing_lcm_operations.yaml";
    public static final String INVALID_HEAL_VNFD_MISSING_CAUSES
            = "vnfd/invalid_heal_vnfd_with_missing_causes.yaml";
    public static final String INVALID_HEAL_VNFD_WITH_INVALID_CAUSES
            = "vnfd/invalid_heal_vnfd_with_invalid_causes.yaml";
    public static final String INVALID_HEAL_VNFD_WITH_MULTIPLE_CAUSES
            = "vnfd/invalid_heal_vnfd_with_multiple_causes.yaml";

    public static final String INVALID_VNFD_WITHOUT_DERIVED_FORM_ATTRIBUTE
            = "vnfd/invalid_vnfd_without_derived_from_attr.yaml";
    public static final String INVALID_VNFD_WITH_WRONG_DERIVED_FORM_VALUE
            = "vnfd/invalid_vnfd_with_wrong_derived_from_value.yaml";

    public static final String NODE_TYPE_NAME = "Ericsson.SGSN-MME.1_20_CXS101289_R81E08.cxp9025898_4r81e08";
}
