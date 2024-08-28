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
import com.ericsson.am.shared.vnfd.model.DataTypeImpl;
import com.ericsson.am.shared.vnfd.model.Input;
import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import com.ericsson.am.shared.vnfd.model.Property;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.policies.InitialDelta;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevelsDataInfo;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevelsProperties;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDataType;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDeltas;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectProperties;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspects;
import com.ericsson.am.shared.vnfd.model.policies.VduInstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyCommon;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeProperty;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeSelector;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeTriggerTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.parser.HelmParamsMappingParser;
import com.ericsson.am.shared.vnfd.model.policies.parser.InstantiationLevelsParser;
import com.ericsson.am.shared.vnfd.model.policies.parser.ScalingAspectsParser;
import com.ericsson.am.shared.vnfd.model.policies.parser.VduInitialDeltaParser;
import com.ericsson.am.shared.vnfd.model.policies.parser.VduInstantiationLevelsParser;
import com.ericsson.am.shared.vnfd.model.policies.parser.VduScalingAspectDeltasParser;
import com.ericsson.am.shared.vnfd.model.policies.parser.VnfPackageChangeParser;
import com.ericsson.am.shared.vnfd.validation.policy.PolicyValidator;
import com.ericsson.am.shared.vnfd.validation.policy.VnfPackageChangeSelectorValidator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Collection;

import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.ROLLBACK_PATTERN;
import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.UPGRADE_PATTERN;
import static com.ericsson.am.shared.vnfd.ChangeVnfPackagePatternUtility.validatePolicyTriggerActionsContainNoPattern;
import static com.ericsson.am.shared.vnfd.CommonUtility.hasPropertyOfTypeJsonArray;
import static com.ericsson.am.shared.vnfd.InterfaceTypeUtility.buildInterfacesOperationsInputsMap;
import static com.ericsson.am.shared.vnfd.utils.Constants.ACTION_IS_MISSING_ASSOCIATION_WITH_ANY_VNF_PACKAGE_CHANGE_INTERFACE;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTRA_VNF_PACKAGE_CHANGE_POLICY_DEFINED;
import static com.ericsson.am.shared.vnfd.utils.Constants.POLICIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_TOSCA_DEFINITION_VERSION_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY;

public final class PolicyUtility {
    public static final String WILDCARD_DESTINATION_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String DOWN_MODIFICATION_QUALIFIER = "down";
    public static final String UP_MODIFICATION_QUALIFIER = "up";

    private PolicyUtility() {
    }

    public static Map<String, Property> getDowngradeAdditionalParameters(String sourceDescriptorId,
                                                                         String destinationDescriptorId,
                                                                         final TopologyTemplate topologyTemplate) {
        if (validateAdditionalParameters(sourceDescriptorId, destinationDescriptorId, topologyTemplate)) {
            return null; //NOSONAR
        }
        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> operationEntry = getRollBackOperationFromPolicy(sourceDescriptorId,
                destinationDescriptorId, topologyTemplate);

        if (operationEntry == null || operationEntry.getKey() == null) {
            return null; //NOSONAR
        }

        String operationName = operationEntry.getKey();
        //currently only one custom interface type is supported
        String interfaceKey = topologyTemplate.getNodeTemplate().getCustomInterface().keySet().iterator().next();
        Map<String, CustomOperation> allInterfaceOperation = topologyTemplate.getNodeTemplate().getCustomInterface()
                .get(interfaceKey).getOperation();
        return getDataTypeProperties(allInterfaceOperation, operationName);
    }

    public static Map<String, Property> getDowngradeAdditionalParameters(String sourceDescriptorId,
                                                                         String destinationDescriptorId,
                                                                         String sourceSoftwareVersion,
                                                                         String destinationSoftwareVersion,
                                                                         final TopologyTemplate topologyTemplate) {
        if (validateAdditionalParameters(sourceDescriptorId, destinationDescriptorId, topologyTemplate)) {
            return null; //NOSONAR
        }
        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> operationEntry = getRollBackOperationFromPolicy(sourceDescriptorId,
                destinationDescriptorId, sourceSoftwareVersion, destinationSoftwareVersion, topologyTemplate);

        if (operationEntry == null || operationEntry.getKey() == null) {
            return null; //NOSONAR
        }

        String operationName = operationEntry.getKey();
        //currently only one custom interface type is supported
        String interfaceKey = topologyTemplate.getNodeTemplate().getCustomInterface().keySet().iterator().next();
        Map<String, CustomOperation> allInterfaceOperation = topologyTemplate.getNodeTemplate().getCustomInterface()
                .get(interfaceKey).getOperation();
        return getDataTypeProperties(allInterfaceOperation, operationName);
    }

