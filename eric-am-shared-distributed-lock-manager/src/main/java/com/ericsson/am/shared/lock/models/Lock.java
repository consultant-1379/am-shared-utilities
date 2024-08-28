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
package com.ericsson.am.shared.lock.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Lock implements Serializable {

    @Serial
    private static final long serialVersionUID = -7391242416645895349L;

    private String holder;

    private String resourceId;

    private long acquisitionTime;

    private long expirationTime;

    private int priority;

    public Lock() {
    }

    public Lock(final String holder, final String resourceId, final long acquisitionTime, final long expirationTime, final int priority) {
        this.holder = holder;
        this.resourceId = resourceId;
        this.acquisitionTime = acquisitionTime;
        this.expirationTime = expirationTime;
        this.priority = priority;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(final String holder) {
        this.holder = holder;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    public long getAcquisitionTime() {
        return acquisitionTime;
    }

    public void setAcquisitionTime(final long acquisitionTime) {
        this.acquisitionTime = acquisitionTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(final long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Lock lock = (Lock) o;
        return acquisitionTime == lock.acquisitionTime && expirationTime == lock.expirationTime && priority == lock.priority && Objects.equals(
                holder, lock.holder) && Objects.equals(resourceId, lock.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, resourceId, acquisitionTime, expirationTime, priority);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

