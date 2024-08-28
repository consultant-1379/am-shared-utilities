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

import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;
import com.ericsson.am.shared.vnfd.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.CHANGE_CURRENT_PACKAGE;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.CHANGE_VNFPKG;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.HEAL;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.INSTANTIATE;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.MODIFY_INFO;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.ROLLBACK;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.SCALE;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.SYNC;
import static com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum.TERMINATE;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATION_MISSING_IN_VNF_LCM_INTERFACES;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ROLLBACK_OPERATION_REQUIRES_INTERFACE_DERIVED_FROM_TOSCA_CCVP;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_CONTAINS_ONLY_NON_SCALABLE_VDU;
import static org.assertj.core.api.Assertions.assertThat;

class ToscaSupportedOperationValidatorTest {
    private static final String VNFD_TOSCA_1_2_MULTI_B_YAML = "vnfd/vnfd_tosca_1_2_25_multi_b.yaml";
    private static final String VNFD_TOSCA_1_3_MULTI_B_YAML = "vnfd/vnfd_tosca_1_3_multi_b.yaml";

    private static final String INVALID_VNFD_WITH_NEGATIVE_DELTA_YAML = "vnfd/invalid_vnfd_with_negative_delta.yaml";
    private static final String MISSING_ROLLBACK_POLICY_YAML = "vnfd/invalid_vnfd_with_missing_rollback_policy.yaml";
    private static final String VNFD_TOSCA_1_2_WITHOUT_OPERATIONS = "vnfd/vnfd_tosca_1_2_25_without_operations.yaml";
    private static final String POLICIES_FAILURE_PATTERN_KEY_NOT_FOUND_YAML
            = "vnfd/invalid_vnfd_with_rollback_policies_failure_pattern_key_not_found.yaml";
    private static final String VNFD_TOSCA_1_2_ALL_OPERATIONS_YAML
            = "vnfd/vnfd_tosca_1_2_25_all_operations.yaml";
    private static final String VNFD_TOSCA_1_3_ALL_OPERATIONS_YAML
            = "vnfd/vnfd_tosca_1_3_all_operations.yaml";
    private static final String VNFD_TOSCA_1_3_ALL_OPERATIONS_WITH_DIFFERENT_ORDER_YAML
            = "vnfd/vnfd_tosca_1_3_all_operations_with_different_order.yaml";
    private static final String REL4_VNFD_WITH_ONE_SCALABLE_VDU
            = "vnfd/valid_vnfd_rel4_with_one_scalable_vdu.yaml";
    private static final String REL4_VNFD_WITH_SCALING_ASPECTS_AND_ALL_NON_SCALABLE_VDU
            = "vnfd/valid_vnfd_rel4_with_scaling_aspects_and_all_non_scalable_vdu.yaml";
    private static final String REL4_VNFD_WITHOUT_SCALING_ASPECTS
            = "vnfd/valid_vnfd_rel4_without_scaling_aspects.yaml";

