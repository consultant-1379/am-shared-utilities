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
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_LCM_OPERATION_HAS_FAILED;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.NodeTypeUtility;
import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;

public class HealOperationValidator implements EvnfmLCMValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealOperationValidator.class);

    @Override
    public OperationDetail validateOperation(JSONObject vnfd) {
        VnfmLcmInterface.Type healOperation = VnfmLcmInterface.Type.HEAL;
        String healOperationName = LCMOperationsEnum.HEAL.getOperation();

        try {
            if (!NodeTypeUtility.isOperationDefinedInVnfLcmInterface(vnfd, healOperation)) {
                return OperationDetail.ofNotSupportedOperationWithError(
                        healOperationName,
                        String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, healOperationName));
            }

            NodeTypeUtility.validateHealConfiguration(vnfd);
        } catch (Exception e) {
            LOGGER.error(String.format(VALIDATION_OF_LCM_OPERATION_HAS_FAILED, healOperationName), e);
            return OperationDetail.ofNotSupportedOperationWithError(
                    healOperationName,
                    EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + e.getMessage());
        }

        return OperationDetail.ofSupportedOperation(healOperationName);
    }
}
