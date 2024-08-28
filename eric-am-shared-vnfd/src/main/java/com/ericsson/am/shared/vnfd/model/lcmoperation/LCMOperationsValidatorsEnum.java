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

import com.ericsson.am.shared.vnfd.validation.AlwaysSupportedOperationValidator;
import com.ericsson.am.shared.vnfd.validation.DefaultOperationsValidator;
import com.ericsson.am.shared.vnfd.validation.EvnfmLCMValidator;
import com.ericsson.am.shared.vnfd.validation.HealOperationValidator;
import com.ericsson.am.shared.vnfd.validation.RollbackOperationValidator;
import com.ericsson.am.shared.vnfd.validation.ScaleOperationValidator;

import java.util.Arrays;
import java.util.List;

public enum LCMOperationsValidatorsEnum {
    INSTANTIATE(LCMOperationsEnum.INSTANTIATE, new DefaultOperationsValidator(LCMOperationsEnum.INSTANTIATE.getOperation())),
    TERMINATE(LCMOperationsEnum.TERMINATE, new DefaultOperationsValidator(LCMOperationsEnum.TERMINATE.getOperation())),
    CHANGE_VNFPKG(LCMOperationsEnum.CHANGE_VNFPKG, new DefaultOperationsValidator(LCMOperationsEnum.CHANGE_VNFPKG.getOperation())),
    CHANGE_CURRENT_PACKAGE(LCMOperationsEnum.CHANGE_CURRENT_PACKAGE,
                           new DefaultOperationsValidator(LCMOperationsEnum.CHANGE_CURRENT_PACKAGE.getOperation())),

    HEAL(LCMOperationsEnum.HEAL, new HealOperationValidator()),
    SCALE(LCMOperationsEnum.SCALE, new ScaleOperationValidator()),
    ROLLBACK(LCMOperationsEnum.ROLLBACK, new RollbackOperationValidator()),

    // MODIFY_INFO and SYNC operations are not defined on Vnflcm interface operations section in VNFD,
    // but they are provided by default for the VNFD package.
    MODIFY_INFO(LCMOperationsEnum.MODIFY_INFO, new AlwaysSupportedOperationValidator(LCMOperationsEnum.MODIFY_INFO.getOperation())),
    SYNC(LCMOperationsEnum.SYNC, new AlwaysSupportedOperationValidator(LCMOperationsEnum.SYNC.getOperation()));

    private final LCMOperationsEnum operation;
    private final EvnfmLCMValidator evnfmLCMValidator;

    LCMOperationsValidatorsEnum(LCMOperationsEnum operation, EvnfmLCMValidator evnfmLCMValidator) {
        this.operation = operation;
        this.evnfmLCMValidator = evnfmLCMValidator;
    }

    public static List<LCMOperationsValidatorsEnum> getList() {
        return Arrays.asList(LCMOperationsValidatorsEnum.values());
    }

    public EvnfmLCMValidator getEvnfmLCMValidator() {
        return evnfmLCMValidator;
    }

    public String getOperation() {
        return operation.getOperation();
    }
}
