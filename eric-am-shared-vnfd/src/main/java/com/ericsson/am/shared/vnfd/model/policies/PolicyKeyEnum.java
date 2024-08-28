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

public enum PolicyKeyEnum {

    SCALING_ASPECT("tosca.policies.nfv.ScalingAspects"),
    VDU_INITAL_DELTA("tosca.policies.nfv.VduInitialDelta"),
    VDU_SCALING_ASPECT_DELTA("tosca.policies.nfv.VduScalingAspectDeltas"),
    VNF_PACKAGE_CHANGE_POLICY_TYPE("tosca.policies.nfv.VnfPackageChange"),
    CUSTOM_VNF_PACKAGE_CHANGE_POLICY_TYPE("ericsson.policies.nfv.VnfPackageChange"),
    VDU_INSTANTIATION_LEVEL("tosca.policies.nfv.VduInstantiationLevels"),
    INSTANTIATION_LEVEL("tosca.policies.nfv.InstantiationLevels"),
    HELM_PARAMS_MAPPING("tosca.policies.nfv.HelmParamsMapping");

    private final String vnfdKey;

    PolicyKeyEnum(String vnfdKey) {
        this.vnfdKey = vnfdKey;
    }

    public String getVnfdKey() {
        return vnfdKey;
    }
}
