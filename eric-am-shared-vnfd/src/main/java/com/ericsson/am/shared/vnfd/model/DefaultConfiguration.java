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

public class DefaultConfiguration {

    @NotNull(message = "heal configuration is required")
    @JsonProperty("heal")
    private HealConfiguration healConfiguration;

    public HealConfiguration getHealConfiguration() {
        return healConfiguration;
    }

    public void setHealConfiguration(HealConfiguration healConfiguration) {
        this.healConfiguration = healConfiguration;
    }
}
