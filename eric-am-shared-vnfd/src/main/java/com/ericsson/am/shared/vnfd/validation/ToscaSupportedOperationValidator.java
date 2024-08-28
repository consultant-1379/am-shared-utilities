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
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.am.shared.vnfd.VnfdUtility.validateYamlAndConvertToJsonObject;

public final class ToscaSupportedOperationValidator {

    private ToscaSupportedOperationValidator() {
    }

    public static List<OperationDetail> getVnfdSupportedOperations(String vnfd) {
        JSONObject vnfdObject = validateYamlAndConvertToJsonObject(vnfd);
        List<LCMOperationsValidatorsEnum> supportedOperations = LCMOperationsValidatorsEnum.getList();
        return supportedOperations.stream()
                .map(operation -> operation.getEvnfmLCMValidator().validateOperation(vnfdObject))
                .collect(Collectors.toList());
    }
}
