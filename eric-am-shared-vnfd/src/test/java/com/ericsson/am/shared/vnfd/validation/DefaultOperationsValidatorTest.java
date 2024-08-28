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

import static com.ericsson.am.shared.vnfd.utils.Constants.EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;
import com.ericsson.am.shared.vnfd.utils.VnfdUtils;

@SuppressWarnings("squid:S2699")
class DefaultOperationsValidatorTest extends BaseOperationValidatorTest {

    @ParameterizedTest
    @EnumSource(value = LCMOperationsValidatorsEnum.class, names = {
        "INSTANTIATE",
        "TERMINATE",
        "CHANGE_VNFPKG" })
    void testNodeTypeHasNoDerivedFromPropertyOperationIsNotSupported(final LCMOperationsValidatorsEnum operation) {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "derived_from not defined for node Ericsson.SGSN-MME.1_20_CXS101289_R81E08.cxp9025898_4r81e08";
        runFailDefaultValidation(VnfdUtils.INVALID_VNFD_WITHOUT_DERIVED_FORM_ATTRIBUTE, errorMessage, operation);
    }

    @ParameterizedTest
    @EnumSource(value = LCMOperationsValidatorsEnum.class, names = {
        "INSTANTIATE",
        "TERMINATE",
        "CHANGE_VNFPKG" })
    void testNodeTypeIsNotDerivedFromToscaNodesNfvVnfOperationIsNotSupportedWithException(
            final LCMOperationsValidatorsEnum operation) {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "derived_from only supports tosca.nodes.nfv.VNF";
        runFailDefaultValidation(VnfdUtils.INVALID_VNFD_WITH_WRONG_DERIVED_FORM_VALUE, errorMessage, operation);
    }

    @ParameterizedTest
    @EnumSource(value = LCMOperationsValidatorsEnum.class, names = {
        "INSTANTIATE",
        "TERMINATE",
        "CHANGE_VNFPKG" })
    void testNodeTypeDerivedFromToscaNodesNfvVnfOperationIsSupported(final LCMOperationsValidatorsEnum operation) {
        runSuccessDefaultValidation(operation);
    }

    private void runSuccessDefaultValidation(final LCMOperationsValidatorsEnum operation) {
        runSuccessOperationValidation(VnfdUtils.VALID_VNFD_FILE, operation);
    }

    private void runFailDefaultValidation(final String vnfdPath, final String errorMessage, final LCMOperationsValidatorsEnum operation) {
        runFailOperationValidation(vnfdPath, errorMessage, operation);
    }
}