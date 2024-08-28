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
package com.ericsson.am.shared.vnfd.model.policies;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangePolicyTosca1dot3 extends VnfPackageChangePolicyCommon {

    @JsonProperty("triggers")
    private Map<String, VnfPackageChangeTriggerTosca1dot3> triggers;

    public Map<String, VnfPackageChangeTriggerTosca1dot3> getTriggers() {
        return triggers;
    }

    public void setTriggers(final Map<String, VnfPackageChangeTriggerTosca1dot3> triggers) {
        this.triggers = triggers;
    }
}
