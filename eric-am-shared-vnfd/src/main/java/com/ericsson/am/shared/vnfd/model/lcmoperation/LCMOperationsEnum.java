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

import java.util.Arrays;
import java.util.List;

public enum LCMOperationsEnum {
    INSTANTIATE("instantiate"),
    TERMINATE("terminate"),
    HEAL("heal"),
    CHANGE_VNFPKG("change_package"),
    CHANGE_CURRENT_PACKAGE("change_current_package"), // added for tosca 1.3 package

    SCALE("scale"),
    ROLLBACK("rollback"),

    MODIFY_INFO("modify_information"),
    SYNC("sync");

    private final String operation;

    LCMOperationsEnum(String operation) {
        this.operation = operation;
    }

    public static List<LCMOperationsEnum> getList() {
        return Arrays.asList(LCMOperationsEnum.values());
    }

    public static boolean isOperationSupported(String operationType) {
        for (LCMOperationsEnum parametersEnum : LCMOperationsEnum.values()) {
            if (String.valueOf(parametersEnum.operation).equals(operationType)) {
                return true;
            }
        }
        return false;
    }

    public String getOperation() {
        return operation;
    }
}
