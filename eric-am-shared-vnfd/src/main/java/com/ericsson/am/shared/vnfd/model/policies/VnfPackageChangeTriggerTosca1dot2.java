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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangeTriggerTosca1dot2 {

    @JsonProperty("action")
    private String action;

    @JsonProperty("event")
    private String event;

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(final String event) {
        this.event = event;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VnfPackageChangeTriggerTosca1dot2 that = (VnfPackageChangeTriggerTosca1dot2) o;
        return Objects.equals(action, that.action) && Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, event);
    }
}
