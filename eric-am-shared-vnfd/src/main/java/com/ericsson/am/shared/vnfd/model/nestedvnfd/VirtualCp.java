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
public class VirtualCp {

    private String name;

    private Map<String, String> requirements;

    public VirtualCp() {}

    public VirtualCp(final String name, final Map<String, String> requirements) {
        this.name = name;
        this.requirements = requirements;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, String> getRequirements() {
        return requirements;
    }

    public void setRequirements(final Map<String, String> requirements) {
        this.requirements = requirements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, requirements);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VirtualCp virtualCp = (VirtualCp) o;
        return Objects.equals(name, virtualCp.name) && Objects.equals(requirements, virtualCp.requirements);
    }
}
