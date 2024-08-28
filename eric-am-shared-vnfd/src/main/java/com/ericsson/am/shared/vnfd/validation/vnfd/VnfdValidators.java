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

import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VnfdValidators {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfdValidators.class);

    private static final Set<VnfdValidator> VNFD_VALIDATORS;

    static {
        VNFD_VALIDATORS = Set.of(
                new MciopHelmChartNamesValidator(),
                new DeployableModuleValidator()
        );
    }

    private VnfdValidators() {
    }

    public static void validateOrThrow(JSONObject vnfd) {
        VNFD_VALIDATORS.forEach(validator -> {
            var validationResult = validator.validate(vnfd);

            if (!validationResult.isValid()) {
                var errorMessage = validationResult.getErrorMessage();
                LOGGER.info("Vnfd validation failed: {}", errorMessage);
                throw new VnfdValidationException(errorMessage);
            }
        });
    }

}
