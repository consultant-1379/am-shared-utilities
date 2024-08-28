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
package com.ericsson.am.shared.vnfd.model.lcmoperation;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LCMOperationsParametersEnum {

    INSTANTIATE(LCMOperationsEnum.INSTANTIATE, true),
    HEAL(LCMOperationsEnum.HEAL, true),
    CHANGE_PACKAGE(LCMOperationsEnum.CHANGE_VNFPKG, true),
    CHANGE_CURRENT_PACKAGE(LCMOperationsEnum.CHANGE_CURRENT_PACKAGE, true),
    ROLLBACK(LCMOperationsEnum.ROLLBACK, true),
    SCALE(LCMOperationsEnum.SCALE, false),
    TERMINATE(LCMOperationsEnum.TERMINATE, false),
    SYNC(LCMOperationsEnum.SYNC, false),
    MODIFY_INFO(LCMOperationsEnum.MODIFY_INFO, false);

    private final LCMOperationsEnum operation;
    private final boolean isAdditionalParametersRequired;

    LCMOperationsParametersEnum(LCMOperationsEnum operation, boolean isAdditionalParametersRequired) {
        this.operation = operation;
        this.isAdditionalParametersRequired = isAdditionalParametersRequired;
    }

    public static boolean isAdditionalParametersRequired(String operationType) {
        return Arrays.stream(LCMOperationsParametersEnum.values())
                .filter(lcmOperation -> lcmOperation.getOperation().equals(operationType))
                .findAny().orElseThrow(() -> new UnsupportedOperationException(String.format("%s operation not supported", operationType)))
                .isAdditionalParametersRequired;
    }

    public String getOperation() {
        return operation.getOperation();
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(operation);
    }
}
