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
package com.ericsson.am.shared.vnfd.model.nestedvnfd;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VduCp {

    private String name;
    private Integer order;
    private Map<String, String> requirements;

    public VduCp() {
    }

    public VduCp(final String name, final Integer order, final Map<String, String> requirements) {
        this.name = name;
        this.order = order;
        this.requirements = requirements;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public Map<String, String> getRequirements() {
        return requirements;
    }

    public void setRequirements(final Map<String, String> requirements) {
        this.requirements = requirements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VduCp vduCp = (VduCp) o;
        return Objects.equals(order, vduCp.order) && Objects.equals(requirements, vduCp.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, requirements);
    }
}
