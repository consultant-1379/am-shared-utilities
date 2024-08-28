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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VduOsContainerDeployableUnit extends VduCompute {
    private String type;
    private Map<String, List<String>> requirements;

    public VduOsContainerDeployableUnit() {

    }

    public VduOsContainerDeployableUnit(final String name,
                                        final String description,
                                        final VduProfile vduProfile,
                                        final String vduComputeKey,
                                        final String type,
                                        final Map<String, List<String>> requirements) {
        super(name, description, vduProfile, vduComputeKey);
        this.type = type;
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
        if (!super.equals(o)) {
            return false;
        }
        final VduOsContainerDeployableUnit that = (VduOsContainerDeployableUnit) o;
        return Objects.equals(type, that.type) && Objects.equals(requirements, that.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, requirements);
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Map<String, List<String>> getRequirements() {
        return requirements;
    }

    public void setRequirements(final Map<String, List<String>> requirements) {
        this.requirements = requirements;
    }
}
