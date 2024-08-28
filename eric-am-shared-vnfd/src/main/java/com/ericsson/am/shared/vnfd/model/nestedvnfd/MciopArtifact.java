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

import java.util.Objects;

public class MciopArtifact {
    private String name;
    private String description;
    private String type;
    private String file;

    public MciopArtifact() {

    }

    public MciopArtifact(final String name, final String description, final String type, final String file) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.file = file;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MciopArtifact that = (MciopArtifact) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(type, that.type)
                && Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, type, file);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }
}
