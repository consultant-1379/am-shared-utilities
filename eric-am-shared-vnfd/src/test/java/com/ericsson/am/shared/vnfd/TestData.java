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
package com.ericsson.am.shared.vnfd;

import java.util.List;
import java.util.Map;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.Checksum;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Mciop;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.MciopArtifact;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainer;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerArtifact;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerDeployableUnit;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduProfile;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduVirtualBlockStorage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduVirtualBlockStorageData;

public final class TestData {

    private TestData() {
    }

    public static List<Mciop> buildExpectedMciopNode() {
        Map<String, List<String>> spiderAppRequirements = Map.of("associatedVdu", List.of("Spider_VDU"));
        MciopArtifact spiderAppArtifact = new MciopArtifact("helm_package2", "Mciop Helm package associated with this descriptor",
                                                            "tosca.artifacts.nfv.HelmChart", "Definitions/OtherTemplates/spider-app-2.208.2.tgz");
        Mciop spiderApp = new Mciop("mciop_spider_app", "tosca.nodes.nfv.Mciop", spiderAppRequirements, List.of(spiderAppArtifact));

        Map<String, List<String>> busyboxRequirements = Map.of("associatedVdu", List.of("Busybox_VDU"));
        MciopArtifact busyboxArtifact = new MciopArtifact("mciop_busybox_helm", "Mciop Helm package associated with this descriptor",
                                                          "tosca.artifacts.nfv.HelmChart",
                                                          "Definitions/OtherTemplates/busybox-simple-chart-1.1.3.tgz");
        Mciop busybox = new Mciop("mciop-busybox", "tosca.nodes.nfv.Mciop", busyboxRequirements, List.of(busyboxArtifact));

        return List.of(spiderApp, busybox);
    }

    public static List<VduOsContainerDeployableUnit> buildExpectedOsContainerDeployableUnit() {
        Map<String, List<String>> spiderAppRequirements = Map.of("container",
                                                                 List.of("Spider_Container_1", "Spider_Container_2"),
                                                                 "virtual_storage",
                                                                 List.of("Spider_Storage"));
        VduOsContainerDeployableUnit spiderApp =
                new VduOsContainerDeployableUnit("Spider App VDU", "Model of the Spider App VDU",
                                                 new VduProfile(1, 4), "Spider_VDU", "tosca.nodes.nfv.Vdu.OsContainerDeployableUnit",
                                                 spiderAppRequirements);

        Map<String, List<String>> busyboxRequirements = Map.of("container",
                                                               List.of("Busybox_Container"),
                                                               "virtual_storage",
                                                               List.of("Busybox_Storage"));
        VduOsContainerDeployableUnit busybox =
                new VduOsContainerDeployableUnit("Busybox VDU", "Model of the Busybox App VDU",
                                                 new VduProfile(1, 4), "Busybox_VDU", "tosca.nodes.nfv.Vdu.OsContainerDeployableUnit",
                                                 busyboxRequirements);

        return List.of(spiderApp, busybox);
    }

    public static List<VduOsContainer> buildExpectedOsContainer() {
        VduOsContainerArtifact spiderOsContainerArtifact = buildSpiderVduOsContainerArtifact();
        VduOsContainer spiderApp1 = new VduOsContainer();
        spiderApp1.setType("tosca.nodes.nfv.Vdu.OsContainer");
        spiderApp1.setNodeName("Spider_Container_1");
        spiderApp1.setName("Spider Container 1");
        spiderApp1.setDescription("Spider Container 1");
        spiderApp1.setRequestedCpuResources("1000");
        spiderApp1.setCpuResourceLimit("2500");
        spiderApp1.setRequestedMemoryResources("1 GiB");
        spiderApp1.setMemoryResourceLimit("10 GiB");
        spiderApp1.setArtifacts(List.of(spiderOsContainerArtifact));

        VduOsContainer spiderApp2 = new VduOsContainer();
        spiderApp2.setType("tosca.nodes.nfv.Vdu.OsContainer");
        spiderApp2.setNodeName("Spider_Container_2");
        spiderApp2.setName("Spider Container 2");
        spiderApp2.setDescription("Spider Container 2");
        spiderApp2.setRequestedCpuResources("1000");
        spiderApp2.setCpuResourceLimit("2500");
        spiderApp2.setRequestedMemoryResources("1 GiB");
        spiderApp2.setMemoryResourceLimit("10 GiB");
        spiderApp2.setArtifacts(List.of(spiderOsContainerArtifact));

        VduOsContainerArtifact busyboxOsContainerArtifact = buildBusyboxVduOsContainerArtifact();
        VduOsContainer busybox = new VduOsContainer();
        busybox.setType("tosca.nodes.nfv.Vdu.OsContainer");
        busybox.setNodeName("Busybox_Container");
        busybox.setName("Busybox Container");
        busybox.setDescription("Busybox Container");
        busybox.setRequestedCpuResources("1000");
        busybox.setCpuResourceLimit("2500");
        busybox.setRequestedMemoryResources("1 GiB");
        busybox.setMemoryResourceLimit("10 GiB");
        busybox.setArtifacts(List.of(busyboxOsContainerArtifact));

        return List.of(spiderApp1, spiderApp2, busybox);
    }

