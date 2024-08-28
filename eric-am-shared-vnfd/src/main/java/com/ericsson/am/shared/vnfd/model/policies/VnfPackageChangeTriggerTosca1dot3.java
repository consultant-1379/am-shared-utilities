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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangeTriggerTosca1dot3 {
    @JsonProperty("event")
    private String event;

    @JsonProperty("action")
    private List<CallOperationTrigger> action;

    public List<CallOperationTrigger> getAction() {
        return action;
    }

    public void setAction(final List<CallOperationTrigger> action) {
        this.action = action;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(final String event) {
        this.event = event;
    }
}
