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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static com.ericsson.am.shared.vnfd.utils.Constants.SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_CP_MISSED_IN_SUBSTITUTION_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_CP_WITH_UNSUPPORTED_VIRTUAL_LINK;
import static com.ericsson.am.shared.vnfd.utils.Constants.VIRTUAL_LINK;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_VIRTUAL_LINK_IN_VDU_CP;
import static com.ericsson.am.shared.vnfd.utils.Constants.VDU_MISSED_IN_HELM_PARAMS_MAPPINGS;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_POLICIES;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.TopologyTemplateUtility;
import com.ericsson.am.shared.vnfd.VnfdUtility;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VduCp;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class VduCpValidatorTest {

    @Test
    public void validateCorrectTopologyTemplate() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        assertDoesNotThrow(() -> VduCpValidator.validate(topologyTemplate));
    }

    @Test
    public void validateTopologyTemplateWithoutVduCpAndSubstitutionMapping() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_WITH_POLICIES)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        assertDoesNotThrow(() -> VduCpValidator.validate(topologyTemplate));
    }

    @Test
    public void validateTopologyTemplateWithVduCpAndEmptySubstitutionMapping() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        topologyTemplate.setSubstitutionMappings(null);
        assertThatThrownBy(() -> VduCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND);
    }

    @Test
    public void validateTopologyTemplateWithVduCpAndEmptySubstitutionMappingRequirements() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        topologyTemplate.getSubstitutionMappings().setRequirements(Collections.emptyMap());

        assertThatThrownBy(() -> VduCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SUBSTITUTION_MAPPINGS_REQUIREMENT_NOT_FOUND);
    }

    @Test
    public void validateTopologyTemplateWithMissedVduCp() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES)
                                                                                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        Map.Entry<String, List<String>> requirementEntry =
                topologyTemplate.getSubstitutionMappings().getRequirements().entrySet().stream().findFirst().get();
        String missedVduCpName = requirementEntry.getValue().get(0);
        topologyTemplate.getSubstitutionMappings().getRequirements().remove(requirementEntry.getKey());

        assertThatThrownBy(() -> VduCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(VDU_CP_MISSED_IN_SUBSTITUTION_MAPPINGS, missedVduCpName));
    }

    @Test
    public void validateTopologyTemplateWithInternalVirtualLinkInVduCp() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_VIRTUAL_LINK_IN_VDU_CP)
                                                                              .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        String vduCpsWithInternalVirtualLink = topologyTemplate.getNodeTemplate().getVduCps().stream()
              .filter(vduCp -> vduCp.getRequirements().containsKey(VIRTUAL_LINK))
              .map(VduCp::getName)
              .collect(Collectors.joining(", "));

        assertThatThrownBy(() -> VduCpValidator.validate(topologyTemplate))
              .isInstanceOf(IllegalArgumentException.class)
              .hasMessage(String.format(VDU_CP_WITH_UNSUPPORTED_VIRTUAL_LINK, vduCpsWithInternalVirtualLink));
    }

    @Test
    public void validateTopologyTemplateWithMissedVduInHelmParamsMapping() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES)
                                                                                .toAbsolutePath());
        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());

        Map.Entry<String, HelmParamsMapping> helmParamsMappingEntry =
                topologyTemplate.getPolicies().getAllHelmParamsMappings().entrySet().stream().findFirst().get();
        String missedVduName = helmParamsMappingEntry.getValue().getVdus().keySet().stream().findFirst().get();
        topologyTemplate.getPolicies().getAllHelmParamsMappings().remove(helmParamsMappingEntry.getKey());

        assertThatThrownBy(() -> VduCpValidator.validate(topologyTemplate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(VDU_MISSED_IN_HELM_PARAMS_MAPPINGS, missedVduName));
    }

}