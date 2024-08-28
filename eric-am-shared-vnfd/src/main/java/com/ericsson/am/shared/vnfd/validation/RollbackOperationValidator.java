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

import static com.ericsson.am.shared.vnfd.NodeTypeUtility.nodeTypeHasInterfaceDerivedFromToscaInterfacesNfvChangeCurrentVnfPackage;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED;
import static com.ericsson.am.shared.vnfd.utils.Constants.ROLLBACK_OPERATION_REQUIRES_INTERFACE_DERIVED_FROM_TOSCA_CCVP;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_LCM_OPERATION_HAS_FAILED;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.VnfdUtility;
import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;

public class RollbackOperationValidator implements EvnfmLCMValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackOperationValidator.class);

    @Override
    public OperationDetail validateOperation(JSONObject vnfd) {
        String rollbackOperation = LCMOperationsEnum.ROLLBACK.getOperation();

        try {
            if (nodeTypeHasInterfaceDerivedFromToscaInterfacesNfvChangeCurrentVnfPackage(vnfd)) {
                VnfdUtility.buildVnfDescriptorDetails(vnfd); // validates rollback policies and operations
                return OperationDetail.ofSupportedOperation(rollbackOperation);
            } else {
                return OperationDetail.ofNotSupportedOperationWithError(
                        rollbackOperation,
                        ROLLBACK_OPERATION_REQUIRES_INTERFACE_DERIVED_FROM_TOSCA_CCVP);
            }
        } catch (Exception e) {
            LOGGER.error(String.format(VALIDATION_OF_LCM_OPERATION_HAS_FAILED, rollbackOperation), e);
            return OperationDetail.ofNotSupportedOperationWithError(
                    rollbackOperation,
                    EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + e.getMessage());
        }
    }
}
