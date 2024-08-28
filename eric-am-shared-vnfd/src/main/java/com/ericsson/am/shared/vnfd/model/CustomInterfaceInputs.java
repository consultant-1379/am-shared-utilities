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

import java.util.Map;
import java.util.Objects;

public class CustomInterfaceInputs implements Input {

    private DataTypeImpl additionalParams;

    private Map<String, Object> staticAdditionalParams;

    public CustomInterfaceInputs() {
    }

    public CustomInterfaceInputs(DataTypeImpl additionalParams) {
        this.additionalParams = additionalParams;
    }

    public DataTypeImpl getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(DataTypeImpl additionalParams) {
        this.additionalParams = additionalParams;
    }

    public Map<String, Object> getStaticAdditionalParams() {
        return staticAdditionalParams;
    }

    public void setStaticAdditionalParams(final Map<String, Object> staticAdditionalParams) {
        this.staticAdditionalParams = staticAdditionalParams;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CustomInterfaceInputs that = (CustomInterfaceInputs) o;
        return Objects.equals(additionalParams, that.additionalParams) && Objects.equals(staticAdditionalParams, that.staticAdditionalParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(additionalParams, staticAdditionalParams);
    }
}
