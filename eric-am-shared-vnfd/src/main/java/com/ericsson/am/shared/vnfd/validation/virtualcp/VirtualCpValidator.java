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
package com.ericsson.am.shared.vnfd.validation.virtualcp;

import static com.ericsson.am.shared.vnfd.CommonUtility.validateNoMissedNames;
import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_CP_MISSED_IN_HELM_PARAMS_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_CP_MISSED_IN_SUBSTITUTION_MAPPINGS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.SubstitutionMappings;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VirtualCp;

public final class VirtualCpValidator {

    private VirtualCpValidator() {}

    public static void validate(TopologyTemplate topologyTemplate) {
        List<VirtualCp> virtualCps = topologyTemplate.getNodeTemplate().getVirtualCps();
        if (!CollectionUtils.isEmpty(virtualCps)) {
            validateSubstitutionMappings(virtualCps, topologyTemplate.getSubstitutionMappings());
            validateHelmParamsMapping(virtualCps, topologyTemplate.getPolicies().getAllHelmParamsMappings());
        }
    }

    private static void validateHelmParamsMapping(List<VirtualCp> virtualCps,
                                                  Map<String, HelmParamsMapping> helmParamsMappings) {
        Set<String> namesOfVirtualCps = virtualCps.stream()
              .map(VirtualCp::getName)
              .collect(Collectors.toSet());

        Set<String> namesOfVirtualCpsFromHelmParamsMapping = helmParamsMappings.values().stream()
              .filter(helmParamsMapping -> !CollectionUtils.isEmpty(helmParamsMapping.getExtCps()))
              .map(helmParamsMapping -> helmParamsMapping.getExtCps().keySet())
              .flatMap(Collection::stream)
              .collect(Collectors.toSet());

        validateNoMissedNames(namesOfVirtualCps,
                              namesOfVirtualCpsFromHelmParamsMapping,
                              VIRTUAL_CP_MISSED_IN_HELM_PARAMS_MAPPINGS);
    }

    private static void validateSubstitutionMappings(List<VirtualCp> virtualCps, SubstitutionMappings substitutionMappings) {
        if (substitutionMappings == null || CollectionUtils.isEmpty(substitutionMappings.getRequirements())) {
            throw new IllegalArgumentException(SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND);
        }

        Set<String> namesOfVirtualCps = virtualCps.stream()
                .map(VirtualCp::getName)
                .collect(Collectors.toSet());

        Set<String> allNamesFromSubstitutionMapping = substitutionMappings.getRequirements().values()
                .stream().map(value -> value.get(0))
                .collect(Collectors.toSet());

        validateNoMissedNames(namesOfVirtualCps,
                              allNamesFromSubstitutionMapping,
                              VIRTUAL_CP_MISSED_IN_SUBSTITUTION_MAPPINGS);
    }
}