    public static List<VduVirtualBlockStorage> buildExpectedVirtualBlockStorage() {
        VduVirtualBlockStorageData vduVirtualBlockStorageData = new VduVirtualBlockStorageData("2 GiB", false);

        VduVirtualBlockStorage spider = new VduVirtualBlockStorage("Spider_Storage", "tosca.nodes.nfv.Vdu.VirtualBlockStorage",
                                                                   vduVirtualBlockStorageData, true);
        VduVirtualBlockStorage busybox = new VduVirtualBlockStorage("Busybox_Storage", "tosca.nodes.nfv.Vdu.VirtualBlockStorage",
                                                                   vduVirtualBlockStorageData, true);

        return List.of(spider, busybox);
    }

    private static VduOsContainerArtifact buildSpiderVduOsContainerArtifact() {
        VduOsContainerArtifact spiderContainerArtifact = new VduOsContainerArtifact();

        spiderContainerArtifact.setNodeName("sw_image");
        spiderContainerArtifact.setType("tosca.artifacts.nfv.SwImage");
        spiderContainerArtifact.setFile("Files/images/spider-app-2.208.2.tar");
        spiderContainerArtifact.setName("spider-app-2.208.2");
        spiderContainerArtifact.setVersion("1.0.0");
        spiderContainerArtifact.setContainerFormat("docker");
        spiderContainerArtifact.setChecksum(new Checksum("sha-512",
                                                         "C2259250EAE4E9D4CDCA5BC2829D40BB2D48A89870E37D21D9364F41BB0A85081757C"
                                                                 + "AAD988D5C7D6D2DC62AFE30A505DFE7827FAA52BDA2CCF5E34C3CDD8301"));
        spiderContainerArtifact.setSize("99 B");
        spiderContainerArtifact.setDiskFormat("raw");
        spiderContainerArtifact.setMinDisk("100 B");

        return spiderContainerArtifact;
    }

    private static VduOsContainerArtifact buildBusyboxVduOsContainerArtifact() {
        VduOsContainerArtifact busyboxArtifact = new VduOsContainerArtifact();

        busyboxArtifact.setNodeName("sw_image");
        busyboxArtifact.setType("tosca.artifacts.nfv.SwImage");
        busyboxArtifact.setFile("Files/images/busybox-simple-chart-1.1.3.tar");
        busyboxArtifact.setName("busybox-simple-chart-1.1.3");
        busyboxArtifact.setVersion("1.0.0");
        busyboxArtifact.setContainerFormat("docker");
        busyboxArtifact.setChecksum(new Checksum("sha-512",
                                                 "C2259250EAE4E9D4CDCA5BC2829D40BB2D48A89870E37D21D9364F41BB0A85081757C"
                                                         + "AAD988D5C7D6D2DC62AFE30A505DFE7827FAA52BDA2CCF5E34C3CDD8302"));
        busyboxArtifact.setSize("99 B");
        busyboxArtifact.setDiskFormat("raw");
        busyboxArtifact.setMinDisk("100 B");

        return busyboxArtifact;
    }
}
