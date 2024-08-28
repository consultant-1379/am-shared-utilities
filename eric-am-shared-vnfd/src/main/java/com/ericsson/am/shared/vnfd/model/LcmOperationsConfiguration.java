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
package com.ericsson.am.shared.vnfd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class LcmOperationsConfiguration {

    @NotNull(message = "type in lcm_operations_configuration is required")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "default configuration for heal is required")
    @JsonProperty("default")
    private DefaultConfiguration defaultConfiguration;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DefaultConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public void setDefaultConfiguration(DefaultConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }
}