    public static String getActionFromOperation(String sourceDescriptorId,
                                                String destinationDescriptorId,
                                                final TopologyTemplate topologyTemplate,
                                                String customInterfaceName) {
        if (validateAdditionalParameters(sourceDescriptorId, destinationDescriptorId, topologyTemplate)) {
            return null;
        }
        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> policyEntry = getRollBackOperationFromPolicy(sourceDescriptorId, destinationDescriptorId,
                topologyTemplate);
        if (policyEntry == null || policyEntry.getValue() == null) {
            return null;
        }
        VnfPackageChangePolicyTosca1dot2 policy = policyEntry.getValue();

        return policy.getTriggers().stream()
                .flatMap(trigger -> trigger.values().stream())
                .map(VnfPackageChangeTriggerTosca1dot2::getAction)
                .filter(action -> action.startsWith(customInterfaceName))
                .findFirst().orElse(null);
    }

    public static List<String> getTriggersAction(final TopologyTemplate topologyTemplate,
                                                 String sourceDescriptorId,
                                                 String destinationDescriptorId) {
        if (validateAdditionalParameters(sourceDescriptorId, destinationDescriptorId, topologyTemplate)) {
            return Collections.emptyList();
        }

        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> policyEntry = getRollBackOperationFromPolicy(sourceDescriptorId, destinationDescriptorId,
                topologyTemplate);

        return Optional.ofNullable(policyEntry)
                .map(Map.Entry::getValue)
                .map(VnfPackageChangePolicyTosca1dot2::getTriggers)
                .stream()
                .flatMap(Collection::stream)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(VnfPackageChangeTriggerTosca1dot2::getAction)
                .collect(Collectors.toList());
    }

    public static List<String> getTriggersAction(final TopologyTemplate topologyTemplate, String sourceDescriptorId,
                                                 String destinationDescriptorId, String sourceSoftwareVersion,
                                                 String destinationSoftwareVersion, String patternKey) {
        if (validateAdditionalParameters(sourceDescriptorId, destinationDescriptorId, topologyTemplate)) {
            return Collections.emptyList();
        }

        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> policyEntry = null;


        if (UPGRADE_PATTERN.equals(patternKey)) {
            policyEntry = getUpgradeOperationFromPolicy(sourceDescriptorId, destinationDescriptorId,
                    sourceSoftwareVersion, destinationSoftwareVersion, topologyTemplate);
        } else {
            policyEntry = getRollBackOperationFromPolicy(sourceDescriptorId, destinationDescriptorId,
                    sourceSoftwareVersion, destinationSoftwareVersion, topologyTemplate);
        }

        return Optional.ofNullable(policyEntry)
                .map(Map.Entry::getValue)
                .map(VnfPackageChangePolicyTosca1dot2::getTriggers)
                .stream()
                .flatMap(Collection::stream)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(VnfPackageChangeTriggerTosca1dot2::getAction)
                .collect(Collectors.toList());
    }

    private static boolean validateAdditionalParameters(String sourceDescriptorId,
                                                        String destinationDescriptorId,
                                                        final TopologyTemplate topologyTemplate) {
        if (Strings.isBlank(sourceDescriptorId)) {
            throw new IllegalArgumentException("source id can't be null");
        }
        if (Strings.isBlank(destinationDescriptorId)) {
            throw new IllegalArgumentException("target id can't be null");
        }
        return (topologyTemplate == null || topologyTemplate.getPolicies() == null ||
                topologyTemplate.getPolicies().getAllVnfPackageChangePolicy() == null ||
                topologyTemplate.getNodeTemplate() == null);
    }

