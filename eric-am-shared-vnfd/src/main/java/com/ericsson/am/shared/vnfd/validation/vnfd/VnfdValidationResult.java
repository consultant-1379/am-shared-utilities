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
package com.ericsson.am.shared.vnfd.validation.vnfd;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class VnfdValidationResult {

    private String errorMessage;

    public VnfdValidationResult() {
    }

    public VnfdValidationResult(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isBlank(errorMessage);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
