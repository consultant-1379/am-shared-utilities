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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.ericsson.am.shared.vnfd.NodeTypeUtility.buildNodeType;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.MISSING_REQUIRED_SCALE_MAP_KEY_FORMAT;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.WARN_MESSAGE_ABOUT_NOT_EXISTENT_OS_CONTAINER_DEPLOYABLE_UNIT;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.getScalingMap;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.validateRel4ScalingMap;
import static com.ericsson.am.shared.vnfd.ScalingMapUtility.validateScalingMap;
import static com.ericsson.am.shared.vnfd.utils.Constants.MCIOP_NAME;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_REL4_POLICY_MODEL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.ScaleMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class ScalingMapTest {

    private static final String VALID_SCALE_MAPPING_FILE = "vnfd/valid_scaling_map.yaml";
    private static final String VALID_REL4_SCALE_MAPPING_FILE = "vnfd/valid_rel4_scaling_map.yaml";
    private static final String VALID_REL4_STORAGE_SCALE_MAPPING_FILE = "vnfd/valid_rel4_storage_scaling_map.yaml";
    private static final String EMPTY_SCALE_MAPPING_FILE = "vnfd/empty_vnfd_file.yaml";
    private static final String INVALID_SCALE_MAPPING_FILE_PATH = "vnfd/invalid_path/valid_scaling_map.yaml";
    private static final String INVALID_SCALE_MAPPING_FILE_MISSING_KEYS = "vnfd/invalid_scaling_map_with_missing_key.yaml";
    private static final String INVALID_SCALE_MAPPING_YAML = "vnfd/invalid_scaling_map.yaml";
    private static final String INVALID_SCALE_MAPPING_WITHOUT_MCIOP_NODE = "vnfd/invalid_scaling_map_without_mciop_node.yaml";
    private static final String INVALID_REL4_SCALE_MAPPING_MISSING_MCIOP = "vnfd/invalid_rel4_scaling_map_missing_mciop.yaml";

    private static final String UNABLE_TO_PARSE_MAPPING_FILE_FORMAT = "Scaling mapping file provided in package is invalid";

    private static final String REL4_VNFD = "vnfd/valid_vnfd_rel4_with_all_duplicated_mciop_helm_charts.yaml";
    private static final String VALID_REL4_SCALING_MAPPING_FILE = "vnfd/valid_rel4_scaling_map_os_container_deployable_unit.yaml";
    private static final String VALID_REL4_SCALING_MAPPING_FILE_WITHOUT_STORAGES = "vnfd"
            + "/valid_rel4_scaling_map_os_container_deployable_unit_without_storages.yaml";
    private static final String VALID_REL4_SCALING_MAPPING_FILE_WITHOUT_CONTAINERS = "vnfd"
            + "/valid_rel4_scaling_map_os_container_deployable_unit_without_containers.yaml";
    private static final String INVALID_REL4_SCALING_MAPPING_FILE_WITH_ROOT_ELEMENT_NOT_DEPLOYABLE_UNIT = "vnfd"
            + "/invalid_rel4_scaling_map_with_root_elements_not_deployable_units.yaml";
    private static final String INVALID_REL4_SCALING_MAPPING_FILE_WITH_NOT_EXISTENT_STORAGES = "vnfd"
            + "/invalid_rel4_scaling_map_os_container_deployable_unit_with_not_existing_storages.yaml";
    private static final String INVALID_REL4_SCALING_MAPPING_FILE_WITH_NOT_EXISTENT_CONTAINERS = "vnfd"
            + "/invalid_rel4_scaling_map_os_container_deployable_unit_with_not_existing_containers.yaml";
    private static final String INVALID_REL4_SCALING_MAPPING_FILE_WITH_NOT_EXISTENT_VDU_AND_STORAGE = "vnfd"
            + "/invalid_rel4_scaling_map_os_container_deployable_unit_with_not_existing_vdu_and_storage.yaml";
    private static final String INVALID_REL4_SCALING_MAPPING_FILE_WITH_EXISTENT_BUT_NOT_ASSOCIATED_STORAGE =
            "vnfd/invalid_rel4_scaling_map_os_container_deployable_unit_with_not_associated_storage.yaml";

    @Test
    void testGetScalingMapFile() {
        Path scaleMapPath = TestUtils.getResource(VALID_SCALE_MAPPING_FILE).toAbsolutePath();
        Map<String, ScaleMapping> scaleMap = getScalingMap(scaleMapPath);
        assertEquals(2, scaleMap.size());
    }

    @Test
    void testGetScalingMapFromString() throws IOException {
        int expectedScalingMappingSize = 2;

        String scalingMappingContent = TestUtils.readDataFromFile(VALID_SCALE_MAPPING_FILE);
        Map<String, ScaleMapping> scalingMapping = getScalingMap(scalingMappingContent);

        assertEquals(expectedScalingMappingSize, scalingMapping.size());
    }

    @Test
    void testGetScalingMapFileWithInvalidPath() {
        File file = new File(INVALID_SCALE_MAPPING_FILE_PATH);
        Path scaleMapPath = Paths.get(file.getAbsolutePath());
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> getScalingMap(scaleMapPath));
        assertThat(exception.getMessage())
                .startsWith("Scaling mapping path provided in VNFD is invalid:")
                .endsWith("/vnfd/invalid_path/valid_scaling_map.yaml");
    }

    @Test
    void testInvalidScalingMapRequiredKeysMissing() {
        Path scaleMapPath = TestUtils.getResource(INVALID_SCALE_MAPPING_FILE_MISSING_KEYS).toAbsolutePath();
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                ScalingMapUtility.getScalingMap(scaleMapPath));
        assertEquals("Scaling-Parameter-Name is a required field in scale mapping file", exception.getMessage());
    }

    @Test
    void testGetScalingMapWithInvalidYaml() {
        Path invalidScalingMapPath = TestUtils.getResource(INVALID_SCALE_MAPPING_YAML).toAbsolutePath();

        assertThatThrownBy(() -> getScalingMap(invalidScalingMapPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNABLE_TO_PARSE_MAPPING_FILE_FORMAT);
    }

    @Test
    void testGetScalingMapWithoutRequiredMciopNode() {
        Path invalidScalingMapPath = TestUtils.getResource(INVALID_SCALE_MAPPING_WITHOUT_MCIOP_NODE).toAbsolutePath();

        assertThatThrownBy(() -> getScalingMap(invalidScalingMapPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format(MISSING_REQUIRED_SCALE_MAP_KEY_FORMAT, MCIOP_NAME));
    }

    @Test
    void testValidateRel4ScalingMapFile() {
        NodeTemplate nodeTemplate = getNodeTemplate(SAMPLE_VNFD_WITH_REL4_POLICY_MODEL);
        Path scaleMapPath = TestUtils.getResource(VALID_REL4_SCALE_MAPPING_FILE).toAbsolutePath();
        Map<String, ScaleMapping> scaleMap = getScalingMap(scaleMapPath);
        validateScalingMap(scaleMap, nodeTemplate);
    }

    @Test
    void testValidateRel4ScalingMapFileMissingMciop() {
        NodeTemplate nodeTemplate = getNodeTemplate(SAMPLE_VNFD_WITH_REL4_POLICY_MODEL);
        Path scaleMapPath = TestUtils.getResource(INVALID_REL4_SCALE_MAPPING_MISSING_MCIOP).toAbsolutePath();
        Map<String, ScaleMapping> scaleMap = getScalingMap(scaleMapPath);
        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                ScalingMapUtility.validateScalingMap(scaleMap, nodeTemplate));
        assertEquals("Mciop-Name invalid-mciop specified in scaling mapping does not match any Mciop node in VNFD",
                     exception.getMessage());
    }

    @Test
    void testValidateRel4WithStorageScalingMapFile() {
        NodeTemplate nodeTemplate = getNodeTemplate(SAMPLE_VNFD_WITH_REL4_POLICY_MODEL);
        Path scaleMapPath = TestUtils.getResource(VALID_REL4_STORAGE_SCALE_MAPPING_FILE).toAbsolutePath();
        Map<String, ScaleMapping> scaleMap = getScalingMap(scaleMapPath);
        validateScalingMap(scaleMap, nodeTemplate);
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_REL4_SCALING_MAPPING_FILE, EMPTY_SCALE_MAPPING_FILE, VALID_REL4_SCALING_MAPPING_FILE_WITHOUT_STORAGES,
        VALID_REL4_SCALING_MAPPING_FILE_WITHOUT_CONTAINERS})
    void testValidateRel4ScalingMappingFileShouldPassWithoutWarnings(String scalingMappingFilePath) {
        List<ILoggingEvent> loggingEvents = runListAppender();

        runScalingMappingFileValidation(scalingMappingFilePath);

        assertThat(loggingEvents).isEmpty();
    }

    @Test
    void testValidateRel4ScalingMappingFileShouldLogWarnAboutNotDeployableUnitRootElement() {
        List<ILoggingEvent> loggingEvents = runListAppender();

        runScalingMappingFileValidation(INVALID_REL4_SCALING_MAPPING_FILE_WITH_ROOT_ELEMENT_NOT_DEPLOYABLE_UNIT);

        assertThat(loggingEvents).hasSize(1);
        ILoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_OS_CONTAINER_DEPLOYABLE_UNIT +
                                                                                       GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                                               "Spider_VDU_not_deployable_unit"));
    }

    @Test
    void testValidateRel4ScalingMappingFileShouldLogWarnAboutNotExistentStorages() {
        List<ILoggingEvent> loggingEvents = runListAppender();

        runScalingMappingFileValidation(INVALID_REL4_SCALING_MAPPING_FILE_WITH_NOT_EXISTENT_STORAGES);

        assertThat(loggingEvents).hasSize(2);
        ILoggingEvent firstLoggingEvent = loggingEvents.get(0);
        assertThat(firstLoggingEvent.getLevel()).isEqualTo(Level.WARN);
        String firstFormattedWarnMessage = String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                         "SpiderStorageNotExistent1",
                                                         "storage", "Spider_VDU",
                                                         "Spider_VDU");
        assertThat(firstLoggingEvent.getFormattedMessage()).isEqualTo(firstFormattedWarnMessage);
        ILoggingEvent secondLoggingEvent = loggingEvents.get(1);
        assertThat(secondLoggingEvent.getLevel()).isEqualTo(Level.WARN);
        String secondFormattedWarnMessage = String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                          "SpiderStorageNotExistent2",
                                                          "storage", "Spider_VDU2",
                                                          "Spider_VDU2");
        assertThat(secondLoggingEvent.getFormattedMessage()).isEqualTo(secondFormattedWarnMessage);
    }

    @Test
    void testValidateRel4ScalingMappingFileWithNotExistentVduAndStorageShouldLogWarnOnlyAboutNotExistentVdu() {
        List<ILoggingEvent> loggingEvents = runListAppender();

        runScalingMappingFileValidation(INVALID_REL4_SCALING_MAPPING_FILE_WITH_NOT_EXISTENT_VDU_AND_STORAGE);

        assertThat(loggingEvents).hasSize(1);
        ILoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_OS_CONTAINER_DEPLOYABLE_UNIT +
                                                                                       GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                                               "Spider_VDU3"));
    }

    @Test
    void testValidateRel4ScalingMappingFileWithExistentButNotAssociatedStorageShouldLogWarnAboutInvalidStorage() {
        List<ILoggingEvent> loggingEvents = runListAppender();

        runScalingMappingFileValidation(INVALID_REL4_SCALING_MAPPING_FILE_WITH_EXISTENT_BUT_NOT_ASSOCIATED_STORAGE);

        assertThat(loggingEvents).hasSize(1);
        ILoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        String expectedWarnMessage = String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                         "SpiderStorage",
                                                         "storage", "Spider_VDU2",
                                                         "Spider_VDU2");
        assertThat(expectedWarnMessage).isEqualTo(loggingEvent.getFormattedMessage());
    }

    @Test
    void testValidateRel4ScalingMappingFileShouldLogWarnAboutNotExistentContainers() {
        List<ILoggingEvent> loggingEvents = runListAppender();

        runScalingMappingFileValidation(INVALID_REL4_SCALING_MAPPING_FILE_WITH_NOT_EXISTENT_CONTAINERS);

        assertThat(loggingEvents).hasSize(2);
        ILoggingEvent firstLoggingEvent = loggingEvents.get(0);
        assertThat(firstLoggingEvent.getLevel()).isEqualTo(Level.WARN);
        String firstFormattedWarnMessage = String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                         "Spider_ContainerNotExistent1",
                                                         "container", "Spider_VDU",
                                                         "Spider_VDU");
        assertThat(firstLoggingEvent.getFormattedMessage()).isEqualTo(firstFormattedWarnMessage);
        ILoggingEvent secondLoggingEvent = loggingEvents.get(1);
        assertThat(secondLoggingEvent.getLevel()).isEqualTo(Level.WARN);
        String secondFormattedWarnMessage = String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                                          "Spider_ContainerNotExistent2",
                                                          "container", "Spider_VDU2",
                                                          "Spider_VDU2");
        assertThat(secondLoggingEvent.getFormattedMessage()).isEqualTo(secondFormattedWarnMessage);
    }

    @Test
    void testMapArtifactsToVduNames() {
        Path scaleMapPath = TestUtils.getResource(VALID_SCALE_MAPPING_FILE).toAbsolutePath();
        Map<String, ScaleMapping> scaleMap = getScalingMap(scaleMapPath);

        String artifactName = "helm_package";
        List<String> expectedVduNames = List.of("Search-Engine-DB", "Search-Engine");
        Map<String, List<String>> artifactsToVduNames = ScalingMapUtility.mapArtifactsToVduNames(scaleMap);
        List<String> actualVduNames = artifactsToVduNames.get(artifactName);

        assertEquals(1, artifactsToVduNames.size());
        assertEquals(2, actualVduNames.size());
        assertEquals(expectedVduNames.size(), actualVduNames.size());
        assertTrue(expectedVduNames.containsAll(actualVduNames));
        assertTrue(actualVduNames.containsAll(expectedVduNames));
    }

    private void runScalingMappingFileValidation(String scalingMappingPath) {
        NodeTemplate nodeTemplate = getNodeTemplate(REL4_VNFD);
        Path scaleMapPath = TestUtils.getResource(scalingMappingPath).toAbsolutePath();
        Map<String, ScaleMapping> scalingMap = getScalingMap(scaleMapPath);
        validateRel4ScalingMap(scalingMap, nodeTemplate);
    }

    private List<ILoggingEvent> runListAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(ScalingMapUtility.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        return listAppender.list;
    }

    private NodeTemplate getNodeTemplate(String vnfdPath) {
        Path pathToNode = TestUtils.getResource(vnfdPath).toAbsolutePath();
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        NodeType nodeTypeObject = getNodeTypeObject(vnfd);
        return NodeTemplateUtility.createNodeTemplate(nodeTypeObject, vnfd);
    }

    private NodeType getNodeTypeObject(final JSONObject vnfd) {
        Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfd);
        Map<String, InterfaceType> allInterfaceType = InterfaceTypeUtility.getInterfaceTypeFromVnfd(vnfd, allDataType);
        return buildNodeType(vnfd, allDataType, allInterfaceType);
    }
}
