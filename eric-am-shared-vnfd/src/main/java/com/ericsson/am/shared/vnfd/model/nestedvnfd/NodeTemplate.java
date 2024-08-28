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

import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;

public class NodeTemplate {

    private List<VnfmLcmInterface> interfaces;
    private String type;
    private Map<String, InterfaceTypeImpl> customInterface;
    private List<VduCompute> vduCompute;
    private List<Mciop> mciop;
    private List<VduOsContainerDeployableUnit> osContainerDeployableUnit;
    private List<VduOsContainer> vduOsContainer;
    private List<VduVirtualBlockStorage> vduVirtualBlockStorages;
    private List<VduCp> vduCps;
    private List<VirtualCp> virtualCps;
    private Map<String, DeployableModule> deploymentModules;

    public Map<String, InterfaceTypeImpl> getCustomInterface() {
        return customInterface;
    }

    public void setCustomInterface(Map<String, InterfaceTypeImpl> customInterface) {
        this.customInterface = customInterface;
    }

    public List<VnfmLcmInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<VnfmLcmInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<VduCompute> getVduCompute() {
        return vduCompute;
    }

    public void setVduCompute(List<VduCompute> vduCompute) {
        this.vduCompute = vduCompute;
    }

    public List<Mciop> getMciop() {
        return mciop;
    }

    public void setMciop(final List<Mciop> mciop) {
        this.mciop = mciop;
    }

    public List<VduOsContainerDeployableUnit> getOsContainerDeployableUnit() {
        return osContainerDeployableUnit;
    }

    public void setOsContainerDeployableUnit(final List<VduOsContainerDeployableUnit> osContainerDeployableUnit) {
        this.osContainerDeployableUnit = osContainerDeployableUnit;
    }

    public List<VduOsContainer> getVduOsContainer() {
        return vduOsContainer;
    }

    public void setVduOsContainer(final List<VduOsContainer> vduOsContainer) {
        this.vduOsContainer = vduOsContainer;
    }

    public List<VduVirtualBlockStorage> getVduVirtualBlockStorages() {
        return vduVirtualBlockStorages;
    }

    public void setVduVirtualBlockStorages(final List<VduVirtualBlockStorage> vduVirtualBlockStorages) {
        this.vduVirtualBlockStorages = vduVirtualBlockStorages;
    }

    public List<VduCp> getVduCps() {
        return vduCps;
    }

    public void setVduCps(final List<VduCp> vduCps) {
        this.vduCps = vduCps;
    }

    public List<VirtualCp> getVirtualCps() {
        return virtualCps;
    }

    public void setVirtualCps(final List<VirtualCp> virtualCps) {
        this.virtualCps = virtualCps;
    }

    public Map<String, DeployableModule> getDeploymentModules() {
        return deploymentModules;
    }

    public void setDeploymentModules(final Map<String, DeployableModule> deploymentModules) {
        this.deploymentModules = deploymentModules;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeTemplate that = (NodeTemplate) o;
        return Objects.equals(interfaces, that.interfaces) && Objects.equals(type, that.type) && Objects.equals(customInterface, that.customInterface)
                && Objects.equals(vduCompute, that.vduCompute) && Objects.equals(mciop, that.mciop)
                && Objects.equals(osContainerDeployableUnit, that.osContainerDeployableUnit) && Objects.equals(vduOsContainer, that.vduOsContainer)
                && Objects.equals(vduVirtualBlockStorages, that.vduVirtualBlockStorages) && Objects.equals(vduCps, that.vduCps)
                && Objects.equals(virtualCps, that.virtualCps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaces,
                            type,
                            customInterface,
                            vduCompute,
                            mciop,
                            osContainerDeployableUnit,
                            vduOsContainer,
                            vduVirtualBlockStorages,
                            vduCps,
                            virtualCps);
    }
}
