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
package com.ericsson.am.shared.vnfd.model.policies;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;

public class Policies {

    private Map<String, ScalingAspects> allScalingAspects;

    private Map<String, InitialDelta> allInitialDelta;

    private Map<String, ScalingAspectDeltas> allScalingAspectDelta;

    private Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy;

    private Map<String, VduInstantiationLevels> allVduInstantiationLevels;

    private Map<String, InstantiationLevels> allInstantiationLevels;

    private Map<String, HelmParamsMapping> allHelmParamsMappings;

    public Policies() {
    }

    private Policies(Builder builder) {
        this.allScalingAspects = builder.allScalingAspects;
        this.allInitialDelta = builder.allInitialDelta;
        this.allScalingAspectDelta = builder.allScalingAspectDelta;
        this.allVnfPackageChangePolicy = builder.allVnfPackageChangePolicy;
        this.allVduInstantiationLevels = builder.allVduInstantiationLevels;
        this.allInstantiationLevels = builder.allInstantiationLevels;
        this.allHelmParamsMappings = builder.allHelmParamsMappings;
    }

    public Map<String, VduInstantiationLevels> getAllVduInstantiationLevels() {
        return this.allVduInstantiationLevels;
    }

    public void setAllVduInstantiationLevels(Map<String, VduInstantiationLevels> allVduInstantiationLevels) {
        this.allVduInstantiationLevels = allVduInstantiationLevels;
    }

    public Map<String, InstantiationLevels> getAllInstantiationLevels() {
        return allInstantiationLevels;
    }

    public void setAllInstantiationLevels(Map<String, InstantiationLevels> allInstantiationLevels) {
        this.allInstantiationLevels = allInstantiationLevels;
    }

    public Map<String, VnfPackageChangePolicyTosca1dot2> getAllVnfPackageChangePolicy() {
        return allVnfPackageChangePolicy;
    }

    public void setAllVnfPackageChangePolicy(Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
        this.allVnfPackageChangePolicy = allVnfPackageChangePolicy;
    }

    public Map<String, ScalingAspects> getAllScalingAspects() {
        return allScalingAspects;
    }

    public void setAllScalingAspects(Map<String, ScalingAspects> allScalingAspects) {
        this.allScalingAspects = allScalingAspects;
    }

    public Map<String, InitialDelta> getAllInitialDelta() {
        return allInitialDelta;
    }

    public void setAllInitialDelta(Map<String, InitialDelta> allInitialDelta) {
        this.allInitialDelta = allInitialDelta;
    }

    public Map<String, ScalingAspectDeltas> getAllScalingAspectDelta() {
        return allScalingAspectDelta;
    }

    public void setAllScalingAspectDelta(Map<String, ScalingAspectDeltas> allScalingAspectDelta) {
        this.allScalingAspectDelta = allScalingAspectDelta;
    }

    public Map<String, HelmParamsMapping> getAllHelmParamsMappings() {
        return allHelmParamsMappings;
    }

    public void setAllHelmParamsMappings(final Map<String, HelmParamsMapping> allHelmParamsMappings) {
        this.allHelmParamsMappings = allHelmParamsMappings;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Policies policies = (Policies) o;
        return Objects.equals(allScalingAspects, policies.allScalingAspects) && Objects.equals(allInitialDelta, policies.allInitialDelta)
                && Objects.equals(allScalingAspectDelta, policies.allScalingAspectDelta)
                && Objects.equals(allVnfPackageChangePolicy, policies.allVnfPackageChangePolicy)
                && Objects.equals(allVduInstantiationLevels, policies.allVduInstantiationLevels)
                && Objects.equals(allInstantiationLevels, policies.allInstantiationLevels)
                && Objects.equals(allHelmParamsMappings, policies.allHelmParamsMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allScalingAspects,
                            allInitialDelta,
                            allScalingAspectDelta,
                            allVnfPackageChangePolicy,
                            allVduInstantiationLevels,
                            allInstantiationLevels,
                            allHelmParamsMappings);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static final class Builder {
        private Map<String, ScalingAspects> allScalingAspects;
        private Map<String, InitialDelta> allInitialDelta;
        private Map<String, ScalingAspectDeltas> allScalingAspectDelta;
        private Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy;
        private Map<String, VduInstantiationLevels> allVduInstantiationLevels;
        private Map<String, InstantiationLevels> allInstantiationLevels;
        private Map<String, HelmParamsMapping> allHelmParamsMappings;

        public Builder allScalingAspects(Map<String, ScalingAspects> allScalingAspects) {
            this.allScalingAspects = allScalingAspects;
            return this;
        }

        public Builder allInitialDelta(Map<String, InitialDelta> allInitialDelta) {
            this.allInitialDelta = allInitialDelta;
            return this;
        }

        public Builder allScalingAspectDelta(Map<String, ScalingAspectDeltas> allScalingAspectDelta) {
            this.allScalingAspectDelta = allScalingAspectDelta;
            return this;
        }

        public Builder allVnfPackageChangePolicy(Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
            this.allVnfPackageChangePolicy = allVnfPackageChangePolicy;
            return this;
        }

        public Builder allVduInstantiationLevels(Map<String, VduInstantiationLevels> allVduInstantiationLevels) {
            this.allVduInstantiationLevels = allVduInstantiationLevels;
            return this;
        }

        public Builder allInstantiationLevels(Map<String, InstantiationLevels> allInstantiationLevels) {
            this.allInstantiationLevels = allInstantiationLevels;
            return this;
        }

        public Builder allHelmParamsMappings(Map<String, HelmParamsMapping> allHelmParamsMappings) {
            this.allHelmParamsMappings = allHelmParamsMappings;
            return this;
        }

        public Policies build() {
            return new Policies(this);
        }
    }
}
