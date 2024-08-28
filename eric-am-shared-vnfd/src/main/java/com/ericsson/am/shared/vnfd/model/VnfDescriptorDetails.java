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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;

public class VnfDescriptorDetails {

    private String vnfDescriptorId;
    private String vnfDescriptorVersion;
    private String vnfProvider;
    private String vnfProductName;
    private String vnfSoftwareVersion;
    private String vnfmInfo;
    private String descriptorModel;
    private List<HelmChart> helmCharts;
    private List<ImageDetails> imagesDetails;
    private Map<String, DataType> allDataTypes;
    private Map<String, InterfaceType> allInterfaceTypes;
    private Map<String, Flavour> flavours;
    private Flavour defaultFlavour;

    public Map<String, DataType> getAllDataTypes() {
        return allDataTypes;
    }

    public void setAllDataTypes(Map<String, DataType> allDataTypes) {
        this.allDataTypes = allDataTypes;
    }

    public Map<String, InterfaceType> getAllInterfaceTypes() {
        return allInterfaceTypes;
    }

    public void setAllInterfaceTypes(Map<String, InterfaceType> allInterfaceTypes) {
        this.allInterfaceTypes = allInterfaceTypes;
    }

    public String getVnfDescriptorId() {
        return vnfDescriptorId;
    }

    public void setVnfDescriptorId(String vnfDescriptorId) {
        this.vnfDescriptorId = vnfDescriptorId;
    }

    public String getDescriptorModel() {
        return descriptorModel;
    }

    public void setDescriptorModel(String descriptorModel) {
        this.descriptorModel = descriptorModel;
    }

    public String getVnfDescriptorVersion() {
        return vnfDescriptorVersion;
    }

    public void setVnfDescriptorVersion(String vnfDescriptorVersion) {
        this.vnfDescriptorVersion = vnfDescriptorVersion;
    }

    public String getVnfProvider() {
        return vnfProvider;
    }

    public void setVnfProvider(String vnfProvider) {
        this.vnfProvider = vnfProvider;
    }

    public String getVnfProductName() {
        return vnfProductName;
    }

    public void setVnfProductName(String vnfProductName) {
        this.vnfProductName = vnfProductName;
    }

    public String getVnfSoftwareVersion() {
        return vnfSoftwareVersion;
    }

    public void setVnfSoftwareVersion(String vnfSoftwareVersion) {
        this.vnfSoftwareVersion = vnfSoftwareVersion;
    }

    public String getVnfmInfo() {
        return vnfmInfo;
    }

    public void setVnfmInfo(String vnfmInfo) {
        this.vnfmInfo = vnfmInfo;
    }

    public List<HelmChart> getHelmCharts() {
        return helmCharts;
    }

    public void setHelmCharts(final List<HelmChart> helmCharts) {
        this.helmCharts = helmCharts;
    }

    public List<ImageDetails> getImagesDetails() {
        return imagesDetails;
    }

    public void setImagesDetails(List<ImageDetails> imagesDetails) {
        this.imagesDetails = imagesDetails;
    }

    public Map<String, Flavour> getFlavours() {
        return flavours;
    }

    public void setFlavours(Map<String, Flavour> flavours) {
        this.flavours = flavours;
    }

    public Flavour getDefaultFlavour() {
        return defaultFlavour;
    }

    public void setDefaultFlavour(Flavour defaultFlavour) {
        this.defaultFlavour = defaultFlavour;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VnfDescriptorDetails that = (VnfDescriptorDetails) o;
        return Objects.equals(vnfDescriptorId, that.vnfDescriptorId) && Objects.equals(vnfDescriptorVersion, that.vnfDescriptorVersion)
                && Objects.equals(vnfProvider, that.vnfProvider) && Objects.equals(vnfProductName, that.vnfProductName)
                && Objects.equals(vnfSoftwareVersion, that.vnfSoftwareVersion) && Objects.equals(vnfmInfo, that.vnfmInfo)
                && Objects.equals(descriptorModel, that.descriptorModel) && Objects.equals(helmCharts, that.helmCharts)
                && Objects.equals(imagesDetails, that.imagesDetails) && Objects.equals(allDataTypes, that.allDataTypes)
                && Objects.equals(allInterfaceTypes, that.allInterfaceTypes) && Objects.equals(flavours, that.flavours)
                && Objects.equals(defaultFlavour, that.defaultFlavour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vnfDescriptorId,
                            vnfDescriptorVersion,
                            vnfProvider,
                            vnfProductName,
                            vnfSoftwareVersion,
                            vnfmInfo,
                            descriptorModel,
                            helmCharts,
                            imagesDetails,
                            allDataTypes,
                            allInterfaceTypes,
                            flavours,
                            defaultFlavour);
    }
}