    public static Policies parseAllPolicies(Map<String, JSONObject> allPolicyMap, Validator validator, String vnfdVersion) {

        Map<String, ScalingAspects> allAspects = new ScalingAspectsParser()
                .parse(allPolicyMap, validator, vnfdVersion);
        Map<String, InitialDelta> allInitialDeltas = new VduInitialDeltaParser()
                .parse(allPolicyMap, validator, vnfdVersion);
        Map<String, ScalingAspectDeltas> allScalingAspectDeltas = new VduScalingAspectDeltasParser()
                .parse(allPolicyMap, validator, vnfdVersion);
        Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy = new VnfPackageChangeParser()
                .parse(allPolicyMap, validator, vnfdVersion);
        Map<String, VduInstantiationLevels> allVduInstantiationLevels = new VduInstantiationLevelsParser()
                .parse(allPolicyMap, validator, vnfdVersion);
        Map<String, InstantiationLevels> allInstantiationLevels = new InstantiationLevelsParser()
                .parse(allPolicyMap, validator, vnfdVersion);
        Map<String, HelmParamsMapping> allHelmParamsMappings = new HelmParamsMappingParser().
                parse(allPolicyMap, validator, vnfdVersion);

        return new Policies.Builder()
                .allScalingAspects(allAspects)
                .allInitialDelta(allInitialDeltas)
                .allVnfPackageChangePolicy(allVnfPackageChangePolicy)
                .allVduInstantiationLevels(allVduInstantiationLevels)
                .allInstantiationLevels(allInstantiationLevels)
                .allScalingAspectDelta(allScalingAspectDeltas)
                .allHelmParamsMappings(allHelmParamsMappings)
                .build();
    }

    private static Map<String, Property> getDataTypeProperties(Map<String, CustomOperation> allInterfaceOperation,
                                                               String operationName) {
        CustomOperation operation = null;
        if (allInterfaceOperation == null || allInterfaceOperation.isEmpty()) {
            return null; //NOSONAR
        } else {
            for (Map.Entry<String, CustomOperation> element : allInterfaceOperation.entrySet()) {
                if (operationName.equals(element.getKey())) {
                    operation = element.getValue();
                }
            }
        }
        Map<String, Property> allProperties = new HashMap<>();
        CustomInterfaceInputs input;
        if (operation == null) {
            return null; //NOSONAR
        } else {
            Input inputs = operation.getInput();
            if (inputs == null) {
                return allProperties;
            } else {
                input = (CustomInterfaceInputs) inputs;
            }
        }
        DataTypeImpl dataTypeImpl = input.getAdditionalParams();
        if (dataTypeImpl == null) {
            return allProperties;
        } else {
            return dataTypeImpl.getProperties();
        }
    }

    @SuppressWarnings("squid:S4248")
    private static Map.Entry<String, VnfPackageChangePolicyTosca1dot2> getRollBackOperationFromPolicy(String sourceDescriptorId,
                                                                                              String destinationDescriptorId,
                                                                                              final TopologyTemplate topologyTemplate) {
        Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy = topologyTemplate.getPolicies()
                .getAllVnfPackageChangePolicy();
        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> genericRollbackPolicy = null;
        for (Map.Entry<String, VnfPackageChangePolicyTosca1dot2> element : allVnfPackageChangePolicy.entrySet()) {
            VnfPackageChangePolicyTosca1dot2 vnfPackageChangePolicy = element.getValue();

            final List<VnfPackageChangeSelector> vnfPackageChangeSelectors = vnfPackageChangePolicy.getProperties().getVnfPackageChangeSelectors();
            final List<String> sourceDescriptorIds = vnfPackageChangeSelectors.stream()
                    .map(VnfPackageChangeSelector::getSourceDescriptorId)
                    .collect(Collectors.toList());
            final List<String> destinationDescriptorIds =
                    vnfPackageChangeSelectors.stream().map(VnfPackageChangeSelector::getDestinationDescriptorId).collect(Collectors.toList());

            if (sourceDescriptorIds.contains(sourceDescriptorId)
                    && DOWN_MODIFICATION_QUALIFIER.equals(vnfPackageChangePolicy.getProperties().getModificationQualifier())) {
                if (destinationDescriptorIds.contains(destinationDescriptorId)) {
                    return element;
                }
                if (destinationDescriptorIds.contains(WILDCARD_DESTINATION_UUID)) {
                    genericRollbackPolicy = element;
                }
            }
        }
        return genericRollbackPolicy;
    }

