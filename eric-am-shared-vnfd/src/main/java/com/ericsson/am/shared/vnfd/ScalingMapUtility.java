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

import static com.ericsson.am.shared.vnfd.NestedMapUtility.castToNestedMap;
import static com.ericsson.am.shared.vnfd.NestedMapUtility.replaceValuesAfterKey;
import static com.ericsson.am.shared.vnfd.NodeTemplateUtility.isRel4NodeTemplate;
import static com.ericsson.am.shared.vnfd.utils.Constants.CONTAINER;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEFAULT_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.MCIOP_NAME;
import static com.ericsson.am.shared.vnfd.utils.Constants.SCALING_PARAMETER_NAME;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_STORAGE;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.ericsson.am.shared.vnfd.model.ScaleMapping;
import com.ericsson.am.shared.vnfd.model.ScaleMappingContainerDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Mciop;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCompute;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduOsContainerDeployableUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ScalingMapUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalingMapUtility.class);

    public static final String MISSING_REQUIRED_SCALE_MAP_KEY_FORMAT = "%s is a required field in scale mapping file";
    public static final String SCALING_MAPPING_PARSING_IS_FAILED_FORMAT = "Scaling mapping parsing is failed: %s";
    private static final String MAPPING_PATH_PROVIDED_IN_VNFD_IS_INVALID_FORMAT = "Scaling mapping path provided in VNFD is invalid: %s";
    private static final String MCIOP_NAME_DOES_NOT_MATCH_VNFD = "Mciop-Name %s specified in scaling mapping does not match any Mciop node in VNFD";
    private static final String UNABLE_TO_PARSE_MAPPING_FILE_FORMAT = "Scaling mapping file provided in package is invalid: %s";

    public static final String GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY = "Dynamic capacity requirement input is not supported";
    public static final String WARN_MESSAGE_ABOUT_NOT_EXISTENT_OS_CONTAINER_DEPLOYABLE_UNIT = "Root element of scaling mapping file %s doesn't "
            + "associate with any of OsContainerDeployableUnit in VNFD.";
    public static final String WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS = "%s %s in the %s doesn't associate with %s OsContainerDeployableUnit "
            + "in VNFD.";

    private static final List<String> SCALING_MAPPING_FIELDS = List.of("Scaling-Parameter-Name", "Mciop-Name", "Auto-Scaling-MinReplicas-Name",
            "Auto-Scaling-MaxReplicas-Name", "Auto-Scaling-Enabled", "Storage");

    private ScalingMapUtility() {
    }

    public static Map<String, ScaleMapping> getScalingMap(final Path scalingMapPath) {
        JSONObject scaleMappingJson = getMappingFileAsJson(scalingMapPath);
        return parseScalingMappingFromJson(scaleMappingJson);
    }

    public static Map<String, ScaleMapping> getScalingMap(final String scalingMap) {
        JSONObject scaleMappingJson = VnfdUtility.validateYamlAndConvertToJsonObject(scalingMap);
        return parseScalingMappingFromJson(scaleMappingJson);
    }

    public static Map<String, List<String>> mapArtifactsToVduNames(Map<String, ScaleMapping> scalingMapping) {
        return scalingMapping.entrySet().stream()
                .filter(entry -> Optional.ofNullable(entry.getValue())
                        .map(ScaleMapping::getMciopName)
                        .isPresent())
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getValue().getMciopName(), entry.getKey()))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private static Map<String, ScaleMapping> parseScalingMappingFromJson(JSONObject scaleMappingJson) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, ScaleMapping> map = new HashMap<>();
        for (String key : scaleMappingJson.keySet()) {
            JSONObject scaleMappingJsonNode = scaleMappingJson.getJSONObject(key);
            validateScalingMapRequiredKeys(scaleMappingJsonNode);
            try {
                ScaleMapping scaleMapping = mapper.readValue(scaleMappingJsonNode.toString(), ScaleMapping.class);
                populateScalingMappingFileWithContainers(scaleMapping, scaleMappingJsonNode);
                map.put(key, scaleMapping);
            } catch (JsonProcessingException e) {
                throw buildIllegalArgumentException(SCALING_MAPPING_PARSING_IS_FAILED_FORMAT, e, e.getMessage());
            }
        }
        return map;
    }

    private static void populateScalingMappingFileWithContainers(ScaleMapping scaleMapping, JSONObject scaleMappingJsonNode) {
        final Map<String, ScaleMappingContainerDetails> containers = scaleMappingJsonNode.keySet()
                .stream()
                .filter(key -> !SCALING_MAPPING_FIELDS.contains(key))
                .collect(Collectors.toMap(Function.identity(), scaleMappingJsonNode::getJSONObject))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, containerJson -> buildScaleMappingContainerDetails(containerJson.getValue())));
        scaleMapping.setContainers(containers);
    }

    private static ScaleMappingContainerDetails buildScaleMappingContainerDetails(JSONObject containerJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(containerJson.toString(), ScaleMappingContainerDetails.class);
        } catch (JsonProcessingException e) {
            throw buildIllegalArgumentException(SCALING_MAPPING_PARSING_IS_FAILED_FORMAT, e, e.getMessage());
        }
    }

    private static JSONObject getMappingFileAsJson(final Path scalingMapPath) {
        final Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Object config;
        try (InputStream file = Files.newInputStream(scalingMapPath)) {
            config = yaml.load(file);
        } catch (final NoSuchFileException nsfe) {
            throw buildIllegalArgumentException(MAPPING_PATH_PROVIDED_IN_VNFD_IS_INVALID_FORMAT, nsfe, nsfe.getMessage());
        } catch (final Exception ex) {
            throw buildIllegalArgumentException(UNABLE_TO_PARSE_MAPPING_FILE_FORMAT, ex, ex.getMessage());
        }
        if (Objects.isNull(config)) {
            return new JSONObject();
        }
        if (!(config instanceof Map)) {
            throw buildIllegalArgumentException(UNABLE_TO_PARSE_MAPPING_FILE_FORMAT, "Is not a map");
        }
        Map<String, Object> configMap = castToNestedMap(config);
        final Map<String, Object> map = replaceValuesAfterKey(configMap, DEFAULT_KEY, null, JSONObject.NULL);
        return new JSONObject(map);
    }

    public static void validateScalingMap(final Map<String, ScaleMapping> scalingMap, NodeTemplate nodeTemplate) {
        if (isRel4NodeTemplate(nodeTemplate)) {
            validateRel4ScalingMap(scalingMap, nodeTemplate);
        }
    }

    public static void validateRel4ScalingMap(final Map<String, ScaleMapping> scalingMap, NodeTemplate nodeTemplate) {
        validateScalingMapMciopNamesMatchWithVnfd(scalingMap, nodeTemplate);

        List<VduOsContainerDeployableUnit> osContainerDeployableUnits = nodeTemplate.getOsContainerDeployableUnit();
        List<String> osContainerDeployableUnitNames = getVduComputeNamesFromVnfd(osContainerDeployableUnits);

        validateScalingMapRootElementsAreDeployableUnit(scalingMap, osContainerDeployableUnitNames);
        validateScalingMapSectionsAreAssociatedWithVnfd(scalingMap, osContainerDeployableUnits);
    }

    private static List<String> getVduComputeNamesFromVnfd(List<? extends VduCompute> vduComputes) {
        return vduComputes.stream()
                .map(VduCompute::getVduComputeKey).collect(Collectors.toList());
    }

    private static void validateScalingMapMciopNamesMatchWithVnfd(Map<String, ScaleMapping> scalingMap, NodeTemplate nodeTemplate) {
        List<String> mciopNames = nodeTemplate.getMciop().stream().map(Mciop::getName).collect(Collectors.toList());
        for (ScaleMapping scaleMapping : scalingMap.values()) {
            if (!mciopNames.contains(scaleMapping.getMciopName())) {
                throw buildIllegalArgumentException(MCIOP_NAME_DOES_NOT_MATCH_VNFD, scaleMapping.getMciopName());
            }
        }
    }

    private static void validateScalingMapRootElementsAreDeployableUnit(Map<String, ScaleMapping> scalingMap,
                                                                        List<String> osContainerDeployableUnitNames) {
        for (String rootElement : scalingMap.keySet()) {
            if (!osContainerDeployableUnitNames.contains(rootElement)) {
                String formattedMessage =
                        String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_OS_CONTAINER_DEPLOYABLE_UNIT + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                                rootElement);
                LOGGER.warn(formattedMessage);
            }
        }
    }

    private static void validateScalingMapSectionsAreAssociatedWithVnfd(Map<String, ScaleMapping> scalingMap,
                                                                        List<VduOsContainerDeployableUnit> osContainerDeployableUnits) {
        for (Map.Entry<String, ScaleMapping> scaleMappingEntry : scalingMap.entrySet()) {
            String rootElement = scaleMappingEntry.getKey();
            ScaleMapping scaleMapping = scaleMappingEntry.getValue();

            Set<String> scaleMappingStorages = scaleMapping.getStorages().keySet();
            Set<String> scaleMappingContainers = scaleMapping.getContainers().keySet();

            osContainerDeployableUnits.stream()
                    .filter(vduOsContainerDeployableUnit -> vduOsContainerDeployableUnit.getVduComputeKey().equals(rootElement))
                    .findFirst()
                    .ifPresent(associatedDeployableUnit -> {
                        String vduComputeKey = associatedDeployableUnit.getVduComputeKey();
                        Map<String, List<String>> requirements = associatedDeployableUnit.getRequirements();
                        List<String> associatedStorages = requirements.getOrDefault(VIRTUAL_STORAGE, Collections.emptyList());
                        List<String> associatedContainers = requirements.getOrDefault(CONTAINER, Collections.emptyList());

                        validateDeployableUnitsContainsElements(associatedStorages, scaleMappingStorages, rootElement, vduComputeKey,
                                "storage");
                        validateDeployableUnitsContainsElements(associatedContainers, scaleMappingContainers, rootElement, vduComputeKey,
                                "container");
                    });
        }
    }

    private static void validateDeployableUnitsContainsElements(List<String> associatedRequirements, Set<String> scaleMappingElements,
                                                                String rootElement, String deployableUnitName, String elementType) {
        for (String scaleMappingElement : scaleMappingElements) {
            if (!associatedRequirements.contains(scaleMappingElement)) {
                String formattedWarnMessage = String.format(WARN_MESSAGE_ABOUT_NOT_EXISTENT_REQUIREMENTS
                                + GENERIC_WARN_MESSAGE_ABOUT_DYNAMIC_CAPACITY,
                        scaleMappingElement,
                        elementType, rootElement,
                        deployableUnitName);
                LOGGER.warn(formattedWarnMessage);
            }
        }
    }

    private static void validateScalingMapRequiredKeys(JSONObject scaleMap) {
        if (!scaleMap.has(SCALING_PARAMETER_NAME)) {
            throw buildIllegalArgumentException(MISSING_REQUIRED_SCALE_MAP_KEY_FORMAT, SCALING_PARAMETER_NAME);
        }
        if (!scaleMap.has(MCIOP_NAME)) {
            throw buildIllegalArgumentException(MISSING_REQUIRED_SCALE_MAP_KEY_FORMAT, MCIOP_NAME);
        }
    }

    private static IllegalArgumentException buildIllegalArgumentException(String messageFormat, Object... args) {
        return buildIllegalArgumentException(messageFormat, null, args);
    }

    private static IllegalArgumentException buildIllegalArgumentException(String messageFormat, Throwable cause, Object... args) {
        var errorMessage = String.format(messageFormat, args);
        return cause != null ? new IllegalArgumentException(errorMessage, cause) : new IllegalArgumentException(errorMessage);
    }
}