    @ParameterizedTest
    @ValueSource(strings = {
        VNFD_TOSCA_1_2_ALL_OPERATIONS_YAML,
        VNFD_TOSCA_1_3_ALL_OPERATIONS_YAML,
        VNFD_TOSCA_1_3_ALL_OPERATIONS_WITH_DIFFERENT_ORDER_YAML})
    void testValidVnfdAllOperationAreSupported(String vnfdPath) throws IOException {
        List<OperationDetail> vnfdSupportedOperations = getOperationDetails(vnfdPath);
        assertThat(vnfdSupportedOperations).isNotNull().hasSize(9);
        assertThat(vnfdSupportedOperations.stream().allMatch(OperationDetail::isSupported)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        VNFD_TOSCA_1_2_MULTI_B_YAML,
        VNFD_TOSCA_1_3_MULTI_B_YAML})
    void testValidVnfdAllOperationExceptForHealAreSupported(String vnfdPath) throws IOException {
        List<OperationDetail> operationDetails = getOperationDetails(vnfdPath);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).isNotNull().hasSize(9);
        validateOperationsAreSupported(operationDetailMap,
                                       INSTANTIATE, TERMINATE, CHANGE_VNFPKG, CHANGE_CURRENT_PACKAGE, SCALE, ROLLBACK, MODIFY_INFO, SYNC);
        validateOperationIsNotSupportedWithError(operationDetailMap, HEAL,
                                                 String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, HEAL.getOperation()));
    }

    @Test
    void testInvalidVnfdScaleAndRollbackValidationsFailWithException() throws IOException {
        String scaleErrorMessage
                = "properties.deltas[delta_1].numberOfInstances: number of instance can be only positive";

        List<OperationDetail> operationDetails = getOperationDetails(INVALID_VNFD_WITH_NEGATIVE_DELTA_YAML);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).isNotNull().hasSize(9);
        validateOperationsAreSupported(operationDetailMap, INSTANTIATE, TERMINATE, CHANGE_VNFPKG, CHANGE_CURRENT_PACKAGE, MODIFY_INFO, SYNC);
        validateOperationIsNotSupportedWithError(operationDetailMap, HEAL, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, "heal"));
        validateOperationIsNotSupportedWithError(operationDetailMap, ROLLBACK, ROLLBACK_OPERATION_REQUIRES_INTERFACE_DERIVED_FROM_TOSCA_CCVP);
        validateOperationIsNotSupportedWithError(
                operationDetailMap, SCALE, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + scaleErrorMessage);
    }

    @Test
    void testVnfdHasMinimalConfigurationOnlyBasicOperationsAreSupported() throws IOException {
        List<OperationDetail> operationDetails = getOperationDetails(VNFD_TOSCA_1_2_WITHOUT_OPERATIONS);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).isNotNull().hasSize(9);

        validateOperationIsSupported(operationDetailMap, INSTANTIATE);
        validateOperationIsSupported(operationDetailMap, TERMINATE);
        validateOperationIsSupported(operationDetailMap, CHANGE_VNFPKG);
        validateOperationIsSupported(operationDetailMap, CHANGE_CURRENT_PACKAGE);
        validateOperationIsSupported(operationDetailMap, MODIFY_INFO);
        validateOperationIsSupported(operationDetailMap, SYNC);
        validateOperationIsNotSupportedWithError(
                operationDetailMap, SCALE, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, LCMOperationsEnum.SCALE.getOperation()));
        validateOperationIsNotSupportedWithError(
                operationDetailMap, HEAL, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, HEAL.getOperation()));
        validateOperationIsNotSupportedWithError(operationDetailMap, ROLLBACK, ROLLBACK_OPERATION_REQUIRES_INTERFACE_DERIVED_FROM_TOSCA_CCVP);
    }

    @Test
    void testInvalidVnfdRollbackValidationFailsWithException() throws IOException {
        String rollbackErrorMessage = "Helmchart not_found with command rollback defined in vnf package change " +
                "pattern not found in artifacts section of vnfd";

        List<OperationDetail> operationDetails = getOperationDetails(POLICIES_FAILURE_PATTERN_KEY_NOT_FOUND_YAML);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).isNotNull().hasSize(9);
        validateOperationsAreSupported(operationDetailMap, INSTANTIATE, TERMINATE, CHANGE_VNFPKG, CHANGE_CURRENT_PACKAGE, MODIFY_INFO, SYNC);
        validateOperationIsNotSupportedWithError(
                operationDetailMap, HEAL, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, HEAL.getOperation()));
        validateOperationIsNotSupportedWithError(operationDetailMap, SCALE,
                                                 String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, LCMOperationsEnum.SCALE.getOperation()));
        validateOperationIsNotSupportedWithError(
                operationDetailMap, ROLLBACK, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + rollbackErrorMessage);
    }

    @Test
    void testToscaSupportedOperationValidatorFailVnfdMissingRollbackPolicy() throws IOException {
        List<OperationDetail> operationDetails = getOperationDetails(MISSING_ROLLBACK_POLICY_YAML);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).isNotNull().hasSize(9);
        validateOperationsAreSupported(operationDetailMap, INSTANTIATE, TERMINATE, CHANGE_VNFPKG, CHANGE_CURRENT_PACKAGE, MODIFY_INFO, SYNC);
        validateOperationIsNotSupportedWithError(
                operationDetailMap, HEAL, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, HEAL.getOperation()));
        validateOperationIsNotSupportedWithError(
                operationDetailMap, SCALE, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, LCMOperationsEnum.SCALE.getOperation()));
        validateOperationIsNotSupportedWithError(
                operationDetailMap, ROLLBACK,
                EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY);
    }

    @Test
    void testToscaSupportedOperationValidatorVnfdWithScalableVdusShouldSupportScaleOperation() throws IOException {
        List<OperationDetail> operationDetails = getOperationDetails(REL4_VNFD_WITH_ONE_SCALABLE_VDU);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).hasSize(9);
        validateOperationIsSupported(operationDetailMap, SCALE);
    }

    @ParameterizedTest
    @ValueSource(strings = { REL4_VNFD_WITHOUT_SCALING_ASPECTS, REL4_VNFD_WITH_SCALING_ASPECTS_AND_ALL_NON_SCALABLE_VDU })
    void testToscaSupportedOperationValidatorVnfdWithScalingAspectsAndAllNonScalableVduShouldNotSupportScaleOperation(String vnfdPath)
            throws IOException {
        List<OperationDetail> operationDetails = getOperationDetails(vnfdPath);
        Map<String, OperationDetail> operationDetailMap = getOperationDetailMap(operationDetails);

        assertThat(operationDetails).hasSize(9);
        validateOperationIsNotSupportedWithError(operationDetailMap, SCALE, VNFD_CONTAINS_ONLY_NON_SCALABLE_VDU);
    }

    private List<OperationDetail> getOperationDetails(String vnfdPath) throws IOException {
        String vnfd = TestUtils.readDataFromFile(vnfdPath);
        return ToscaSupportedOperationValidator.getVnfdSupportedOperations(vnfd);
    }

    private Map<String, OperationDetail> getOperationDetailMap(List<OperationDetail> operationDetails) {
        return operationDetails.stream()
                .collect(Collectors.toMap(OperationDetail::getOperationName, operationDetail -> operationDetail));
    }

    private void validateOperationsAreSupported(Map<String, OperationDetail> operationDetailMap,
                                                LCMOperationsValidatorsEnum... operations) {
        for (LCMOperationsValidatorsEnum operation : operations) {
            validateOperationIsSupported(operationDetailMap, operation);
        }
    }

    private void validateOperationIsSupported(Map<String, OperationDetail> operationDetailMap,
                                              LCMOperationsValidatorsEnum operation) {
        assertThat(operationDetailMap.get(operation.getOperation()).isSupported()).isTrue();
        assertThat(operationDetailMap.get(operation.getOperation()).getErrorMessage()).isNull();
    }

    private void validateOperationIsNotSupportedWithError(Map<String, OperationDetail> operationDetailMap,
                                                          LCMOperationsValidatorsEnum operation, String errorMessage) {
        assertThat(operationDetailMap.get(operation.getOperation()).isSupported()).isFalse();
        assertThat(operationDetailMap.get(operation.getOperation()).getErrorMessage()).isEqualTo(errorMessage);
    }
}