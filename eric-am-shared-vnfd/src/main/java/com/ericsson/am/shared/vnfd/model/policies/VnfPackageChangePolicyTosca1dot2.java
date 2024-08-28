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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangePolicyTosca1dot2 extends VnfPackageChangePolicyCommon {

    @JsonProperty("triggers")
    private List<Map<String, VnfPackageChangeTriggerTosca1dot2>> triggers;

    public List<Map<String, VnfPackageChangeTriggerTosca1dot2>> getTriggers() {
        return triggers;
    }

    public void setTriggers(final List<Map<String, VnfPackageChangeTriggerTosca1dot2>> triggers) {
        this.triggers = triggers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VnfPackageChangePolicyTosca1dot2 that = (VnfPackageChangePolicyTosca1dot2) o;
        return Objects.equals(triggers, that.triggers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(triggers);
    }
}
