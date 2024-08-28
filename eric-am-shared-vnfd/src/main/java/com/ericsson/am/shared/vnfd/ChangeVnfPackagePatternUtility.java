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

import com.ericsson.am.shared.vnfd.model.CustomInterfaceInputs;
import com.ericsson.am.shared.vnfd.model.CustomOperation;
import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import org.apache.commons.lang3.tuple.MutablePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.ericsson.am.shared.vnfd.VnfPackageChangePatternCommand.isSupportedCommand;
import static com.ericsson.am.shared.vnfd.utils.Constants.CRD_PACKAGE_PREFIX;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELMCHART_IN_PATTERN_NOT_FOUND_IN_ARTIFACTS;
import static com.ericsson.am.shared.vnfd.utils.Constants.MISSING_FAILURE_PATTERN;
import static com.ericsson.am.shared.vnfd.utils.Constants.PARSING_ISSUE_WITH_ROLLBACK_FAILURE_PATTERN;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNSUPPORTED_COMMAND_IN_PATTERN;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_PATTERN_FOR_POLICY_OPERATION;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_BASE;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_DELETE_USAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_ENDING;
import static com.ericsson.am.shared.vnfd.utils.Constants.WRONG_ROLLBACK_PATTERN_INSTALL_USAGE;
import static java.util.stream.Collectors.toMap;

public final class ChangeVnfPackagePatternUtility {

