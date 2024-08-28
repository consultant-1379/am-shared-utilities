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

public class HelmPackage {

    private String id;
    private Integer priority;
    private Map<String, Object> helmValues;

    public HelmPackage() {
    }

    public HelmPackage(String helmPackageIdentifier, Integer helmPackagePriority) {
        this.id = helmPackageIdentifier;
        this.priority = helmPackagePriority;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    public Map<String, Object> getHelmValues() {
        return helmValues;
    }

    public void setHelmValues(final Map<String, Object> helmValues) {
        this.helmValues = helmValues;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HelmPackage that = (HelmPackage) o;
        return Objects.equals(id, that.id) && Objects.equals(priority, that.priority) && Objects.equals(helmValues, that.helmValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, priority, helmValues);
    }
}
