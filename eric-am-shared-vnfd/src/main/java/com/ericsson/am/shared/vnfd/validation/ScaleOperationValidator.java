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
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_CONTAINS_ONLY_NON_SCALABLE_VDU;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.NodeTemplateUtility;
import com.ericsson.am.shared.vnfd.NodeTypeUtility;
import com.ericsson.am.shared.vnfd.PolicyUtility;
import com.ericsson.am.shared.vnfd.TopologyTemplateUtility;
import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;

public class ScaleOperationValidator implements EvnfmLCMValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleOperationValidator.class);

    @Override
    public OperationDetail validateOperation(JSONObject vnfd) {
        VnfmLcmInterface.Type scaleOperation = VnfmLcmInterface.Type.SCALE;
        String scaleOperationName = LCMOperationsEnum.SCALE.getOperation();

        try {
            if (!NodeTypeUtility.isOperationDefinedInVnfLcmInterface(vnfd, scaleOperation)) {
                return OperationDetail.ofNotSupportedOperationWithError(
                        scaleOperationName, String.format(OPERATION_MISSING_IN_VNF_LCM_INTERFACES, scaleOperationName));
            }
            TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(vnfd, new NodeType());
            boolean isRel4NodeTemplate = NodeTemplateUtility.isRel4NodeTemplate(topologyTemplate.getNodeTemplate());
            if (isRel4NodeTemplate) {
                LOGGER.info("VNFD is rel4. Started checking if some VDUs are scalable");
                boolean isAllNonScalableVdus = TopologyTemplateUtility.isAllNonScalableVdusInVnfd(topologyTemplate);
                if (isAllNonScalableVdus) {
                    LOGGER.warn("VNFD contains only NonScalable VDUs. Scale operation is not supported");
                    return OperationDetail.ofNotSupportedOperationWithError(
                            scaleOperationName, VNFD_CONTAINS_ONLY_NON_SCALABLE_VDU);
                }
            }

            PolicyUtility.createAndValidatePolicies(vnfd);
        } catch (Exception e) {
            LOGGER.error(String.format(VALIDATION_OF_LCM_OPERATION_HAS_FAILED, scaleOperationName), e);
            return OperationDetail.ofNotSupportedOperationWithError(
                    scaleOperationName, EXCEPTION_WHEN_CHECK_IF_OPERATION_IS_SUPPORTED + e.getMessage());
        }

        return OperationDetail.ofSupportedOperation(scaleOperationName);
    }
}
