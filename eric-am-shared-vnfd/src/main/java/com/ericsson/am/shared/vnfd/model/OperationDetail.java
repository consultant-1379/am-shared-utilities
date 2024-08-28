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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = OperationDetail.Builder.class)
public final class OperationDetail {
    private String operationName;
    private boolean supported = true;
    private String errorMessage;

    private OperationDetail(Builder builder) {
        setOperationName(builder.operationName);
        setSupported(builder.supported);
        setErrorMessage(builder.errorMessage);
    }

    public static OperationDetail ofSupportedOperation(String operationName) {
        return new Builder()
                .supported(true)
                .operationName(operationName)
                .build();
    }

    public static OperationDetail ofNotSupportedOperation(String operationName) {
        return new Builder()
                .supported(false)
                .operationName(operationName)
                .build();
    }

    public static OperationDetail ofNotSupportedOperationWithError(String operationName, String errorMessage) {
        return new Builder()
                .supported(false)
                .operationName(operationName)
                .errorMessage(errorMessage)
                .build();
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OperationDetail that = (OperationDetail) o;
        return supported == that.supported
                && Objects.equals(operationName, that.operationName)
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationName, supported, errorMessage);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String operationName;
        private boolean supported;
        private String errorMessage;

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder supported(boolean supported) {
            this.supported = supported;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public OperationDetail build() {
            return new OperationDetail(this);
        }
    }

}
