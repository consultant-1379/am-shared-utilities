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
package com.ericsson.am.shared.vnfd.validation;

import static com.ericsson.am.shared.vnfd.utils.Constants.ACTION_IS_MISSING_ASSOCIATION_WITH_ANY_VNF_PACKAGE_CHANGE_INTERFACE;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTRA_VNF_PACKAGE_CHANGE_POLICY_DEFINED;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACE_TYPE_NOT_DEFINED;
import static com.ericsson.am.shared.vnfd.utils.Constants.MISSING_FAILURE_PATTERN;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_PACKAGE_CHANGE_POLICY_NOT_DEFINED_FOR_VNF_PACKAGE_CHANGE_INTERFACE;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_BASE;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_DELETE_USAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_ENDING;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_INSTALL_USAGE;

import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;

@SuppressWarnings("squid:S2699")
class RollbackOperationValidatorTest extends BaseOperationValidatorTest {
    private static final String VNFD_VNFD_TOSCA_1_2_MULTI_B_YAML = "vnfd/vnfd_tosca_1_2_25_multi_b.yaml";
    private static final String VNFD_VNFD_TOSCA_1_3_MULTI_B_YAML = "vnfd/vnfd_tosca_1_3_multi_b.yaml";
    private static final String ROLLBACK_INTERFACE_AND_WITHOUT_ROLLBACK_POLICIES_YAML
            = "vnfd/invalid_vnfd_with_rollback_interface_and_without_rollback_policies.yaml";
    private static final String ROLLBACK_POLICIES_PATTERN_COMMAND_NOT_FOUND_YAML
            = "vnfd/invalid_vnfd_with_rollback_policies_pattern_command_not_found.yaml";
    private static final String ROLLBACK_POLICIES_FAILURE_PATTERN_UNABLE_TO_PARSE_YAML
            = "vnfd/invalid_vnfd_with_rollback_policies_failure_pattern_unable_to_parse.yaml";
    private static final String ROLLBACK_POLICIES_FAILURE_PATTERN_KEY_NOT_FOUND_YAML
            = "vnfd/invalid_vnfd_with_rollback_policies_failure_pattern_key_not_found.yaml";
    private static final String VNFD_WITH_VALID_DIFFERENT_ROLLBACK_PATTERNS
            = "vnfd/valid_vnfd_with_different_valid_rollback_patterns.yaml";

    @Test
    void testTosca1v2VnfdHasValidRollbackDefinitionRollbackIsSupported() {
        runSuccessRollbackValidation(VNFD_VNFD_TOSCA_1_2_MULTI_B_YAML);
    }

    @Test
    void testTosca1v3VnfdHasValidRollbackDefinitionRollbackIsSupported() {
        runSuccessRollbackValidation(VNFD_VNFD_TOSCA_1_3_MULTI_B_YAML);
    }

    @Test
    void testDifferentValidRollbackPatterns() {
        runSuccessRollbackValidation(VNFD_WITH_VALID_DIFFERENT_ROLLBACK_PATTERNS);
    }

    @Test
    void testTosca1v2VnfdHasNoRollbackPoliciesRollbackIsNotSupported() {
        runFailRollbackValidation(ROLLBACK_INTERFACE_AND_WITHOUT_ROLLBACK_POLICIES_YAML,
                                  EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                                          VNF_PACKAGE_CHANGE_POLICY_NOT_DEFINED_FOR_VNF_PACKAGE_CHANGE_INTERFACE);
    }

    @Test
    void testTosca1v2VnfdHasInvalidCommandInRollbackPatternRollbackIsNotSupported() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Command command_not_found for helm chart helm_package1 defined in vnf package change " +
                "pattern not supported. The supported commands are: [ROLLBACK, INSTALL, UPGRADE, DELETE, DELETE_PVC]";
        runFailRollbackValidation(ROLLBACK_POLICIES_PATTERN_COMMAND_NOT_FOUND_YAML, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasMissingRollbackPatternRollbackIsNotSupportedWithException() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Rollback/failure pattern is incorrectly defined, unable to parse";
        runFailRollbackValidation(ROLLBACK_POLICIES_FAILURE_PATTERN_UNABLE_TO_PARSE_YAML, errorMessage);
    }

