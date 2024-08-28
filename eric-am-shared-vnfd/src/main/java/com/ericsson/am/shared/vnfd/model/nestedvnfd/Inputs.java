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

public class Inputs {

    private List<HelmPackage> helmPackages;

    private String additionalParamsDataType;

    private Map<String, DataTypePropertiesDetails> additionalParams;

    public List<HelmPackage> getHelmPackages() {
        return helmPackages;
    }

    public void setHelmPackages(List<HelmPackage> helmPackages) {
        this.helmPackages = helmPackages;
    }

    public Map<String, DataTypePropertiesDetails> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, DataTypePropertiesDetails> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public String getAdditionalParamsDataType() {
        return additionalParamsDataType;
    }

    public void setAdditionalParamsDataType(final String additionalParamsDataType) {
        this.additionalParamsDataType = additionalParamsDataType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Inputs inputs = (Inputs) o;
        return Objects.equals(helmPackages, inputs.helmPackages) && Objects.equals(additionalParamsDataType, inputs.additionalParamsDataType)
                && Objects.equals(additionalParams, inputs.additionalParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(helmPackages, additionalParamsDataType, additionalParams);
    }
}
