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
package com.ericsson.am.shared.vnfd.validation.policy;

import static com.ericsson.am.shared.vnfd.utils.Constants.ASPECT_DELTA_NOT_PRESENT_IN_VNFD;
import static com.ericsson.am.shared.vnfd.utils.Constants.DUPLICATE_VDU_IN_INITIAL_DELTA;
import static com.ericsson.am.shared.vnfd.utils.Constants.EMPTY_POLICY_KEY_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INITIAL_DELTA_NOT_PRESENT_IN_VNFD;
import static com.ericsson.am.shared.vnfd.utils.Constants.MISSING_VDU;
import static com.ericsson.am.shared.vnfd.utils.Constants.SCALING_ASPECTS_NOT_PRESENT_IN_INSTANTIATION_LEVELS;
import static com.ericsson.am.shared.vnfd.utils.Constants.SCALING_ASPECT_NOT_PRESENT_IN_VNFD;
import static com.ericsson.am.shared.vnfd.utils.Constants.SCALING_DELTA_NOT_DEFINED;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_SCALING_DELTA_NOT_DEFINED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.am.shared.vnfd.PolicyUtility;

import com.ericsson.am.shared.vnfd.exception.ScalingInfoValidationException;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.policies.InitialDelta;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDataType;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDeltas;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspects;
import com.ericsson.am.shared.vnfd.model.policies.VduInstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import org.springframework.util.CollectionUtils;

public class PolicyValidator {
    private final Map<String, ScalingAspects> allAspects;
    private final Map<String, InitialDelta> allInitialDeltas;
    private final Map<String, ScalingAspectDeltas> allScalingAspectDeltas;
    private final Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy;
    private final Map<String, VduInstantiationLevels> allVduInstantiationLevels;
    private final Map<String, InstantiationLevels> allInstantiationLevels;
    private final Map<String, HelmParamsMapping> allHelmParamsMappings;

    public PolicyValidator(Policies policies) {
        this.allAspects = policies.getAllScalingAspects();
        this.allInitialDeltas = policies.getAllInitialDelta();
        this.allScalingAspectDeltas = policies.getAllScalingAspectDelta();
        this.allVnfPackageChangePolicy = policies.getAllVnfPackageChangePolicy();
        this.allVduInstantiationLevels = policies.getAllVduInstantiationLevels();
        this.allInstantiationLevels = policies.getAllInstantiationLevels();
        this.allHelmParamsMappings = policies.getAllHelmParamsMappings();
    }

    public void validatePolicies() {
        boolean isAllPoliciesEmpty = Stream.of(
                        allAspects,
                        allInitialDeltas,
                        allScalingAspectDeltas,
                        allVnfPackageChangePolicy,
                        allVduInstantiationLevels,
                        allInstantiationLevels,
                        allHelmParamsMappings)
                .allMatch(CollectionUtils::isEmpty);
        if (isAllPoliciesEmpty) {
            throw new ScalingInfoValidationException(EMPTY_POLICY_KEY_ERROR_MESSAGE);
        }

        if (!allInstantiationLevels.isEmpty()) {
            validateScalingAspectsInInstantiationLevels(allAspects, allInstantiationLevels);
        }

        if (!aspectsAndDeltaPoliciesAreEmpty()) {
            validateScalingPoliciesAreNotEmpty(allAspects, allInitialDeltas, allScalingAspectDeltas);
            validateMissingVduScalingAspectDeltas(allAspects, allScalingAspectDeltas, allInitialDeltas);
        }
    }

    public boolean aspectsAndDeltaPoliciesAreEmpty() {
        return Stream.of(allInitialDeltas,
                        allScalingAspectDeltas,
                        allAspects)
                .allMatch(CollectionUtils::isEmpty);
    }

    private static void validateScalingAspectsInInstantiationLevels(final Map<String, ScalingAspects> scalingAspects,
                                                                    final Map<String, InstantiationLevels> instantiationLevels) {
        Set<String> scalingAspectNames = PolicyUtility.getScalingAspectNames(scalingAspects);
        Set<String> scalingAspectNamesFromInstantiationLevels
                = PolicyUtility.getScalingAspectNamesFromInstantiationLevels(instantiationLevels);

        if (!scalingAspectNames.containsAll(scalingAspectNamesFromInstantiationLevels)) {
            throw new ScalingInfoValidationException(SCALING_ASPECTS_NOT_PRESENT_IN_INSTANTIATION_LEVELS);
        }
    }

