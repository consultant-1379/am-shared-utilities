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
import static com.ericsson.am.shared.vnfd.utils.Constants.LCM_OPERATIONS_MANDATORY;
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATION_MISSING_IN_VNF_LCM_INTERFACES;

import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;
import com.ericsson.am.shared.vnfd.utils.VnfdUtils;

@SuppressWarnings("squid:S2699")
class HealOperationValidatorTest extends BaseOperationValidatorTest {

    @Test
    void nodeTypeHasNoDerivedFromKeyHealIsNotSupported() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "derived_from not defined for node Ericsson.SGSN-MME"
                + ".1_20_CXS101289_R81E08.cxp9025898_4r81e08";
        runFailHealValidation(VnfdUtils.INVALID_VNFD_WITHOUT_DERIVED_FORM_ATTRIBUTE, errorMessage);
    }

    @Test
    void nodeTypeIsNotDerivedFromToscaNodesNfvVnfHealIsNotSupported() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "derived_from only supports tosca.nodes.nfv.VNF";
        runFailHealValidation(VnfdUtils.INVALID_VNFD_WITH_WRONG_DERIVED_FORM_VALUE, errorMessage);
    }

    @Test
    void nodeTypeHasNoLcmOperationConfigurationKeyHealIsNotSupportedWithError() {
        runFailHealValidation(VnfdUtils.INVALID_HEAL_VNFD_WITH_MISSING_LCM_OPERATIONS,
                              EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + LCM_OPERATIONS_MANDATORY);
    }

    @Test
    void nodeTypeHasMultipleHealCausesHealIsNotSupportedWithError() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED +
                "defaultConfiguration: default configuration for heal is required";
        runFailHealValidation(VnfdUtils.INVALID_HEAL_VNFD_MISSING_DEFAULT_CONFIG, errorMessage);
    }

    @Test
    void nodeTypeHasNoHealOperationInVnflcmInterfaceHealIsNotSupported() {
        runFailHealValidation(VnfdUtils.VALID_VNFD_FILE, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, "heal"));
    }

    @Test
    void healDefinitionIsValidHealIsSupported() {
        runSuccessHealValidation();
    }

    private void runSuccessHealValidation() {
        runSuccessOperationValidation(VnfdUtils.INVALID_HEAL_VNFD_WITH_MULTIPLE_CAUSES, LCMOperationsValidatorsEnum.HEAL);
    }

    private void runFailHealValidation(final String vnfdPath, final String errorMessage) {
        runFailOperationValidation(vnfdPath, errorMessage, LCMOperationsValidatorsEnum.HEAL);
    }
}