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

import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_POLICIES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.SubstitutionMappings;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

public class SubstitutionMappingsUtilityTest {

    @Test
    public void testCreateSubstitutionMappings() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES));

        Map<String, List<String>> expectedRequirements = new HashMap<>();
        expectedRequirements.put("Search-Engine_macvlan_virtual_link", Arrays.asList("Search-Engine_vdu_cp_macvlan", "virtual_link"));
        expectedRequirements.put("Search-Engine-DB_normal_virtual_link", Arrays.asList("Search-Engine-DB_vdu_cp_normal", "virtual_link"));

        SubstitutionMappings result = SubstitutionMappingsUtility.createSubstitutionMappings(parsed);
        assertThat(result).isNotNull();
        assertThat(result.getNodeType()).isEqualTo("Ericsson.SAMPLE-VNF.1_25_CXS101289_R81E08.cxp9025898_4r81e08");
        assertThat(result.getRequirements()).isEqualTo(expectedRequirements);

    }

    @Test
    public void testCreateEmptySubstitutionMappings() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                VALID_VNFD_WITH_POLICIES));

        SubstitutionMappings result = SubstitutionMappingsUtility.createSubstitutionMappings(parsed);
        assertThat(result).isNull();

    }
}