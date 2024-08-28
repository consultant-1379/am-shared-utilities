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
import static com.ericsson.am.shared.vnfd.utils.Constants.OPERATION_MISSING_IN_VNF_LCM_INTERFACES;

import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;
import com.ericsson.am.shared.vnfd.utils.VnfdUtils;

@SuppressWarnings("squid:S2699")
class ScaleOperationValidatorTest extends BaseOperationValidatorTest {
    private static final String VNFD_VNFD_TOSCA_1_2_MULTI_B_YAML
            = "vnfd/vnfd_tosca_1_2_25_multi_b.yaml";
    private static final String VNFD_VNFD_TOSCA_1_3_MULTI_B_YAML
            = "vnfd/vnfd_tosca_1_3_multi_b.yaml";
    private static final String TOSCA_1_2_WITHOUT_SCALE_YAML
            = "vnfd/valid_vnfd.yaml";
    private static final String TOSCA_1_3_MULTI_B_WITHOUT_SCALE_YAML
            = "vnfd/vnfd_tosca_1_3_multi_b_without_scale.yaml";
    private static final String VNFD_INVALID_VNFD_WITH_MISSING_SCALING_DELTA_YAML
            = "vnfd/invalid_vnfd_with_missing_scaling_delta.yaml";
    private static final String VDU_SCALING_ASPECT_DELTA_YAML
            = "vnfd/invalid_vnfd_with_missing_vdu_scaling_aspect_delta.yaml";
    private static final String TOSCA_1_3_MULTI_B_WRONG_POLICIES_YAML
            = "vnfd/vnfd_tosca_1_3_multi_b_wrong_policies.yaml";

    @Test
    void testNodeTypeHasNoDerivedFromKeyScaleIsNotSupported() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + "derived_from not defined for node Ericsson.SGSN-MME"
                + ".1_20_CXS101289_R81E08.cxp9025898_4r81e08";
        runFailScaleValidation(VnfdUtils.INVALID_VNFD_WITHOUT_DERIVED_FORM_ATTRIBUTE, errorMessage);
    }

    @Test
    void testNodeTypeIsNotDerivedFromToscaNodesNfvVnfScaleIsNotSupported() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + "derived_from only supports tosca.nodes.nfv.VNF";
        runFailScaleValidation(VnfdUtils.INVALID_VNFD_WITH_WRONG_DERIVED_FORM_VALUE, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasValidScalingPoliciesScaleIsSupported() {
        runSuccessScaleValidation(VNFD_VNFD_TOSCA_1_2_MULTI_B_YAML);
    }

    @Test
    void testTosca1v3VnfdHasValidScalingPoliciesScaleIsSupported() {
        runSuccessScaleValidation(VNFD_VNFD_TOSCA_1_3_MULTI_B_YAML);
    }

    @Test
    void testTosca1v2VnfdHasNoScaleOperationScaleIsNotSupported() {
        runFailScaleValidation(TOSCA_1_2_WITHOUT_SCALE_YAML,
                               String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, LCMOperationsEnum.SCALE.getOperation()));
    }

    @Test
    void testTosca1v3VnfdHasNoScaleOperationScaleIsNotSupported() {
        runFailScaleValidation(TOSCA_1_3_MULTI_B_WITHOUT_SCALE_YAML,
                               String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, LCMOperationsEnum.SCALE.getOperation()));
    }

    @Test
    void testTosca1v2VnfdHasMissingScalingDeltaScaleIsNotSupportedWithException() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + "Scaling delta delta_2 is not defined for scaling aspect Payload in " +
                "VduScalingDelta Payload_ScalingAspectDeltas";
        runFailScaleValidation(VNFD_INVALID_VNFD_WITH_MISSING_SCALING_DELTA_YAML, errorMessage);
    }

    @Test
    void testTosca1v2VnfdHasMissingVfuScalingAspectScaleIsNotSupportedWithException() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + "VduScalingDelta not defined for aspect Payload_2";
        runFailScaleValidation(VDU_SCALING_ASPECT_DELTA_YAML, errorMessage);
    }

    @Test
    void testTosca1v3VnfdHasMissingScalingDeltaScaleIsNotSupportedWithException() {
        String errorMessage = EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + "VduScalingDelta not defined for aspect Aspect4";
        runFailScaleValidation(TOSCA_1_3_MULTI_B_WRONG_POLICIES_YAML, errorMessage);
    }

    private void runSuccessScaleValidation(final String vnfdPath) {
        runSuccessOperationValidation(vnfdPath, LCMOperationsValidatorsEnum.SCALE);
    }

    private void runFailScaleValidation(final String vnfdPath, final String errorMessage) {
        runFailOperationValidation(vnfdPath, errorMessage, LCMOperationsValidatorsEnum.SCALE);
    }
}