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

public class Mciop {
    private String name;
    private String type;
    private Map<String, List<String>> requirements;
    private List<MciopArtifact> artifacts;

    public Mciop() {

    }

    public Mciop(final String name,
                 final String type,
                 final Map<String, List<String>> requirements,
                 final List<MciopArtifact> artifacts) {
        this.name = name;
        this.type = type;
        this.requirements = requirements;
        this.artifacts = artifacts;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Mciop mciop = (Mciop) o;
        return Objects.equals(name, mciop.name) && Objects.equals(type, mciop.type) && Objects.equals(requirements, mciop.requirements)
                && Objects.equals(artifacts, mciop.artifacts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, requirements, artifacts);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public List<MciopArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(final List<MciopArtifact> artifacts) {
        this.artifacts = artifacts;
    }
}
