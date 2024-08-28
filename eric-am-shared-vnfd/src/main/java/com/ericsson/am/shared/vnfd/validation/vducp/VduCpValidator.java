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
package com.ericsson.am.shared.vnfd.validation.vducp;

import static com.ericsson.am.shared.vnfd.CommonUtility.validateNoMissedNames;
import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_CP_MISSED_IN_SUBSTITUTION_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_CP_WITH_UNSUPPORTED_VIRTUAL_LINK;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_MISSED_IN_HELM_PARAMS_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_BINDING;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_LINK;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.SubstitutionMappings;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCp;

public final class VduCpValidator {

    private VduCpValidator() {
    }

    public static void validate(TopologyTemplate topologyTemplate) {
        List<VduCp> vduCps = topologyTemplate.getNodeTemplate().getVduCps();
        if (!CollectionUtils.isEmpty(vduCps)) {
            validateVduCpsWithoutInternalVirtualLink(vduCps);
            validateSubstitutionMappings(vduCps, topologyTemplate.getSubstitutionMappings());
            validateHelmParamsMapping(vduCps, topologyTemplate.getPolicies().getAllHelmParamsMappings());
        }
    }

    private static void validateVduCpsWithoutInternalVirtualLink(List<VduCp> vduCps) {
        String vduCpsWithInternalVirtualLink = vduCps.stream()
             .filter(vduCp -> vduCp.getRequirements().containsKey(VIRTUAL_LINK))
             .map(VduCp::getName)
             .collect(Collectors.joining(", "));

        if (StringUtils.isNotBlank(vduCpsWithInternalVirtualLink)) {
            throw new IllegalArgumentException(String.format(VDU_CP_WITH_UNSUPPORTED_VIRTUAL_LINK, vduCpsWithInternalVirtualLink));
        }
    }

    private static void validateSubstitutionMappings(List<VduCp> vduCps, SubstitutionMappings substitutionMappings) {
        if (substitutionMappings == null || CollectionUtils.isEmpty(substitutionMappings.getRequirements())) {
            throw new IllegalArgumentException(SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND);
        }

        Set<String> setOfNamesFromVduCps = vduCps.stream()
              .map(VduCp::getName)
              .collect(Collectors.toSet());

        Set<String> vduCpNamesFromSubstitutionMapping = substitutionMappings.getRequirements().values()
                .stream().map(value -> value.get(0))
                .collect(Collectors.toSet());

        validateNoMissedNames(setOfNamesFromVduCps,
                              vduCpNamesFromSubstitutionMapping,
                              VDU_CP_MISSED_IN_SUBSTITUTION_MAPPINGS);
    }

    private static void validateHelmParamsMapping(List<VduCp> vduCps,
                                                  Map<String, HelmParamsMapping> helmParamsMappings) {
        Set<String> vduNamesFromVduCps = vduCps.stream()
                .map(vduCp -> vduCp.getRequirements().get(VIRTUAL_BINDING))
                .collect(Collectors.toSet());

        Set<String> vduNamesFromHelmParamsMapping = helmParamsMappings.values().stream()
                .filter(helmParamsMapping -> !CollectionUtils.isEmpty(helmParamsMapping.getVdus()))
                .map(helmParamsMapping -> helmParamsMapping.getVdus().keySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        validateNoMissedNames(vduNamesFromVduCps,
                              vduNamesFromHelmParamsMapping,
                              VDU_MISSED_IN_HELM_PARAMS_MAPPINGS);
    }
}
