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

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONObject;

import com.ericsson.am.shared.vnfd.VnfdUtility;
import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsValidatorsEnum;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

public abstract class BaseOperationValidatorTest {

    protected void runSuccessOperationValidation(final String vnfdPath, final LCMOperationsValidatorsEnum operation) {
        // given
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath));

        // when
        OperationDetail operationDetail = operation.getEvnfmLCMValidator().validateOperation(vnfdObject);

        // then
        assertThat(operationDetail).isNotNull();
        assertThat(operationDetail.getOperationName()).isEqualTo(operation.getOperation());
        assertThat(operationDetail.isSupported()).isTrue();
        assertThat(operationDetail.getErrorMessage()).isNull();
    }

    protected void runFailOperationValidation(final String vnfdPath, final String errorMessage,
                                              final LCMOperationsValidatorsEnum operation) {
        // given
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath));

        // when
        OperationDetail operationDetail = operation.getEvnfmLCMValidator().validateOperation(vnfdObject);

        // then
        assertThat(operationDetail).isNotNull();
        assertThat(operationDetail.getOperationName()).isEqualTo(operation.getOperation());
        assertThat(operationDetail.isSupported()).isFalse();
        assertThat(operationDetail.getErrorMessage()).isEqualTo(errorMessage);
    }
}