    public static final String ROLLBACK_PATTERN = "rollback_pattern";
    public static final String UPGRADE_PATTERN = "upgrade_pattern";
    public static final String ROLLBACK_AT_FAILURE_PATTERN = "rollback_at_failure_pattern";
    private static final String ROLLBACK_AT_FAILURE_PATTERN_FLOW_SPLIT_REGEX = ",(?![^\\[]*\\])";

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeVnfPackagePatternUtility.class);

    private ChangeVnfPackagePatternUtility() {
    }

    public static List<MutablePair<String, String>> getPattern(JSONObject vnfd, String sourceDescriptorId,
                                                               String destinationDescriptorId,
                                                               String sourceSoftwareVersion,
                                                               String destinationSoftwareVersion,
                                                               String patternKey) {
        TopologyTemplate topologyTemplate = parseTopologyTemplate(vnfd);
        return getPatternFromTemplate(topologyTemplate, sourceDescriptorId, destinationDescriptorId,
                sourceSoftwareVersion, destinationSoftwareVersion, patternKey);
    }

    public static List<MutablePair<String, String>> getRollbackPatternAtFailureForHelmChart(String helmChartKey,
                                                                                            JSONObject vnfd,
                                                                                            String sourceDescriptorId,
                                                                                            String destinationDescriptorId) {
        TopologyTemplate topologyTemplate = parseTopologyTemplate(vnfd);
        return getPatternFromTemplateForHelmChart(helmChartKey, topologyTemplate,
                sourceDescriptorId, destinationDescriptorId, ROLLBACK_AT_FAILURE_PATTERN);
    }

    public static Map<String, Object> getGlobalStaticParams(JSONObject vnfd,
                                                            String sourceDescriptorId,
                                                            String destinationDescriptor) {
        TopologyTemplate topologyTemplate = parseTopologyTemplate(vnfd);
        return getGlobalStaticParamsFromTemplate(topologyTemplate, sourceDescriptorId, destinationDescriptor);
    }

    private static TopologyTemplate parseTopologyTemplate(JSONObject vnfd) {
        VnfDescriptorDetails vnfDescriptorDetails = VnfdUtility.buildVnfDescriptorDetails(vnfd);
        return vnfDescriptorDetails.getDefaultFlavour().getTopologyTemplate();
    }

    private static List<MutablePair<String, String>> getPatternFromTemplate(TopologyTemplate topologyTemplate,
                                                                            String sourceDescriptorId,
                                                                            String destinationDescriptorId,
                                                                            String sourceSoftwareVersion,
                                                                            String destinationSoftwareVersion,
                                                                            String patternKey) {
        Map<String, Object> operationParams = getAllParams(topologyTemplate, sourceDescriptorId, destinationDescriptorId,
                sourceSoftwareVersion, destinationSoftwareVersion, patternKey);
        return fetchPatterns(operationParams, patternKey);
    }

    private static List<MutablePair<String, String>> getPatternFromTemplateForHelmChart(String helmChartKey,
                                                                                        TopologyTemplate topologyTemplate,
                                                                                        String sourceDescriptorId,
                                                                                        String destinationDescriptorId,
                                                                                        String patternKey) {
        Map<String, Object> operationParams = getAllParams(topologyTemplate, sourceDescriptorId, destinationDescriptorId);
        return fetchPatternsForHelmChart(operationParams, helmChartKey, patternKey);
    }

    private static Map<String, Object> getGlobalStaticParamsFromTemplate(TopologyTemplate topologyTemplate,
                                                                         String sourceDescriptorId,
                                                                         String destinationDescriptorId) {
        Map<String, Object> operationParams = getAllParams(topologyTemplate, sourceDescriptorId, destinationDescriptorId);
        removeNonStaticParameters(operationParams);
        return operationParams;
    }

    public static Map<String, Object> getAllParams(TopologyTemplate topologyTemplate, String sourceDescriptorId,
                                                   String destinationDescriptorId) {
        String operationName =
                findCustomInterfaceOperationName(topologyTemplate, sourceDescriptorId, destinationDescriptorId);
        InterfaceTypeImpl customInterfaceNode = getCustomInterfaceNode(topologyTemplate);
        return getOperationInputs(customInterfaceNode, operationName);
    }

    public static Map<String, Object> getAllParams(TopologyTemplate topologyTemplate, String sourceDescriptorId,
                                                   String destinationDescriptorId, String sourceSoftwareVersion,
                                                   String destinationSoftwareVersion, String patternKey) {
        String operationName =
                findCustomInterfaceOperationName(topologyTemplate, sourceDescriptorId, destinationDescriptorId,
                        sourceSoftwareVersion, destinationSoftwareVersion, patternKey);
        InterfaceTypeImpl customInterfaceNode = getCustomInterfaceNode(topologyTemplate);
        return getOperationInputs(customInterfaceNode, operationName);
    }

    private static void removeNonStaticParameters(Map<String, Object> operationParams) {
        operationParams.remove(ROLLBACK_PATTERN);
        operationParams.remove(ROLLBACK_AT_FAILURE_PATTERN);
        operationParams.remove(UPGRADE_PATTERN);
    }

    private static String findCustomInterfaceOperationName(TopologyTemplate topologyTemplate,
                                                           String sourceDescriptorId,
                                                           String destinationDescriptorId) {
        List<String> triggersAction =
                PolicyUtility.getTriggersAction(topologyTemplate, sourceDescriptorId, destinationDescriptorId);
        String customInterfaceName = getCustomInterfaceName(topologyTemplate);
        return findOperationByInterfaceName(triggersAction, customInterfaceName);
    }

    private static String findCustomInterfaceOperationName(TopologyTemplate topologyTemplate,
                                                           String sourceDescriptorId,
                                                           String destinationDescriptorId,
                                                           String sourceSoftwareVersion,
                                                           String destinationSoftwareVersion,
                                                           String patternKey) {
        List<String> triggersAction =
                PolicyUtility.getTriggersAction(topologyTemplate, sourceDescriptorId, destinationDescriptorId,
                        sourceSoftwareVersion, destinationSoftwareVersion, patternKey);
        String customInterfaceName = getCustomInterfaceName(topologyTemplate);
        return findOperationByInterfaceName(triggersAction, customInterfaceName);
    }

    private static String getCustomInterfaceName(TopologyTemplate topologyTemplate) {
        return Optional.ofNullable(topologyTemplate)
                .map(TopologyTemplate::getNodeTemplate)
                .map(NodeTemplate::getCustomInterface)
                .map(Map::keySet)
                .stream()
                .flatMap(Collection::stream)
                .findAny()
                .orElse(null);
    }

    private static Map<String, Object> getOperationInputs(InterfaceTypeImpl interfaceType, String operationName) {
        Map<String, Object> operationsInputs = extractOperationInputs(interfaceType, operationName);
        Map<String, Object> interfaceInputs = extractInterfaceInputs(interfaceType);

        Map<String, Object> resultInputs = new HashMap<>(interfaceInputs);

        operationsInputs.forEach((operationKey, operationValue) ->
                resultInputs.merge(operationKey, operationValue, getDeepMergeFunction()));

        return resultInputs;
    }

    @SuppressWarnings("unchecked")
    private static BiFunction<Object, Object, Object> getDeepMergeFunction() {
        return (resultInput, operationInput) -> {
            if (resultInput instanceof Map) {
                ((Map<String, Object>) resultInput).forEach(((Map<String, Object>) operationInput)::putIfAbsent);
            }
            return operationInput;
        };
    }

    private static String findOperationByInterfaceName(List<String> triggersAction, String customInterfaceName) {
        return triggersAction.stream()
                .filter(interfaceOperation -> interfaceOperation.startsWith(customInterfaceName))
                .findAny()
                .map(ChangeVnfPackagePatternUtility::extractOperationName)
                .orElse(null);
    }

    private static InterfaceTypeImpl getCustomInterfaceNode(TopologyTemplate topologyTemplate) {
        return Optional.ofNullable(topologyTemplate)
                .map(TopologyTemplate::getNodeTemplate)
                .map(NodeTemplate::getCustomInterface)
                .map(Map::values)
                .stream()
                .flatMap(Collection::stream)
                .findAny()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractInterfaceInputs(InterfaceTypeImpl interfaceType) {
        return Optional.ofNullable(interfaceType)
                .map(InterfaceType::getInputs)
                .filter(Map.class::isInstance)
                .map(i -> (Map<String, Object>) i)
                .orElseGet(HashMap::new);
    }

    private static Map<String, Object> extractOperationInputs(InterfaceTypeImpl interfaceType, String operationName) {
        return Optional.ofNullable(interfaceType)
                .map(InterfaceType::getOperation)
                .map(o -> o.get(operationName))
                .map(CustomOperation::getInput)
                .filter(CustomInterfaceInputs.class::isInstance)
                .map(i -> (CustomInterfaceInputs) i)
                .map(CustomInterfaceInputs::getStaticAdditionalParams)
                .orElseGet(HashMap::new);
    }

    public static void validateRollbackPatterns(List<HelmChart> helmCharts, NodeTemplate nodeTemplate) {
        List<String> chartKeys = helmCharts.stream().map(HelmChart::getChartKey)
                .filter(key -> !key.startsWith(CRD_PACKAGE_PREFIX)).collect(Collectors.toList());

        Map<String, InterfaceTypeImpl> customInterface = nodeTemplate.getCustomInterface();
        if (!CollectionUtils.isEmpty(customInterface)) {
            customInterface.forEach((key, interfaceType) -> {
                validateRollbackAtFailurePatternPresent(interfaceType);
                validateGlobalInputs(chartKeys, interfaceType);
                validateOperations(chartKeys, interfaceType);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateRollbackAtFailurePatternPresent(final InterfaceTypeImpl interfaceType) {
        final Map<String, Object> inputs = (Map<String, Object>) interfaceType.getInputs();
        final Map<String, CustomOperation> operations = interfaceType.getOperation();
        boolean isDefaultRollbackAtFailurePatternPresent = Optional.ofNullable(inputs)
                .map(i -> i.containsKey(ROLLBACK_AT_FAILURE_PATTERN))
                .orElse(Boolean.FALSE);

        if (!isDefaultRollbackAtFailurePatternPresent && !CollectionUtils.isEmpty(operations)) {
            validateRollbackAtFailurePatternPresentInOperations(operations);
        }
    }

    private static void validateRollbackAtFailurePatternPresentInOperations(Map<String, CustomOperation> operations) {
        operations.forEach((key2, operation) -> {
            Map<String, Object> staticAdditionalParams = ((CustomInterfaceInputs) operation.getInput())
                    .getStaticAdditionalParams();
            if (CollectionUtils.isEmpty(staticAdditionalParams) || !staticAdditionalParams.containsKey(ROLLBACK_AT_FAILURE_PATTERN)) {
                LOGGER.info("Rollback at failure patterns are not present in current package");
            }
        });
    }

    public static void validatePolicyTriggerActionsContainNoPattern(VnfPackageChangePolicyTosca1dot2 policy,
                                                                     String patternKey,
                                                                     Map<String, Map<String, Object>> triggerActionsInputs) {
        Optional.ofNullable(policy)
                .map(VnfPackageChangePolicyTosca1dot2::getTriggers)
                .ifPresent(triggers -> triggers.stream()
                        .flatMap(triggersMap -> triggersMap.values().stream())
                        .filter(trigger -> trigger.getAction() != null)
                        .forEach(trigger -> validateOperationContainsNoPattern(trigger.getAction(), policy, patternKey,
                                triggerActionsInputs)));
    }

    private static void validateOperationContainsNoPattern(String operation,
                                                           VnfPackageChangePolicyTosca1dot2 policy,
                                                           String patternKey,
                                                           Map<String, Map<String, Object>> operationInputs) {
        if (operationInputs.getOrDefault(operation, Collections.emptyMap()).containsKey(patternKey)) {
            LOGGER.warn(WRONG_PATTERN_FOR_POLICY_OPERATION, operation, patternKey,
                    policy.getProperties().getModificationQualifier());
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateGlobalInputs(final List<String> chartKeys, final InterfaceTypeImpl interfaceType) {
        Map<String, Object> inputs = (Map<String, Object>) interfaceType.getInputs();
        if (!CollectionUtils.isEmpty(inputs)) {
            Map<String, Object> patterns = validateRollbackPatternsKeys(chartKeys, inputs);
            validateRollbackPatternsValues(chartKeys, patterns);
        }
    }

    private static void validateOperations(final List<String> chartKeys, final InterfaceTypeImpl interfaceType) {
        Map<String, CustomOperation> operations = interfaceType.getOperation();
        if (!CollectionUtils.isEmpty(operations)) {
            operations.forEach((key2, operation) -> {
                Map<String, Object> staticAdditionalParams = ((CustomInterfaceInputs) operation.getInput())
                        .getStaticAdditionalParams();
                if (!CollectionUtils.isEmpty(staticAdditionalParams)) {
                    Map<String, Object> operationPatterns = validateRollbackPatternsKeys(chartKeys, staticAdditionalParams);
                    validateRollbackPatternsValues(chartKeys, operationPatterns);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> validateRollbackPatternsKeys(final List<String> chartKeys, final Map<String, Object> inputs) {
        Map<String, Object> patterns = inputs.entrySet().stream()
                .filter(input -> ROLLBACK_PATTERN.equals(input.getKey()) || ROLLBACK_AT_FAILURE_PATTERN
                        .equals(input.getKey())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        patterns.forEach((rollbackPatternKey, pattern) -> validateRollbackPatternKeys(chartKeys,
                (List<Map<String, Object>>) pattern));
        List<Map<String, Object>> rollbackAtFailurePattern = (List<Map<String, Object>>) patterns
                .get(ROLLBACK_AT_FAILURE_PATTERN);
        if (!CollectionUtils.isEmpty(rollbackAtFailurePattern)) {
            List<String> failurePatternChartKeys = rollbackAtFailurePattern.stream()
                    .map(Map::keySet)
                    .flatMap(Collection::stream)
                    .filter(key -> !key.startsWith(CRD_PACKAGE_PREFIX))
                    .collect(Collectors.toList());
            if (isChartsNotMatchRollbackPattern(chartKeys, failurePatternChartKeys)) {
                failurePatternChartKeys.forEach(chartKeys::remove);
                throw new IllegalArgumentException(String.format(MISSING_FAILURE_PATTERN, chartKeys));
            }
        }
        return patterns;
    }

    private static boolean isChartsNotMatchRollbackPattern(List<String> chartKeys, List<String> failurePatternChartKeys) {
        return chartKeys.size() != failurePatternChartKeys.size() || !new HashSet<>(chartKeys).containsAll(failurePatternChartKeys);
    }

    @SuppressWarnings("unchecked")
    private static void validateRollbackPatternsValues(final List<String> chartKeys,
                                                       final Map<String, Object> operationPatterns) {
        List<Map<String, Object>> rollbackAtFailurePattern = (List<Map<String, Object>>) operationPatterns
                .get(ROLLBACK_AT_FAILURE_PATTERN);
        validateRollbackAtFailurePattern(chartKeys, rollbackAtFailurePattern);
        List<Map<String, Object>> rollbackPattern = (List<Map<String, Object>>) operationPatterns.get(ROLLBACK_PATTERN);
        validateRollbackPattern(rollbackPattern);
    }

    private static void validateRollbackAtFailurePattern(final List<String> chartKeys,
                                                         final List<Map<String, Object>> rollbackAtFailurePattern) {
        if (!CollectionUtils.isEmpty(rollbackAtFailurePattern)) {
            try {
                rollbackAtFailurePattern.forEach(patternMap -> failurePattern(patternMap, chartKeys));
            } catch (IndexOutOfBoundsException exception) {
                throw new IllegalArgumentException(PARSING_ISSUE_WITH_ROLLBACK_FAILURE_PATTERN, exception);
            }
        }
    }

    private static void validateRollbackPattern(final List<Map<String, Object>> rollbackPattern) {
        if (!CollectionUtils.isEmpty(rollbackPattern)) {
            rollbackPattern.forEach(ChangeVnfPackagePatternUtility::validateRollbackPatternCommand);
            validateRollbackPatternCommandChain(rollbackPattern);
        }
    }

    private static void validateRollbackPatternKeys(List<String> chartKeys, List<Map<String, Object>> rollbackPattern) {
        if (!CollectionUtils.isEmpty(rollbackPattern)) {
            rollbackPattern.forEach(c -> c.forEach((key, value) -> {
                if (!key.startsWith(CRD_PACKAGE_PREFIX) && !chartKeys.contains(key)) {
                    throw new IllegalArgumentException(
                            String.format(HELMCHART_IN_PATTERN_NOT_FOUND_IN_ARTIFACTS, key, value));
                }
            }));
        }
    }

    private static void validateRollbackPatternCommand(Map<String, Object> map) {
        map.forEach((key, rollbackPattern) -> {
            String requestedCommand = rollbackPattern.toString();
            if (!isSupportedCommand(requestedCommand)) {
                throw new IllegalArgumentException(
                        String.format(UNSUPPORTED_COMMAND_IN_PATTERN, requestedCommand, key));
            }
        });
    }

    private static void failurePattern(Map<String, Object> patternMap, List<String> chartKeys) {
        patternMap.forEach((key, value) -> {
            String patternAsString = value.toString();
            String[] splitPattern = patternAsString.split(ROLLBACK_AT_FAILURE_PATTERN_FLOW_SPLIT_REGEX); //NOSONAR
            for (String part : splitPattern) {
                String[] pattern = part.split(":");
                String chartKey = pattern[0].trim();
                String command = pattern[1].trim();
                if (!chartKey.startsWith(CRD_PACKAGE_PREFIX)) {
                    if (!chartKeys.contains(chartKey)) {
                        throw new IllegalArgumentException(
                                String.format(HELMCHART_IN_PATTERN_NOT_FOUND_IN_ARTIFACTS, chartKey, command));
                    }
                    if (!isSupportedCommand(command)) {
                        throw new IllegalArgumentException(
                                String.format(UNSUPPORTED_COMMAND_IN_PATTERN, command, chartKey));
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<MutablePair<String, String>> fetchPatternsForHelmChart(Map<String, Object> operationVars,
                                                                               String helmChartKey,
                                                                               String patternKey) {
        List<Map<String, Object>> patternMaps =
                (List<Map<String, Object>>) operationVars.getOrDefault(patternKey, new ArrayList<>());

        return patternMaps.stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(pattern -> helmChartKey.equals(pattern.getKey()))
                .map(ChangeVnfPackagePatternUtility::parseRollbackAtFailurePattern)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static List<MutablePair<String, String>> fetchPatterns(Map<String, Object> allOperationParams, String patternKey) {
        List<Map<String, Object>> patternMaps =
                (List<Map<String, Object>>) allOperationParams.getOrDefault(patternKey, new ArrayList<>());

        return patternMaps.stream()
                .filter(map -> !CollectionUtils.isEmpty(map))
                .flatMap(map -> map.entrySet().stream())
                .filter(pattern -> !pattern.getKey().startsWith(CRD_PACKAGE_PREFIX))
                .map(pattern -> new MutablePair<>(pattern.getKey(), pattern.getValue().toString()))
                .collect(Collectors.toList());
    }

    private static List<MutablePair<String, String>> parseRollbackAtFailurePattern(Map.Entry<String, Object> failurePatternChartKeys) {
        List<MutablePair<String, String>> failurePatterns = new ArrayList<>();
        String failure = failurePatternChartKeys.getValue().toString();
        String[] splitRollbackPattern = failure.split(ROLLBACK_AT_FAILURE_PATTERN_FLOW_SPLIT_REGEX); //NOSONAR
        for (String part : splitRollbackPattern) {
            String[] pattern = part.split(":");
            String chartKey = pattern[0].trim();
            String command = pattern[1].trim();
            if (!chartKey.startsWith(CRD_PACKAGE_PREFIX)) {
                failurePatterns.add(new MutablePair<>(chartKey, command));
            }
        }
        return failurePatterns;
    }

    private static String extractOperationName(String interfaceOperationPair) {
        return interfaceOperationPair.substring(interfaceOperationPair.indexOf('.') + 1);
    }

    private static void validateRollbackPatternCommandChain(final List<Map<String, Object>> rollbackPattern) {

        Map<String, LinkedList<String>> commandsToCharts = extractCommandsToCharts(rollbackPattern);
        for (Map.Entry<String, LinkedList<String>> commands: commandsToCharts.entrySet()) {
            validateLastRollbackPatternCommandForChart(commands.getValue(), commands.getKey());

            validateRollbackPatternCommandsSequenceOrder(commands.getValue(), commands.getKey());
        }
    }

    private static void validateRollbackPatternCommandsSequenceOrder(final LinkedList<String> commands, String chartName) {
        boolean isDeleteCommandAppear = false;
        for (String command: commands) {
            if (VnfPackageChangePatternCommand.DELETE.getCommand().equals(command.toLowerCase())
                    || VnfPackageChangePatternCommand.DELETE_PVC.getCommand().equals(command.toLowerCase())) {
                isDeleteCommandAppear = true;
            } else if (VnfPackageChangePatternCommand.INSTALL.getCommand().equals(command.toLowerCase())) {
                if (isDeleteCommandAppear) {
                    isDeleteCommandAppear = false;
                } else {
                    throw new IllegalArgumentException(String.format(WRONG_ROLLBACK_PATTERN_BASE,
                                                                     chartName, WRONG_ROLLBACK_PATTERN_INSTALL_USAGE));
                }
            } else if ((VnfPackageChangePatternCommand.ROLLBACK.getCommand().equals(command.toLowerCase())
                    || VnfPackageChangePatternCommand.UPGRADE.getCommand().equals(command.toLowerCase()))
                    && isDeleteCommandAppear) {
                throw new IllegalArgumentException(String.format(WRONG_ROLLBACK_PATTERN_BASE,
                                                                     chartName, String.format(WRONG_ROLLBACK_PATTERN_DELETE_USAGE,
                                                                                              command.toLowerCase())));
            }
        }
    }

    private static void validateLastRollbackPatternCommandForChart(final LinkedList<String> commands, String chartName) {
        if (VnfPackageChangePatternCommand.UPGRADE.getCommand().equals(commands.getLast().toLowerCase())) {

            throw new IllegalArgumentException(String.format(WRONG_ROLLBACK_PATTERN_BASE,
                                                             chartName, WRONG_ROLLBACK_PATTERN_ENDING));
        }
    }

    private static Map<String, LinkedList<String>> extractCommandsToCharts(final List<Map<String, Object>> pattern) {
        Map<String, LinkedList<String>> commandsToCharts = new HashMap<>();
        for (var chartCommandPair: pattern) {
            for (var chartToCommand: chartCommandPair.entrySet()) {
                commandsToCharts.computeIfAbsent(chartToCommand.getKey(), key -> new LinkedList<>())
                        .add(chartToCommand.getValue().toString());
            }
        }

        return commandsToCharts;
    }
}
