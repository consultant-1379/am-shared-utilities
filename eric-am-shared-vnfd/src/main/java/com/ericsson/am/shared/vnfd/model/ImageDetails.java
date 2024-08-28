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
package com.ericsson.am.shared.vnfd.model;

import java.util.Objects;

public class ImageDetails implements ToscaNodeInput {
    private String path;
    private String resourceId;

    public ImageDetails() {
    }

    public ImageDetails(final String path, final String resourceID) {
        this.path = path;
        this.resourceId = resourceID;
    }

    public ImageDetails(final String path) {
        this.path = path;
        this.resourceId = "software_image";
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImageDetails that = (ImageDetails) o;
        return Objects.equals(path, that.path) && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, resourceId);
    }
}