    private static void validateScalingPoliciesAreNotEmpty(final Map<String, ScalingAspects> allAspects,
                                                           final Map<String, InitialDelta> allInitialDeltas,
                                                           final Map<String, ScalingAspectDeltas> allScalingAspectDeltas) {
        if (allInitialDeltas.isEmpty()) {
            throw new ScalingInfoValidationException(INITIAL_DELTA_NOT_PRESENT_IN_VNFD);
        }
        if (allAspects.isEmpty()) {
            throw new ScalingInfoValidationException(SCALING_ASPECT_NOT_PRESENT_IN_VNFD);
        }
        if (allScalingAspectDeltas.isEmpty()) {
            throw new ScalingInfoValidationException(ASPECT_DELTA_NOT_PRESENT_IN_VNFD);
        }
    }

    private static void validateMissingVduScalingAspectDeltas(final Map<String, ScalingAspects> scalingAspects,
                                                              final Map<String, ScalingAspectDeltas> scalingAspectDeltas,
                                                              final Map<String, InitialDelta> initialDeltas) {
        for (Map.Entry<String, ScalingAspects> entry : scalingAspects.entrySet()) {
            Map<String, ScalingAspectDataType> aspectDefinitions = entry.getValue().getProperties().getAllAspects();
            checkIfVduScalingDeltaDefined(aspectDefinitions, scalingAspectDeltas);
        }

        List<String> vduScalingDeltaTarget = new ArrayList<>();
        for (Map.Entry<String, ScalingAspectDeltas> entry : scalingAspectDeltas.entrySet()) {
            vduScalingDeltaTarget.addAll(Arrays.asList(entry.getValue().getTargets()));
        }

        List<String> vduInitialDeltaTarget = new ArrayList<>();
        for (Map.Entry<String, InitialDelta> entry : initialDeltas.entrySet()) {
            vduInitialDeltaTarget.addAll(Arrays.asList(entry.getValue().getTargets()));
        }
        if ((new HashSet<>(vduInitialDeltaTarget).size()) != vduInitialDeltaTarget.size()) {
            throw new ScalingInfoValidationException(DUPLICATE_VDU_IN_INITIAL_DELTA);
        }
        if (!(new HashSet<>(vduInitialDeltaTarget)).containsAll(new HashSet<>(vduScalingDeltaTarget))) {
            throw new ScalingInfoValidationException(MISSING_VDU);
        }
    }

    private static void checkIfVduScalingDeltaDefined(final Map<String, ScalingAspectDataType> aspectDefinitions,
                                                      final Map<String, ScalingAspectDeltas> scalingAspectDeltas) {

        for (Map.Entry<String, ScalingAspectDataType> entry : aspectDefinitions.entrySet()) {
            String aspectName = entry.getKey();
            List<String> aspectDeltas = entry.getValue().getStepDeltas();

            List<String> scalingDeltaDefinedAspects = scalingAspectDeltas.entrySet().stream()
                    .filter(deltaEntry -> aspectName.equals(deltaEntry.getValue().getProperties().getAspect()))
                    .map(deltaEntry -> {
                        checkIfScalingDeltaDefined(aspectName,
                                deltaEntry.getKey(),
                                deltaEntry.getValue().getProperties().getDeltas().keySet(),
                                aspectDeltas);
                        return deltaEntry.getKey();
                    })
                    .collect(Collectors.toList());

            if (scalingDeltaDefinedAspects.isEmpty()) {
                throw new ScalingInfoValidationException(String.format(VDU_SCALING_DELTA_NOT_DEFINED, aspectName));
            }
        }
    }

    private static void checkIfScalingDeltaDefined(final String aspectName,
                                                   final String delta,
                                                   final Set<String> allScalingDeltas,
                                                   final List<String> allDefinedScalingAspectDelta) {
        for (String tempDelta : allDefinedScalingAspectDelta) {
            if (!allScalingDeltas.contains(tempDelta)) {
                throw new ScalingInfoValidationException(String.format(SCALING_DELTA_NOT_DEFINED,
                        tempDelta, aspectName, delta));
            }
        }
    }
}
