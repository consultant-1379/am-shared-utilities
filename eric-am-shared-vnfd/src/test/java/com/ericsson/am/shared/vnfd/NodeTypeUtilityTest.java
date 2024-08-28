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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.ericsson.am.shared.vnfd.utils.Constants.FULL_RESTORE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_LCM_OPERATIONS_HEAL;
import static com.ericsson.am.shared.vnfd.utils.Constants.LCM_OPERATIONS_MANDATORY;

import jakarta.validation.ConstraintViolationException;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.utils.TestUtils;
import com.ericsson.am.shared.vnfd.utils.VnfdUtils;

class NodeTypeUtilityTest {


    @Test
    void testLcmOperationConfigurationDoesNotHaveTypePropertyHealConfigurationValidationFails() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_WITH_MISSING_TYPE).toAbsolutePath());
        assertThatThrownBy(() -> NodeTypeUtility.validateHealConfiguration(vnfd))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessage("type: type in lcm_operations_configuration is required");
    }

    @Test
    void testLcmOperationConfigurationDoesNotHaveDefaultPropertyHealConfigurationValidationFails() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_MISSING_DEFAULT_CONFIG).toAbsolutePath());
        assertThatThrownBy(() -> NodeTypeUtility.validateHealConfiguration(vnfd))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessage("defaultConfiguration: default configuration for heal is required");
    }

    @Test
    void testLcmOperationConfigurationHasEmptyDefaultPropertyHealConfigurationValidationFails() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_MISSING_HEAL_CONFIG).toAbsolutePath());
        assertThatThrownBy(() -> NodeTypeUtility.validateHealConfiguration(vnfd))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessage("defaultConfiguration: default configuration for heal is required");
    }

    @Test
    void testLcmOperationConfigurationIsMissingHealConfigurationValidationFails() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_WITH_MISSING_LCM_OPERATIONS).toAbsolutePath());
        assertThatThrownBy(() -> NodeTypeUtility.validateHealConfiguration(vnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LCM_OPERATIONS_MANDATORY);
    }

    @Test
    void testHealCausesAreEmptyHealConfigurationValidationFails() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_MISSING_CAUSES).toAbsolutePath());
        assertThatThrownBy(() -> NodeTypeUtility.validateHealConfiguration(vnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_LCM_OPERATIONS_HEAL);
    }

    @Test
    void testLcmOperationConfigurationDoesNotContainNeededHealCauseHealCauseValidationFails() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_WITH_INVALID_CAUSES).toAbsolutePath());
        assertThatThrownBy(() -> NodeTypeUtility.validateHealCauseIsSupported(vnfd, FULL_RESTORE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Causes for Heal must not be empty and must contain Full Restore");
    }

    @Test
    void testHealConfigurationIsValidAndContainsMultipleCausesHealConfigurationAndCauseValidationSucceed() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.INVALID_HEAL_VNFD_WITH_MULTIPLE_CAUSES).toAbsolutePath());
        NodeTypeUtility.validateHealConfiguration(vnfd);
        NodeTypeUtility.validateHealCauseIsSupported(vnfd, FULL_RESTORE);
    }

}