    @Test
    void testTosca1v2VnfdUsesNonExistentArtifactInRollbackPatternRollbackIsNotSupportedWithException() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Helmchart not_found with command rollback defined in vnf package change pattern " +
                "not found in artifacts section of vnfd";
        runFailRollbackValidation(ROLLBACK_POLICIES_FAILURE_PATTERN_KEY_NOT_FOUND_YAML, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasInvalidDeletePvcCommandInRollbackPatternRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/valid_vnfd_with_rollback_policies_new_format_invalid_delete_pvc.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Command delete_pvc[app=trigger2 for helm chart helm_package2 defined in " +
                "vnf package change pattern not supported. The supported commands are: " +
                "[ROLLBACK, INSTALL, UPGRADE, DELETE, DELETE_PVC]";
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasDowngradeOperationWithNoAssociationWithPolicyRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_missing_association_in_downgrade_operation.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                String.format(ACTION_IS_MISSING_ASSOCIATION_WITH_ANY_VNF_PACKAGE_CHANGE_INTERFACE, "change_to");
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasInvalidRollbackAtFailurePatternRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_policies_failure_pattern_unable_to_parse.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Rollback/failure pattern is incorrectly defined, unable to parse";
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasInvalidCommandInRollbackPatternRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_policies_pattern_command_not_found.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Command command_not_found for helm chart helm_package1 defined in vnf package change " +
                "pattern not supported. The supported commands are: [ROLLBACK, INSTALL, UPGRADE, DELETE, DELETE_PVC]";
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasMissingRollbackPolicyRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_interface_and_without_rollback_policies.yaml";
        runFailRollbackValidation(vnfdPath, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                VNF_PACKAGE_CHANGE_POLICY_NOT_DEFINED_FOR_VNF_PACKAGE_CHANGE_INTERFACE);
    }

    @Test
    void testTosca1v2VnfdHasMissingRollbackPolicyForMultipleOperationsRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_missing_rollback_policy.yaml";
        runFailRollbackValidation(vnfdPath, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY);
    }

    @Test
    void testTosca1v2VnfdHasNoPolicyKeyInTopologyTemplateRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_interface_and_without_policies.yaml";
        runFailRollbackValidation(vnfdPath, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                VNF_PACKAGE_CHANGE_POLICY_NOT_DEFINED_FOR_VNF_PACKAGE_CHANGE_INTERFACE);
    }

    @Test
    void testTosca1v2VnfdHasInvalidChartInRollbackAtFailurePatterRollbackIsNotSupportedInVnfd() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_policies_failure_pattern_key_not_found.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Helmchart not_found with command rollback defined in vnf package change pattern " +
                "not found in artifacts section of vnfd";
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasInvalidCommandInRollbackAtFailurePatternRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_policies_failure_pattern_command_not_found.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "Command command_not_found for helm chart helm_package2 defined in vnf package change " +
                "pattern not supported. The supported commands are: [ROLLBACK, INSTALL, UPGRADE, DELETE, DELETE_PVC]";
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasNoFailurePatternForOneHelmChartRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_rollback_policies_new_format_missing_failure_pattern.yaml";
        runFailRollbackValidation(vnfdPath, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                String.format(MISSING_FAILURE_PATTERN, "[helm_package1]"));
    }

    @Test
    void testTosca1v2VVnfdHasExtraRollbackPolicyRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_extra_rollback_policy.yaml";
        runFailRollbackValidation(vnfdPath, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                EXTRA_VNF_PACKAGE_CHANGE_POLICY_DEFINED);
    }

    @Test
    void testTosca1v2HasMissingInterfaceTypeDefinitionForDowngradeInterfaceRollbackIsNotSupportedWithException() {
        String vnfdPath = "vnfd/vnfd_tosca_1_2_25_multi_b_wrong_node_types_interfaces.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                String.format(INTERFACE_TYPE_NOT_DEFINED,
                              "ericsson.interfaces.nfv.EricssonChangeCurrentVnfPackage2");
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testVnfdRollbackPatternWithWrongEndingWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_wrong_rollback_pattern_last_command.yaml";
        String errorMessage =
                EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                        String.format(WRONG_ROLLBACK_PATTERN_BASE,
                                      "helm_package2", WRONG_ROLLBACK_PATTERN_ENDING);
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testVnfdRollbackPatternWithInstallUsageWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_wrong_rollback_pattern_install_usage.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                String.format(WRONG_ROLLBACK_PATTERN_BASE,
                              "helm_package2", WRONG_ROLLBACK_PATTERN_INSTALL_USAGE);
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testVnfdRollbackPatternWithUpgradeUsageWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_wrong_rollback_pattern_upgrade_usage.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                String.format(WRONG_ROLLBACK_PATTERN_BASE,
                              "helm_package1", String.format(WRONG_ROLLBACK_PATTERN_DELETE_USAGE, "upgrade"));
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testVnfdRollbackPatternWithUpgradeAfterDeletePvcUsageWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_wrong_rollback_pattern_upgrade_after_delete_pvc.yaml";
        String errorMessage =
                EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                        String.format(WRONG_ROLLBACK_PATTERN_BASE,
                                      "helm_package1", String.format(WRONG_ROLLBACK_PATTERN_DELETE_USAGE, "upgrade"));
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    @Test
    void testVnfdRollbackPatternWithRollbackUsageWithException() {
        String vnfdPath = "vnfd/invalid_vnfd_with_wrong_rollback_pattern_rollback_usage.yaml";
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                String.format(WRONG_ROLLBACK_PATTERN_BASE,
                              "helm_package1", String.format(WRONG_ROLLBACK_PATTERN_DELETE_USAGE, "rollback"));
        runFailRollbackValidation(vnfdPath, errorMessage);
    }

    private void runSuccessRollbackValidation(final String vnfdPath) {
        runSuccessOperationValidation(vnfdPath, LCMOperationsValidatorsEnum.ROLLBACK);
    }

    private void runFailRollbackValidation(final String vnfdPath, final String errorMessage) {
        runFailOperationValidation(vnfdPath, errorMessage, LCMOperationsValidatorsEnum.ROLLBACK);
    }
}