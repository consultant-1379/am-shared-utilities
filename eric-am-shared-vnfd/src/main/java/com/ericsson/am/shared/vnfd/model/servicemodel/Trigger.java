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
package com.ericsson.am.shared.vnfd.model.servicemodel;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trigger {
    private String name;
    private Object description;
    private String event;
    private List<Action> action;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(final Object description) {
        this.description = description;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(final String event) {
        this.event = event;
    }

    public List<Action> getAction() {
        return action;
    }

    public void setAction(final List<Action> action) {
        this.action = action;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Trigger trigger = (Trigger) o;
        return Objects.equals(name, trigger.name) && Objects.equals(description, trigger.description)
                && Objects.equals(event, trigger.event) && Objects.equals(action, trigger.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, event, action);
    }
}
