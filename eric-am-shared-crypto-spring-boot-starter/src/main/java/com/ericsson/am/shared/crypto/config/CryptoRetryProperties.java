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
package com.ericsson.am.shared.crypto.config;

public class CryptoRetryProperties {

    private long initialBackoff;
    private long maxBackoff;
    private int maxAttempts;
    private double multiplier;
    private int connectTimeout;
    private int readTimeout;

    public CryptoRetryProperties() {
        this.initialBackoff = 2000;
        this.maxBackoff = 16000;
        this.maxAttempts = 3;
        this.multiplier = 2.0;
        this.connectTimeout = 3000;
        this.readTimeout = 3000;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getInitialBackoff() {
        return initialBackoff;
    }

    public long getMaxBackoff() {
        return maxBackoff;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void setInitialBackoff(long initialBackoff) {
        this.initialBackoff = initialBackoff;
    }

    public void setMaxBackoff(long maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
