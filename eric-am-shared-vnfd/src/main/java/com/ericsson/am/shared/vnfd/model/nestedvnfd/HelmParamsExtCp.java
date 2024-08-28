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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmParamsExtCp {

    private String loadBalancerIp;
    private String addressPoolName;
    private String render;
    private List<String> interfaceNames;

    public HelmParamsExtCp() {
    }

    public HelmParamsExtCp(final String loadBalancerIp,
                           final String addressPoolName,
                           final String render,
                           final List<String> interfaceNames) {
        this.loadBalancerIp = loadBalancerIp;
        this.addressPoolName = addressPoolName;
        this.render = render;
        this.interfaceNames = interfaceNames;
    }

    public String getLoadBalancerIp() {
        return loadBalancerIp;
    }

    public void setLoadBalancerIp(final String loadBalancerIp) {
        this.loadBalancerIp = loadBalancerIp;
    }

    public String getAddressPoolName() {
        return addressPoolName;
    }

    public void setAddressPoolName(final String addressPoolName) {
        this.addressPoolName = addressPoolName;
    }

    public String isRender() {
        return render;
    }

    public void setRender(final String render) {
        this.render = render;
    }

    public List<String> getInterfaceNames() {
        return interfaceNames;
    }

    public void setInterfaceNames(final List<String> interfaceNames) {
        this.interfaceNames = interfaceNames;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HelmParamsExtCp that = (HelmParamsExtCp) o;
        return Objects.equals(getLoadBalancerIp(), that.getLoadBalancerIp()) && Objects.equals(getAddressPoolName(), that.getAddressPoolName())
                && Objects.equals(render, that.render) && Objects.equals(getInterfaceNames(), that.getInterfaceNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLoadBalancerIp(), getAddressPoolName(), render, getInterfaceNames());
    }
}
