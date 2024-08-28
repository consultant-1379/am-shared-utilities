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

import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyCommon;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangePolicyTosca1dot2;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeProperty;
import com.ericsson.am.shared.vnfd.model.policies.VnfPackageChangeSelector;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class VnfPackageChangeSelectorValidator {

    public static final String NONE_OF_CCVP_OPTIONS_HAS_REQUIRED_FIELDS_SET =
            "None of package change options in selector (VNF Descriptor ID and Software Version) for VnfPackageChange " +
                    "policy %s has all required fields set";
    public static final String ONLY_ONE_CCVP_OPTION_ALLOWED =
            "Only one package change option in selector (Software Version or VNF Descriptor ID) for VnfPackageChange " +
                    "policy %s allowed. Other package change options fields shall be empty";
    public static final String ONLY_ONE_SELECTOR_ALLOWED_FOR_POLICY =
            "VnfPackageChange policy %s has more than one selector. Only one selector allowed for each policy";
    public static final String SELECTORS_IN_POLICIES_NOT_UNIQUE =
            "Duplicate selectors found in VnfPackageChange policies. The selectors shall be unique";

    private final Set<String> errors = new HashSet<>();
    private final Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy;

    public VnfPackageChangeSelectorValidator(@NotNull Map<String, VnfPackageChangePolicyTosca1dot2> allVnfPackageChangePolicy) {
        this.allVnfPackageChangePolicy = allVnfPackageChangePolicy;
    }

    public void validate() {
        Map<String, VnfPackageChangePolicyCommon> policiesWithNonNullSelectors = allVnfPackageChangePolicy.entrySet().stream()
                .filter(policy -> Optional.ofNullable(policy.getValue())
                        .map(VnfPackageChangePolicyCommon::getProperties)
                        .map(VnfPackageChangeProperty::getVnfPackageChangeSelectors)
                        .isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        validateEachPolicyHasTheOnlySelector(policiesWithNonNullSelectors);
        validateAllSelectorsAreUnique(policiesWithNonNullSelectors.values());
        validateSelectors(policiesWithNonNullSelectors);
    }

    private void validateEachPolicyHasTheOnlySelector(Map<String, VnfPackageChangePolicyCommon> policies) {
        policies.entrySet().stream()
                .filter(policy -> policy.getValue().getProperties().getVnfPackageChangeSelectors().size() > 1)
                .findAny()
                .ifPresent(policy -> errors.add(String.format(ONLY_ONE_SELECTOR_ALLOWED_FOR_POLICY, policy.getKey())));
    }

    private void validateAllSelectorsAreUnique(Collection<VnfPackageChangePolicyCommon> policies) {
        Set<VnfPackageChangeSelector> uniqueSelectors = policies.stream()
                .flatMap(policy -> policy.getProperties().getVnfPackageChangeSelectors().stream())
                .collect(Collectors.toSet());

        List<VnfPackageChangeSelector> allSelectors = policies.stream()
                .flatMap(policy -> policy.getProperties().getVnfPackageChangeSelectors().stream())
                .collect(Collectors.toList());

        if (allSelectors.size() != uniqueSelectors.size()) {
            errors.add(SELECTORS_IN_POLICIES_NOT_UNIQUE);
        }
    }

    private void validateSelectors(Map<String, VnfPackageChangePolicyCommon> policies) {
        policies.forEach((policyKey, policy) ->
                validatePolicySelectorsFields(policyKey, policy.getProperties().getVnfPackageChangeSelectors()));
    }

    private void validatePolicySelectorsFields(String policyKey, List<VnfPackageChangeSelector> selectors) {
        selectors.forEach(selector -> {
            if (noneOfUpgradeOptionsHasRequiredFieldsSet(selector)) {
                errors.add(String.format(NONE_OF_CCVP_OPTIONS_HAS_REQUIRED_FIELDS_SET, policyKey));
            }
            if (isBothUpgradeOptionFieldsSet(selector)) {
                errors.add(String.format(ONLY_ONE_CCVP_OPTION_ALLOWED, policyKey));
            }
        });
    }

    private static boolean noneOfUpgradeOptionsHasRequiredFieldsSet(VnfPackageChangeSelector selector) {
        return !StringUtils.isNoneBlank(selector.getSourceDescriptorId(), selector.getDestinationDescriptorId()) &&
                !StringUtils.isNoneBlank(selector.getSourceSoftwareVersion(), selector.getDestinationSoftwareVersion());
    }

    private static boolean isBothUpgradeOptionFieldsSet(VnfPackageChangeSelector selector) {
        return StringUtils.isAllBlank(selector.getSourceDescriptorId(), selector.getDestinationDescriptorId()) ==
                StringUtils.isAllBlank(selector.getSourceSoftwareVersion(), selector.getDestinationSoftwareVersion()) &&
                !StringUtils.isAllBlank(selector.getSourceDescriptorId(), selector.getDestinationDescriptorId(),
                        selector.getSourceSoftwareVersion(), selector.getDestinationSoftwareVersion());
    }

    public Set<String> getErrors() {
        return errors;
    }
}