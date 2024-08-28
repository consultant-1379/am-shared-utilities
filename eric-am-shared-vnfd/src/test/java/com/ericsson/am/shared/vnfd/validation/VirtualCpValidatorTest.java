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
package com.ericsson.am.shared.vnfd.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_CP_MISSED_IN_HELM_PARAMS_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_CP_MISSED_IN_SUBSTITUTION_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_VIRTUAL_CP;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_POLICIES;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.TopologyTemplateUtility;
import com.ericsson.am.shared.vnfd.VnfdUtility;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.utils.TestUtils;
import com.ericsson.am.shared.vnfd.validation.virtualcp.VirtualCpValidator;

public class VirtualCpValidatorTest {

    @Test
    public void validateCorrectTopologyTemplate() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_VIRTUAL_CP)
                                                                              .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        assertDoesNotThrow(() -> VirtualCpValidator.validate(topologyTemplate));
    }

    @Test
    public void validateTopologyTemplateWithoutVirtualCp() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_WITH_POLICIES)
                                                                              .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        assertDoesNotThrow(() -> VirtualCpValidator.validate(topologyTemplate));
    }

    @Test
    public void validateTopologyTemplateWithMissedVirtualInHelmParamsMapping() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_VIRTUAL_CP)
                                                                              .toAbsolutePath());
        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());

        Map.Entry<String, HelmParamsMapping> helmParamsMappingEntry =
              topologyTemplate.getPolicies().getAllHelmParamsMappings().entrySet().stream().findFirst().get();
        String missedVduName = helmParamsMappingEntry.getValue().getExtCps().keySet().stream().findFirst().get();
        topologyTemplate.getPolicies().getAllHelmParamsMappings().remove(helmParamsMappingEntry.getKey());

        assertThatThrownBy(() -> VirtualCpValidator.validate(topologyTemplate))
              .isInstanceOf(IllegalArgumentException.class)
              .hasMessage(String.format(VIRTUAL_CP_MISSED_IN_HELM_PARAMS_MAPPINGS, missedVduName));
    }

    @Test
    public void validateTopologyTemplateWithVirtualCpAndEmptySubstitutionMapping() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_VIRTUAL_CP)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        topologyTemplate.setSubstitutionMappings(null);
        assertThatThrownBy(() -> VirtualCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND);
    }

    @Test
    public void validateTopologyTemplateWithVirtualCpAndEmptySubstitutionMappingRequirements() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_VIRTUAL_CP)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        topologyTemplate.getSubstitutionMappings().setRequirements(Collections.emptyMap());

        assertThatThrownBy(() -> VirtualCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND);
    }

    @Test
    public void validateTopologyTemplateWithMissedVirtualCp() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_VIRTUAL_CP)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        Map.Entry<String, List<String>> requirementEntry =
                topologyTemplate.getSubstitutionMappings().getRequirements().entrySet().stream().findFirst().get();
        String missedVirtualCpName = requirementEntry.getValue().get(0);
        topologyTemplate.getSubstitutionMappings().getRequirements().remove(requirementEntry.getKey());

        assertThatThrownBy(() -> VirtualCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(VIRTUAL_CP_MISSED_IN_SUBSTITUTION_MAPPINGS, missedVirtualCpName));
    }
}
