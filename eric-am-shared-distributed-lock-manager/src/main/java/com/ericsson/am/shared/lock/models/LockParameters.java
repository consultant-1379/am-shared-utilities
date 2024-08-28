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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LockParameters implements Serializable {

    @Serial
    private static final long serialVersionUID = -5933231078521146136L;

    private static final String LOCK_PRIORITY_KEY = "%s:%s:locks-next-holder-priority";
    private static final String GLOBAL_LOCKS_REDIS_KEY = "%s:%s:locks:%s";
    private static final String REPLICA_LOCK_LIST_REDIS_KEY = "%s:%s:locks";

    private String serviceId;

    private String replicaName;

    private String componentName;

    private int lockAcquisitionRetryTimeoutMs;

    private int lockAcquisitionRetryIntervalMs;

    public LockParameters(final String serviceId, final String replicaName, final String componentName, final int lockAcquisitionRetryTimeoutMs,
            final int lockAcquisitionRetryIntervalMs) {
        this.serviceId = serviceId;
        this.replicaName = replicaName;
        this.componentName = componentName;
        this.lockAcquisitionRetryTimeoutMs = lockAcquisitionRetryTimeoutMs;
        this.lockAcquisitionRetryIntervalMs = lockAcquisitionRetryIntervalMs;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public String getReplicaName() {
        return replicaName;
    }

    public void setReplicaName(final String replicaName) {
        this.replicaName = replicaName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public int getLockAcquisitionRetryTimeoutMs() {
        return lockAcquisitionRetryTimeoutMs;
    }

    public void setLockAcquisitionRetryTimeoutMs(final int lockAcquisitionRetryTimeoutMS) {
        this.lockAcquisitionRetryTimeoutMs = lockAcquisitionRetryTimeoutMs;
    }

    public int getLockAcquisitionRetryIntervalMs() {
        return lockAcquisitionRetryIntervalMs;
    }

    public void setLockAcquisitionRetryIntervalMs(final int lockAcquisitionRetryIntervalMs) {
        this.lockAcquisitionRetryIntervalMs = lockAcquisitionRetryIntervalMs;
    }

    public String getLockPriorityKey() {
        return LOCK_PRIORITY_KEY.formatted(serviceId, componentName);
    }

    public String getGlobalLocksRedisKey() {
        return GLOBAL_LOCKS_REDIS_KEY.formatted(serviceId, componentName, "%s");
    }

    public String getReplicaLockListRedisKey() {
        return REPLICA_LOCK_LIST_REDIS_KEY.formatted(serviceId, componentName);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE, false, true);
    }
}