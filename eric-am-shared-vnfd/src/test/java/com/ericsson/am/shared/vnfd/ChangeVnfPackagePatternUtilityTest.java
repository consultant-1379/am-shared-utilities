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

import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.ROLLBACK_PATTERN;
import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.UPGRADE_PATTERN;
import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.getGlobalStaticParams;
import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.getPattern;
import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.getRollbackPatternAtFailureForHelmChart;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.assertj.core.groups.Tuple;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.utils.TestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.Logger;

class ChangeVnfPackagePatternUtilityTest {

    @Test
    @SuppressWarnings("unchecked")
    void testRollbackPatternAtFailureNotPresentValidation() {
        Logger logger = (Logger) LoggerFactory.getLogger(ChangeVnfPackagePatternUtility.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_rollback_at_failure_pattern.yaml"));
        VnfdUtility.buildVnfDescriptorDetails(parsed);
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("Rollback at failure patterns are not present in current package", Level.INFO));
        logger.detachAndStopAllAppenders();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetRollbackPatternAtFailureForHelmChartWithGlobalInputPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));
        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern =
                getRollbackPatternAtFailureForHelmChart("helm_package2", parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                        "36ff67a9-0de4-48f9-97a3-4b0661670933");
        assertThat(helmChartCommandPattern).hasSize(3);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("rollback");
    }

    @Test
    void testGetRollbackPatternAtFailureForHelmChartWithOperationInputPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_override_global_pattern.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern =
                getRollbackPatternAtFailureForHelmChart("helm_package2", parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                                         "36ff67a9-0de4-48f9-97a3-4b0661670933");
        assertThat(helmChartCommandPattern).hasSize(1);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
    }

    @Test
    void testGetRollbackPatternAtFailureForHelmChartWithNoRollbackPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_no_pattern.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern =
                getRollbackPatternAtFailureForHelmChart("helm_package2", parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                                         "36ff67a9-0de4-48f9-97a3-4b0661670933");
        assertThat(helmChartCommandPattern).isEmpty();
    }

    @Test
    void testGetRollbackPatternForHelmChartWithGlobalInputPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                    "36ff67a9-0de4-48f9-97a3-4b0661670934", "1.0.11s", "1.1.11s", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(3);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("rollback");
    }

    @Test
    void testGetRollbackPatternForHelmChartWithCompute() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_with_compute.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-569d-xyz3-5g15f7h499",
                                    "multi-chart-477c-aab3-2b04e6a383", "1.0.11s", "1.1.11s", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(5);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(4).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(4).getRight()).isEqualTo("install");
    }

    @Test
    void testGetRollbackPatternForHelmChartWithOperationInputPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_override_global_pattern.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                    "36ff67a9-0de4-48f9-97a3-4b0661670933", "1.0.11s", "1.1.11s", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(3);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("rollback");
    }

    @Test
    void testGetUpgradePatternForHelmChartWithOperationInputPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_different_order.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a363",
                "multi-chart-etsi-rel4-b-455379754e37", "1.0.11s", "1.1.11s", UPGRADE_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(4);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("upgrade");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("install");
    }

    @Test
    void testGetUpgradePatternForHelmChartWithInterfaceInputPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_different_order_with_interface_input.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a364",
                "multi-chart-etsi-rel4-b-455379754e33", "1.7.1+3", "1.7.1+4", UPGRADE_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(4);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("upgrade");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("install");
    }

    @Test
    void testGetUpgradePatternForHelmChartWithInterfaceInputPatternWhenSourceSoftwareVersionIsExactVersion() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_software_version.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a364",
                "multi-chart-etsi-rel4-b-455379754e33", "1.7.1+3", "1.7.1+4", UPGRADE_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(4);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("upgrade");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("install");
    }

    @Test
    void testGetRollbackPatternForHelmChartWithInterfaceInputPatternWhenDestinationSoftwareVersionIsRegex() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_software_version.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a364",
                "multi-chart-etsi-rel4-b-455379754e33", "1.9.2+3", "1.6.1+4", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(4);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("install");


    }

    @Test
    void testGetRollbackPatternForHelmChartWithInterfaceInputPatternWhenSourceSoftwareVersionIsExactVersion() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_software_version.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a364",
                "multi-chart-etsi-rel4-b-455379754e33", "1.7.1+3", "1.7.1+4", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(3);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("install");
    }

    @Test
    void testGetUpgradePatternForHelmChartWithInterfaceInputPatternWhenSourceSoftwareVersionIsRegex() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_software_version.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a364",
                "multi-chart-etsi-rel4-b-455379754e33", "1.7.3+4", "1.7.1+4", UPGRADE_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(4);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("upgrade");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("install");
    }

    @Test
    void testGetUpgradePatternForHelmChartPatternWhenSourceSoftwareVersionIsRegex() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_all_operations_with_software_version.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-477c-aab3-2b04e6a364",
                "multi-chart-etsi-rel4-b-455379754e33", "1.7.2+4", "1.7.1+6", UPGRADE_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(3);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("upgrade");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("install");
    }

    @Test
    void testGetRollbackPatternForHelmChartWithNoRollbackPattern() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_no_pattern.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                    "36ff67a9-0de4-48f9-97a3-4b0661670934", "1.0.11s", "1.1.11s", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).isEmpty();
    }

    @Test
    void testGetRollbackPatternForHelmChartWithAllRollbackCommandTypes() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_using_all_patterns.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                    "36ff67a9-0de4-48f9-97a3-4b0661670933", "1.0.11s", "1.1.11s",
                ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(6);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("upgrade");
        assertThat(helmChartCommandPattern.get(4).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(4).getRight()).isEqualTo("install");
        assertThat(helmChartCommandPattern.get(5).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(5).getRight()).isEqualTo("rollback");
    }

    @Test
    void testGetRollbackFailurePatternForHelmChartWithAllRollbackCommandTypes() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_using_all_patterns.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);

        List<MutablePair<String, String>> helmPackage1 =
                getRollbackPatternAtFailureForHelmChart("helm_package1", parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                                         "36ff67a9-0de4-48f9-97a3-4b0661670933");

        assertThat(helmPackage1).hasSize(2);
        assertThat(helmPackage1.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmPackage1.get(0).getRight())
                .isEqualTo("delete_pvc[app=trigger, app.kubernetes.io/instance=test, app.kubernetes.io/name=eric-eo-evnfm-mb]");
        assertThat(helmPackage1.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage1.get(1).getRight()).isEqualTo("install");

        List<MutablePair<String, String>> helmPackage2 =
                getRollbackPatternAtFailureForHelmChart("helm_package2", parsed, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                                         "36ff67a9-0de4-48f9-97a3-4b0661670933");
        assertThat(helmPackage2).hasSize(4);
        assertThat(helmPackage2.get(0).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(0).getRight()).isEqualTo("delete");
        assertThat(helmPackage2.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(1).getRight()).isEqualTo("delete_pvc");
        assertThat(helmPackage2.get(2).getLeft()).isEqualTo("helm_package1");
        assertThat(helmPackage2.get(2).getRight()).isEqualTo("upgrade");
        assertThat(helmPackage2.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(3).getRight()).isEqualTo("install");
    }

    @Test
    void testGetStaticGlobalVarFromOperationsAndInterface() {
        JSONObject parsedYaml = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));
        // call test method
        Map<String, Object> globalStaticVariables = getGlobalStaticParams(parsedYaml, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                       "36ff67a9-0de4-48f9-97a3-4b0661670934");

        // Assertions
        assertThat(globalStaticVariables)
                .hasSize(4)
                .containsEntry("scalarStringParam", "optionValue")
                .containsEntry("listParam", List.of("listVal4", "listVal5", "listVal6"))
                .containsEntry("spring", Map.of("profile", "dev", "logging", "enabled"))
                .containsEntry("listParamRollBack", List.of("listVal4", "listVal5", "listVal6"));
    }

    @Test
    void testGetStaticGlobalVarNoOperation() {
        JSONObject parsedYaml = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsedYaml);

        // call test method
        Map<String, Object> globalStaticVariables = getGlobalStaticParams(parsedYaml, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                       "36ff67a9-0de4-48f9-97a3-4b0661670933");

        // Assertions
        assertThat(globalStaticVariables)
                .hasSize(3)
                .containsEntry("scalarStringParam", "interfaceValue")
                .containsEntry("listParam", List.of("listVal1", "listVal2", "listVal3"))
                .containsEntry("spring", Map.of("logging", "disabled", "profile", "dev"));
    }

    @Test
    void testGetStaticGlobalVarZeroParams() {
        JSONObject parsedYaml = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_multi_heal.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsedYaml);

        // call test method
        Map<String, Object> globalStaticVariables = getGlobalStaticParams(parsedYaml, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                       "36ff67a9-0de4-48f9-97a3-4b0661670934");

        // Assertions
        assertThat(globalStaticVariables).isEmpty();
    }

    @Test
    void testGetStaticGlobalVarNoRollbackPatterns() {
        JSONObject parsedYaml = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsedYaml);

        // call test method
        Map<String, Object> globalStaticVariables = getGlobalStaticParams(parsedYaml, "2ce9484e-85e5-49b7-ac97-445379754e37",
                                       "36ff67a9-0de4-48f9-97a3-4b0661670933");

        // Assertions
        assertThat(globalStaticVariables).hasSize(3);

        assertThat(globalStaticVariables.get("rollback_pattern")).isNull();
        assertThat(globalStaticVariables.get("rollback_at_failure_pattern")).isNull();
    }

    @Test
    void testGetRollbackPatternForHelmChartWithWildcardedOperationPolicy() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_wildcard_destination_id_for_rollback_patterns.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern = getPattern(parsed, "multi-chart-569d-xyz3-5g15f7h499",
                                    "any_destination_id", "1.0.11s", "1.1.11s", ROLLBACK_PATTERN);
        assertThat(helmChartCommandPattern).hasSize(5);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("delete");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmChartCommandPattern.get(3).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(3).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(4).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(4).getRight()).isEqualTo("install");
    }

    @Test
    void testGetRollbackPatternAtFailureForHelmChartWithWildcardedOperationPolicy() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_wildcard_destination_id_for_rollback_patterns.yaml"));

        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmPackage1 =
                getRollbackPatternAtFailureForHelmChart("helm_package1", parsed, "multi-chart-569d-xyz3-5g15f7h499",
                                                         "any_destination_id");

        assertThat(helmPackage1).hasSize(2);
        assertThat(helmPackage1.get(0).getLeft()).isEqualTo("helm_package1");
        assertThat(helmPackage1.get(0).getRight())
                .isEqualTo("delete_pvc[app=trigger, app.kubernetes.io/instance=test]");
        assertThat(helmPackage1.get(1).getLeft()).isEqualTo("helm_package1");
        assertThat(helmPackage1.get(1).getRight()).isEqualTo("install");

        List<MutablePair<String, String>> helmPackage2 =
                getRollbackPatternAtFailureForHelmChart("helm_package2", parsed, "multi-chart-569d-xyz3-5g15f7h499",
                                                         "any_destination_id");
        assertThat(helmPackage2).hasSize(5);
        assertThat(helmPackage2.get(0).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmPackage2.get(1).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(1).getRight()).isEqualTo("delete");
        assertThat(helmPackage2.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(2).getRight()).isEqualTo("delete_pvc");
        assertThat(helmPackage2.get(3).getLeft()).isEqualTo("helm_package1");
        assertThat(helmPackage2.get(3).getRight()).isEqualTo("upgrade");
        assertThat(helmPackage2.get(4).getLeft()).isEqualTo("helm_package2");
        assertThat(helmPackage2.get(4).getRight()).isEqualTo("install");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetRollbackPatternAtFailureForHelmChartWithGlobalInputPatternAndDifferentNameForCCVPInterface() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_and_different_name_for_ccvp_interface.yaml"));
        VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<MutablePair<String, String>> helmChartCommandPattern =
                getRollbackPatternAtFailureForHelmChart("helm_package2", parsed, "2ce9484e-85e5-49b7-ac97-445379754856",
                                                         "36ff67a9-0de4-48f9-97a3-4b0661670r90");
        assertThat(helmChartCommandPattern).hasSize(3);
        assertThat(helmChartCommandPattern.get(0).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(0).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(1).getLeft()).isEqualTo("helm_package1");
        assertThat(helmChartCommandPattern.get(1).getRight()).isEqualTo("rollback");
        assertThat(helmChartCommandPattern.get(2).getLeft()).isEqualTo("helm_package2");
        assertThat(helmChartCommandPattern.get(2).getRight()).isEqualTo("rollback");
    }
}