    @SuppressWarnings("squid:S4248")
    private static Map.Entry<String, VnfPackageChangePolicyTosca1dot2> getRollBackOperationFromPolicy(String sourceDescriptorId,
                                                                                                      String destinationDescriptorId,
                                                                                                      String sourceSoftwareVersion,
                                                                                                      String destinationSoftwareVersion,
                                                                                                      final TopologyTemplate topologyTemplate) {
        Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy = topologyTemplate.getPolicies()
                .getAllVnfPackageChangePolicy().entrySet()
                .stream()
                .filter(policy -> DOWN_MODIFICATION_QUALIFIER.equals(policy.getValue().getProperties().getModificationQualifier()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> element = getVnfPackageChangePolicyTosca1dot2Entry(sourceDescriptorId,
                destinationDescriptorId, allVnfPackageChangePolicy);

        if (element != null || sourceSoftwareVersion == null || destinationSoftwareVersion == null) {
            return element;
        }

        return filterPoliciesBySoftwareVersionRollback(sourceSoftwareVersion, destinationSoftwareVersion, allVnfPackageChangePolicy);

    }

    private static Map.Entry<String, VnfPackageChangePolicyTosca1dot2> getVnfPackageChangePolicyTosca1dot2Entry(
            String sourceDescriptorId,
            String destinationDescriptorId,
            Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
        for (Map.Entry<String, VnfPackageChangePolicyTosca1dot2> element : allVnfPackageChangePolicy.entrySet()) {
            VnfPackageChangePolicyTosca1dot2 vnfPackageChangePolicy = element.getValue();

            final List<VnfPackageChangeSelector> vnfPackageChangeSelectors = vnfPackageChangePolicy.getProperties().getVnfPackageChangeSelectors();
            final List<String> sourceDescriptorIds = vnfPackageChangeSelectors.stream()
                    .map(VnfPackageChangeSelector::getSourceDescriptorId)
                    .collect(Collectors.toList());
            final List<String> destinationDescriptorIds =
                    vnfPackageChangeSelectors.stream().map(VnfPackageChangeSelector::getDestinationDescriptorId).collect(Collectors.toList());


            if (sourceDescriptorIds.contains(sourceDescriptorId) && destinationDescriptorIds.contains(destinationDescriptorId)) {
                return element;
            } else if (DOWN_MODIFICATION_QUALIFIER.equals(element.getValue().getProperties().getModificationQualifier())
                    && destinationDescriptorIds.contains(WILDCARD_DESTINATION_UUID)) {
                return element;
            }

        }
        return null;
    }


    @SuppressWarnings("squid:S4248")
    private static Map.Entry<String, VnfPackageChangePolicyTosca1dot2> getUpgradeOperationFromPolicy(String sourceDescriptorId,
                                                                                                     String destinationDescriptorId,
                                                                                                     String sourceSoftwareVersion,
                                                                                                     String destinationSoftwareVersion,
                                                                                                     final TopologyTemplate topologyTemplate) {
        Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy = topologyTemplate.getPolicies()
                .getAllVnfPackageChangePolicy().entrySet()
                .stream()
                .filter(policy -> UP_MODIFICATION_QUALIFIER.equals(policy.getValue().getProperties().getModificationQualifier()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map.Entry<String, VnfPackageChangePolicyTosca1dot2> element = getVnfPackageChangePolicyTosca1dot2Entry(sourceDescriptorId,
                destinationDescriptorId, allVnfPackageChangePolicy);

        if (element != null || sourceSoftwareVersion == null || destinationSoftwareVersion == null) {
            return element;
        }

        return filterPoliciesBySoftwareVersionUpgrade(sourceSoftwareVersion,
                 destinationSoftwareVersion, allVnfPackageChangePolicy);
    }

    private static Map.Entry<String, VnfPackageChangePolicyTosca1dot2> filterPoliciesBySoftwareVersionUpgrade(
            String sourceSoftwareVersion,
            String destinationSoftwareVersion,
            Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
        var filteredPolicyByDestinationAndSourceSoftwareVersion =
                filteredPolicyByDestinationSoftwareVersion(destinationSoftwareVersion, allVnfPackageChangePolicy)
                        .filter(entry -> {
                            VnfPackageChangePolicyTosca1dot2 policy = entry.getValue();
                            var selectors = Optional.ofNullable(policy.getProperties())
                                    .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                                    .orElseGet(Collections::emptyList);
                            return selectors.stream()
                                    .filter(x -> x.getSourceSoftwareVersion() != null)
                                    .anyMatch(selector -> sourceSoftwareVersion
                                            .equals(selector.getSourceSoftwareVersion()));
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!filteredPolicyByDestinationAndSourceSoftwareVersion.isEmpty()) {
            return filteredPolicyByDestinationAndSourceSoftwareVersion
                    .entrySet()
                    .iterator()
                    .next();
        }

        var filteredPolicyByDestinationAndRegexSourceSoftwareVersion =
                filteredPolicyByDestinationSoftwareVersion(destinationSoftwareVersion, allVnfPackageChangePolicy)
                        .filter(entry -> {
                            VnfPackageChangePolicyTosca1dot2 policy = entry.getValue();
                            var selectors = Optional.ofNullable(policy.getProperties())
                                    .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                                    .orElseGet(Collections::emptyList);
                            return selectors.stream()
                                    .filter(x -> x.getSourceSoftwareVersion() != null)
                                    .anyMatch(selector -> Pattern.matches(selector.getSourceSoftwareVersion(),
                                            sourceSoftwareVersion));
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filteredPolicyByDestinationAndRegexSourceSoftwareVersion.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .findFirst()
                .orElse(null);

    }

    private static Map.Entry<String, VnfPackageChangePolicyTosca1dot2> filterPoliciesBySoftwareVersionRollback(
            String sourceSoftwareVersion,
            String destinationSoftwareVersion,
            Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {


        var filteredPolicyByDestinationAndSourceSoftwareVersion =
                filteredPolicyBySourceSoftwareVersion(sourceSoftwareVersion, allVnfPackageChangePolicy)
                        .filter(entry -> {
                            VnfPackageChangePolicyTosca1dot2 policy = entry.getValue();
                            var selectors = Optional.ofNullable(policy.getProperties())
                                    .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                                    .orElseGet(Collections::emptyList);
                            return selectors.stream()
                                    .filter(x -> x.getSourceSoftwareVersion() != null)
                                    .anyMatch(selector -> destinationSoftwareVersion
                                            .equals(selector.getDestinationSoftwareVersion()));
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!filteredPolicyByDestinationAndSourceSoftwareVersion.isEmpty()) {
            return filteredPolicyByDestinationAndSourceSoftwareVersion
                    .entrySet()
                    .iterator()
                    .next();
        }

        var filteredPolicyByDestinationRegexAndSourceSoftwareVersion =
                filteredPolicyBySourceSoftwareVersion(sourceSoftwareVersion, allVnfPackageChangePolicy)
                        .filter(entry -> {
                            VnfPackageChangePolicyTosca1dot2 policy = entry.getValue();
                            var selectors = Optional.ofNullable(policy.getProperties())
                                    .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                                    .orElseGet(Collections::emptyList);
                            return selectors.stream()
                                    .filter(x -> x.getSourceSoftwareVersion() != null)
                                    .anyMatch(selector -> Pattern.matches(selector.getDestinationSoftwareVersion(),
                                            destinationSoftwareVersion));
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filteredPolicyByDestinationRegexAndSourceSoftwareVersion.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .findFirst()
                .orElse(null);

    }

    private static Stream<Map.Entry<String, VnfPackageChangePolicyTosca1dot2>> filteredPolicyByDestinationSoftwareVersion(
            String destinationSoftwareVersion, Map<String,
            VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
        return allVnfPackageChangePolicy
                .entrySet().stream()
                .filter(entry -> {
                    VnfPackageChangePolicyTosca1dot2 policy = entry.getValue();
                    var selectors = Optional.ofNullable(policy.getProperties())
                            .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                            .orElseGet(Collections::emptyList);
                    return selectors.stream()
                            .filter(x -> x.getDestinationSoftwareVersion() != null)
                            .anyMatch(selector -> destinationSoftwareVersion
                                    .equals(selector.getDestinationSoftwareVersion()));
                });
    }

    private static Stream<Map.Entry<String, VnfPackageChangePolicyTosca1dot2>> filteredPolicyBySourceSoftwareVersion(
            String sourceSoftwareVersion, Map<String,
            VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
        return allVnfPackageChangePolicy
                .entrySet().stream()
                .filter(entry -> {
                    VnfPackageChangePolicyTosca1dot2 policy = entry.getValue();
                    var selectors = Optional.ofNullable(policy.getProperties())
                            .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                            .orElseGet(Collections::emptyList);
                    return selectors.stream()
                            .filter(x -> x.getDestinationSoftwareVersion() != null)
                            .anyMatch(selector -> sourceSoftwareVersion
                                    .equals(selector.getSourceSoftwareVersion()));
                });
    }

    public static void validateVnfPackageChangePolicy(final TopologyTemplate topologyTemplate) {
        boolean rollbackIsNotDefined = topologyTemplate == null
                || topologyTemplate.getNodeTemplate() == null
                || topologyTemplate.getNodeTemplate().getCustomInterface() == null
                || topologyTemplate.getNodeTemplate().getCustomInterface().isEmpty();
        if (rollbackIsNotDefined) {
            return;
        }

        if (topologyTemplate.getPolicies() == null
                || topologyTemplate.getPolicies().getAllVnfPackageChangePolicy().isEmpty()) {
            throw new IllegalArgumentException("VNF package change policy not defined for VNF package change interface");
        }
        validateOperationPresent(topologyTemplate);
    }

    private static void validateOperationPresent(final TopologyTemplate topologyTemplate) {
        // currently, only one custom interface type is supported - ChangeCurrentVnfPackage
        InterfaceTypeImpl vnfPackageChangeInterface
                = topologyTemplate.getNodeTemplate().getCustomInterface().values().iterator().next();
        Map<String, CustomOperation> vnfPackageChangeOperations
                = vnfPackageChangeInterface.getOperation();
        Map<String, VnfPackageChangePolicyTosca1dot2> vnfPackageChangePolicies = getStringVnfPackageChangePolicyTosca1dot2Map(
                topologyTemplate, vnfPackageChangeOperations);

        for (String vnfPackageChangePolicyName : vnfPackageChangePolicies.keySet()) {
            if (!vnfPackageChangeOperations.containsKey(vnfPackageChangePolicyName)) {
                throw new IllegalArgumentException(String.format(
                        ACTION_IS_MISSING_ASSOCIATION_WITH_ANY_VNF_PACKAGE_CHANGE_INTERFACE, vnfPackageChangePolicyName));
            }
        }
    }

    private static Map<String, VnfPackageChangePolicyTosca1dot2> getStringVnfPackageChangePolicyTosca1dot2Map(
           final TopologyTemplate topologyTemplate, final Map<String, CustomOperation> vnfPackageChangeOperations) {
        Map<String, VnfPackageChangePolicyTosca1dot2> vnfPackageChangePolicies
                = topologyTemplate.getPolicies().getAllVnfPackageChangePolicy();

        if (vnfPackageChangeOperations.size() > vnfPackageChangePolicies.size()) {
            throw new IllegalArgumentException(VNF_PACKAGE_CHANGE_OPERATION_DEFINITION_MISSING_IN_POLICY);
        } else if (vnfPackageChangeOperations.size() < vnfPackageChangePolicies.size()) {
            throw new IllegalArgumentException(EXTRA_VNF_PACKAGE_CHANGE_POLICY_DEFINED);
        }
        return vnfPackageChangePolicies;
    }

    public static void validateVnfPackageChangePoliciesSelectorsAndPatterns(JSONObject vnfd) {
        NodeType nodeType = NodeTypeUtility.buildNodeType(vnfd);
        Flavour defaultFlavour = FlavourUtility.getDefaultFlavour(vnfd, nodeType);
        TopologyTemplate topologyTemplate = defaultFlavour.getTopologyTemplate();

        validateVnfPackageChangeSelectors(topologyTemplate);
        validatePoliciesPatterns(topologyTemplate);
    }

    private static void validateVnfPackageChangeSelectors(TopologyTemplate topologyTemplate) {
        Map<String, VnfPackageChangePolicyTosca1dot2> vnfPackageChangePolicies =
                Optional.ofNullable(topologyTemplate.getPolicies())
                        .map(Policies::getAllVnfPackageChangePolicy)
                        .orElse(Collections.emptyMap());
        VnfPackageChangeSelectorValidator validator = new VnfPackageChangeSelectorValidator(vnfPackageChangePolicies);

        validator.validate();
        if (!validator.getErrors().isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", validator.getErrors()));
        }
    }

    private static void validatePoliciesPatterns(TopologyTemplate topologyTemplate) {
        Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicies =
                Optional.ofNullable(topologyTemplate.getPolicies())
                        .map(Policies::getAllVnfPackageChangePolicy)
                        .orElse(Collections.emptyMap());
        Map<String, InterfaceTypeImpl> customInterfaces = Optional.ofNullable(topologyTemplate.getNodeTemplate())
                .map(NodeTemplate::getCustomInterface)
                .orElse(Collections.emptyMap());
        Map<String, Map<String, Object>> interfacesOperationsInputs = buildInterfacesOperationsInputsMap(customInterfaces);

        validateUpgradePoliciesPatterns(allVnfPackageChangePolicies, interfacesOperationsInputs);
        validateRollbackPoliciesPatterns(allVnfPackageChangePolicies, interfacesOperationsInputs);
    }

    private static void validateUpgradePoliciesPatterns(Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicies,
                                                        Map<String, Map<String, Object>> interfacesOperationsInputs) {
        Map<String, VnfPackageChangePolicyTosca1dot2> upgradePolicies =
                filterVnfPackageChangePoliciesByModificationQualifier(
                        allVnfPackageChangePolicies, PolicyUtility.UP_MODIFICATION_QUALIFIER);

        upgradePolicies.values().forEach(value ->
                validatePolicyTriggerActionsContainNoPattern(value, ROLLBACK_PATTERN, interfacesOperationsInputs));
    }

    private static void validateRollbackPoliciesPatterns(Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicies,
                                                         Map<String, Map<String, Object>> interfacesOperationsInputs) {
        Map<String, VnfPackageChangePolicyTosca1dot2> rollbackPolicies =
                filterVnfPackageChangePoliciesByModificationQualifier(
                        allVnfPackageChangePolicies, PolicyUtility.DOWN_MODIFICATION_QUALIFIER);

        rollbackPolicies.values().forEach(value ->
                validatePolicyTriggerActionsContainNoPattern(value, UPGRADE_PATTERN, interfacesOperationsInputs));
    }

    @SuppressWarnings("ConstantConditions")
    private static Map<String, VnfPackageChangePolicyTosca1dot2> filterVnfPackageChangePoliciesByModificationQualifier(
            Map<String, VnfPackageChangePolicyTosca1dot2> policies, String modificationQualifier) {
        return policies.entrySet().stream()
                .filter(entry -> Objects.equals(Optional.ofNullable(entry.getValue())
                        .map(VnfPackageChangePolicyCommon::getProperties)
                        .map(VnfPackageChangeProperty::getModificationQualifier)
                        .orElse(StringUtils.EMPTY), modificationQualifier))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Policies createAndValidatePolicies(JSONObject vnfd) {
        return getPoliciesFromVnfd(vnfd, true);
    }

    public static Policies createPolicies(JSONObject vnfd) {
        return getPoliciesFromVnfd(vnfd, false);
    }

    private static Policies getPoliciesFromVnfd(JSONObject vnfd, boolean isValidationEnabled) {
        String vnfdVersion = vnfd.getString(VNFD_TOSCA_DEFINITION_VERSION_KEY);
        Policies policies = null;
        JSONObject topologyTemplate = CommonUtility.getTopologyTemplate(vnfd);
        if (isTopologyTemplateValid(topologyTemplate)) {
            Validator validator = getValidator();
            Map<String, JSONObject> allPolicyMap = getAllPoliciesObjectMap(topologyTemplate);

            policies = parseAllPolicies(allPolicyMap, validator, vnfdVersion);

            PolicyValidator policyValidator = new PolicyValidator(policies);
            if (isValidationEnabled) {
                policyValidator.validatePolicies();
            }

            if (!policyValidator.aspectsAndDeltaPoliciesAreEmpty()) {
                settingAspectDeltaInScalingAspect(
                        policies.getAllScalingAspects(),
                        policies.getAllScalingAspectDelta(),
                        policies.getAllInitialDelta());
            }
        }
        return policies;
    }

    private static Map<String, JSONObject> getAllPoliciesObjectMap(JSONObject topologyTemplate) {
        JSONArray allPolicies = topologyTemplate.getJSONArray(POLICIES_KEY);

        Map<String, JSONObject> allPolicyMap = new HashMap<>();
        IntStream
                .range(0, allPolicies.length())
                .mapToObj(allPolicies::getJSONObject).forEach(policy -> {
                    String key = policy.keys().next();
                    allPolicyMap.put(key, policy.getJSONObject(key));
                });
        return allPolicyMap;
    }

    private static Validator getValidator() {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    private static boolean isTopologyTemplateValid(JSONObject topologyTemplate) {
        return hasPropertyOfTypeJsonArray(topologyTemplate, POLICIES_KEY);
    }

    private static void settingAspectDeltaInScalingAspect(Map<String, ScalingAspects> allAspectsDefinedUnderPolicies,
                                                          Map<String, ScalingAspectDeltas> allScalingAspectDeltas,
                                                          Map<String, InitialDelta> allInitialDeltas) {
        for (Map.Entry<String, ScalingAspects> entry : allAspectsDefinedUnderPolicies.entrySet()) {
            Map<String, ScalingAspectDataType> allAspects = entry.getValue().getProperties().getAllAspects();
            for (Map.Entry<String, ScalingAspectDataType> aspectEntry : allAspects.entrySet()) {
                ScalingAspectDataType scalingAspectValue = aspectEntry.getValue();
                Map<String, ScalingAspectDeltas> selectedAspectDelta =
                        getAllAspectDeltaForProvidedAspect(aspectEntry.getKey(),
                                allScalingAspectDeltas);
                setInitialDeltaInAspectDelta(selectedAspectDelta, allInitialDeltas);
                scalingAspectValue.setAllScalingAspectDelta(selectedAspectDelta);
            }
        }
    }

    private static void setInitialDeltaInAspectDelta(Map<String, ScalingAspectDeltas> selectedAspectDelta,
                                                     Map<String, InitialDelta> allInitialDeltas) {

        for (Map.Entry<String, ScalingAspectDeltas> aspectDeltaEntry : selectedAspectDelta.entrySet()) {
            ScalingAspectDeltas aspectDelta = aspectDeltaEntry.getValue();
            aspectDelta.setAllInitialDelta(getAllInitialDeltaForScalingAspectDelta(aspectDelta, allInitialDeltas));
        }
    }

    private static Map<String, InitialDelta> getAllInitialDeltaForScalingAspectDelta(
            ScalingAspectDeltas scalingAspectDelta, Map<String, InitialDelta> allInitialDelta) {
        Map<String, InitialDelta> selectedInitialDelta = new HashMap<>();
        String[] targets = scalingAspectDelta.getTargets();
        for (String target : targets) {
            String initialDeltaKey = getInitialDeltaKeyForProvidedTarget(target, allInitialDelta);
            selectedInitialDelta.put(initialDeltaKey, allInitialDelta.get(initialDeltaKey));
        }
        return selectedInitialDelta;
    }

    private static String getInitialDeltaKeyForProvidedTarget(String target, Map<String, InitialDelta> allInitialDelta) {
        for (Map.Entry<String, InitialDelta> initialDeltaEntry : allInitialDelta.entrySet()) {
            if (Arrays.asList(initialDeltaEntry.getValue().getTargets()).contains(target)) {
                return initialDeltaEntry.getKey();
            }
        }
        return null;
    }

    private static Map<String, ScalingAspectDeltas> getAllAspectDeltaForProvidedAspect(
            String aspectId, Map<String, ScalingAspectDeltas> allScalingAspectDeltas) {
        Map<String, ScalingAspectDeltas> selectedScalingAspectDeltas = new HashMap<>();

        for (Map.Entry<String, ScalingAspectDeltas> aspectDeltaEntry : allScalingAspectDeltas.entrySet()) {
            ScalingAspectDeltas tempScalingAspectDelta = aspectDeltaEntry.getValue();
            if (aspectId.equals(tempScalingAspectDelta.getProperties().getAspect())) {
                selectedScalingAspectDeltas.put(aspectDeltaEntry.getKey(), tempScalingAspectDelta);
            }
        }
        return selectedScalingAspectDeltas;
    }

    public static Set<String> getScalingAspectNames(final Map<String, ScalingAspects> allAspectsDefinedUnderPolicies) {
        return allAspectsDefinedUnderPolicies.values().stream()
                .map(ScalingAspects::getProperties)
                .map(ScalingAspectProperties::getAllAspects)
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());
    }

    public static Set<String> getScalingAspectNamesFromInstantiationLevels(final Map<String, InstantiationLevels> allInstantiationLevels) {
        return allInstantiationLevels.values().stream()
                .map(InstantiationLevels::getProperties)
                .map(InstantiationLevelsProperties::getInstantiationLevelsDataInfo)
                .flatMap(map -> map.values().stream())
                .map(InstantiationLevelsDataInfo::getScaleInfo)
                .filter(Objects::nonNull)
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());
    }
}