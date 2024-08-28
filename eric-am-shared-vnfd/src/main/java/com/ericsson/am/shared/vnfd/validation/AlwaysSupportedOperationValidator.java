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
import org.json.JSONObject;

public class AlwaysSupportedOperationValidator implements EvnfmLCMValidator {
    private final String operationName;

    public AlwaysSupportedOperationValidator(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public OperationDetail validateOperation(JSONObject vnfd) {
        return new OperationDetail.Builder()
                .operationName(operationName)
                .supported(true)
                .build();
    }
}
