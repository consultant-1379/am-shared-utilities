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
public class Policy {
    private String name;
    private String description;
    private String type;
    private List<Property> properties;
    private Targets targets;
    private List<Trigger> triggers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Targets getTargets() {
        return targets;
    }

    public void setTargets(Targets targets) {
        this.targets = targets;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(final List<Trigger> triggers) {
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
        final Policy policy = (Policy) o;
        return Objects.equals(name, policy.name) && Objects.equals(description, policy.description) && Objects.equals(type, policy.type)
                && Objects.equals(properties, policy.properties) && Objects.equals(targets, policy.targets)
                && Objects.equals(triggers, policy.triggers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, type, properties, targets, triggers);
    }
}
