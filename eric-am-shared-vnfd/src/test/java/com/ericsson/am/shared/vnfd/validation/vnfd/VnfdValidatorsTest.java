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
package com.ericsson.am.shared.vnfd.validation.vnfd;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.VnfdUtility;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class VnfdValidatorsTest {

    @Test
    void testValidateOrThrowShouldThrowExceptionIfWrongMciopNodesAmount() {
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/validation/invalid_vnfd_rel4_with_wrong_mciop_nodes_amount.yaml"));
        assertThatThrownBy(() -> VnfdValidators.validateOrThrow(vnfdObject))
                .isInstanceOf(VnfdValidationException.class)
                .hasMessage("Mciop nodes amount 1 doesn't match exactly helm charts nodes 2");
    }

    @Test
    void testValidateOrThrowShouldThrowExceptionIfMciopArtifactNamesNotMatch() {
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/validation/invalid_vnfd_rel4_mciop_artifact_names_nodes_not_match.yaml"));
        assertThatThrownBy(() -> VnfdValidators.validateOrThrow(vnfdObject))
                .isInstanceOf(VnfdValidationException.class)
                .hasMessage("Mciop artifact names doesn't match to corresponded artifacts names in helm charts");
    }

    @Test
    void testValidateOrThrowShouldNotThrowExceptionIfAllValidationPassed() {
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/validation/sample_vnfd_rel4.yaml"));
        assertDoesNotThrow(() -> VnfdValidators.validateOrThrow(vnfdObject));
    }


}