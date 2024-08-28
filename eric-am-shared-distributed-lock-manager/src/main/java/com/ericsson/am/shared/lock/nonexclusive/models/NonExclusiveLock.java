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
package com.ericsson.am.shared.lock.nonexclusive.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class NonExclusiveLock implements Serializable {
    @Serial
    private static final long serialVersionUID = -7391242416645895349L;

    private String holder;

    private String replicaId;

    private Set<String> sharingGroups = new HashSet<>();

    private String resourceId;

    private long acquisitionTime;

    private long expirationTime;

    private int priority;

    public NonExclusiveLock() {
    }

    public NonExclusiveLock(final String holder,
                            final String replicaId,
                            final Set<String> sharingGroups,
                            final String resourceId,
                            final long acquisitionTime,
                            final long expirationTime,
                            final int priority) {
        this.holder = holder;
        this.replicaId = replicaId;
        this.sharingGroups = sharingGroups;
        this.resourceId = resourceId;
        this.acquisitionTime = acquisitionTime;
        this.expirationTime = expirationTime;
        this.priority = priority;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final NonExclusiveLock that = (NonExclusiveLock) object;
        return acquisitionTime == that.acquisitionTime && expirationTime == that.expirationTime && priority == that.priority && Objects.equals(
                holder,
                that.holder) && Objects.equals(replicaId, that.replicaId) && Objects.equals(sharingGroups, that.sharingGroups)
                && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, replicaId, sharingGroups, resourceId, acquisitionTime, expirationTime, priority);
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(final String holder) {
        this.holder = holder;
    }

    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(final String replicaId) {
        this.replicaId = replicaId;
    }

    public Set<String> getSharingGroups() {
        return sharingGroups;
    }

    public void setSharingGroups(final Set<String> sharingGroups) {
        this.sharingGroups = sharingGroups;
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
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
