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
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import org.json.JSONObject;

import java.util.List;

public interface EvnfmLCMValidator {


    OperationDetail validateOperation(JSONObject vnfd);

    static boolean isLcmInterfaceDefined(String operationName, List<VnfmLcmInterface> vnfmLcmInterfaces) {
        return vnfmLcmInterfaces
                .stream()
                .anyMatch(vnfmLcmInterface -> vnfmLcmInterface
                        .getType()
                        .getLabel()
                        .equals(operationName));
    }
}
