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

import static com.ericsson.am.shared.vnfd.validation.policy.VnfPackageChangeSelectorValidator.NONE_OF_CCVP_OPTIONS_HAS_REQUIRED_FIELDS_SET;
import static com.ericsson.am.shared.vnfd.validation.policy.VnfPackageChangeSelectorValidator.ONLY_ONE_CCVP_OPTION_ALLOWED;
import static com.ericsson.am.shared.vnfd.validation.policy.VnfPackageChangeSelectorValidator.ONLY_ONE_SELECTOR_ALLOWED_FOR_POLICY;
import static com.ericsson.am.shared.vnfd.validation.policy.VnfPackageChangeSelectorValidator.SELECTORS_IN_POLICIES_NOT_UNIQUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.ericsson.am.shared.vnfd.utils.Constants.EMPTY_POLICY_KEY_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_POLICIES;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.ConstraintViolationException;

import com.ericsson.am.shared.vnfd.model.nestedvnfd.Flavour;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.exception.ScalingInfoValidationException;
import com.ericsson.am.shared.vnfd.model.Property;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsMapping;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmParamsVdu;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.Multus;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.policies.InitialDelta;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevels;
import com.ericsson.am.shared.vnfd.model.policies.InstantiationLevelsDataInfo;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDataType;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspectDeltas;
import com.ericsson.am.shared.vnfd.model.policies.ScalingAspects;
import com.ericsson.am.shared.vnfd.model.policies.VduInstantiationLevels;
import com.ericsson.am.shared.vnfd.utils.Constants;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class PolicyUtilityTest {

    @Test
    void testPoliciesWithoutAnyPolicies() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_" +
                                                                                              "vnfd_without_policies_in_policy_section.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageEndingWith(EMPTY_POLICY_KEY_ERROR_MESSAGE);
    }

    @Test
    void testPoliciesWithoutScaleOutPolicies() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/valid_" +
                                                                                              "vnfd_without_scaling_policy_and_with_rollback_policy"
                                                                                              + ".yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllVnfPackageChangePolicy()).isNotEmpty();
        assertThat(policies.getAllInitialDelta()).isEmpty();
        assertThat(policies.getAllScalingAspects()).isEmpty();
        assertThat(policies.getAllScalingAspectDelta()).isEmpty();
    }

    @Test
    void testSkippedScalingPolicies() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/valid_vnfd_no_scale_info.yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllScalingAspects()).isEmpty();
        assertThat(policies.getAllScalingAspectDelta()).isEmpty();
    }

    @Test
    void testPoliciesWithMixedScaleInfoPresence() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/valid_vnfd_with_scale_info_and_without_it.yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllScalingAspects()).isNotEmpty();
        assertThat(policies.getAllScalingAspectDelta()).isNotEmpty();
        assertThat(policies.getAllScalingAspects()).hasSize(1);
        assertThat(policies.getAllScalingAspectDelta()).hasSize(4);
    }

    @Test
    void testPoliciesWithoutComponentMappingInScalingPolicy() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/valid_vnfd_" +
                                                                                              "without_component_mapping_in_rollback_policy.yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllVnfPackageChangePolicy()).isNotEmpty();
        assertThat(policies.getAllInitialDelta()).isEmpty();
        assertThat(policies.getAllScalingAspects()).isEmpty();
        assertThat(policies.getAllScalingAspectDelta()).isEmpty();
    }

    @Test
    void testPoliciesInVnfd() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_WITH_POLICIES));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        Map<String, ScalingAspects> allScalingAspects = policies.getAllScalingAspects();
        assertThat(allScalingAspects).isNotNull().isNotEmpty();
        for (String scalingAspectKey : allScalingAspects.keySet()) {
            Map<String, ScalingAspectDataType> allAspects = allScalingAspects.get(scalingAspectKey).getProperties().getAllAspects();
            assertThat(allAspects).isNotNull().isNotEmpty();
            for (String aspectDataTypeKey : allAspects.keySet()) {
                Map<String, ScalingAspectDeltas> allScalingAspectDelta = allAspects.get(aspectDataTypeKey).getAllScalingAspectDelta();
                assertThat(allScalingAspectDelta).isNotNull().isNotEmpty();
                for (String scalingAspectDeltaKey : allScalingAspectDelta.keySet()) {
                    Map<String, InitialDelta> allInitialDelta = allScalingAspectDelta.get(scalingAspectDeltaKey).getAllInitialDelta();
                    assertThat(allInitialDelta).isNotNull().isNotEmpty();
                    assertThat(allInitialDelta).hasSize(2);
                }
            }
        }
        Map<String, ScalingAspectDeltas> allScalingAspectDelta = policies.getAllScalingAspectDelta();
        assertThat(allScalingAspectDelta).isNotNull().isNotEmpty();
        for (String allScalingAspectDeltaKey : allScalingAspectDelta.keySet()) {
            Map<String, InitialDelta> allInitialDelta = allScalingAspectDelta.get(allScalingAspectDeltaKey).getAllInitialDelta();
            assertThat(allInitialDelta).isNotNull().isNotEmpty();
            assertThat(allInitialDelta).hasSize(2);
        }
        Map<String, InitialDelta> allInitialDelta = policies.getAllInitialDelta();
        assertThat(allInitialDelta).hasSize(2);
    }

    @Test
    void testPoliciesInVnfdWithMultipleAspectDeltaForOneAspect() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/valid_" +
                                                                                              "vnfd_with_multiple_aspect_delta_for_a_aspect.yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
    }

    @Test
    void testPoliciesInvalidVnfdWithMultipleAspectDeltaForOneAspect() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid" +
                                                                                              "_vnfd_with_multiple_aspect_delta_for_a_aspect.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("properties.deltas: deltas are mandatory can't be null");
    }

    @Test
    void testPoliciesInVnfdWithNegativeMaxScaleLevel() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/vnfd_with_negative_max_scale_level.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("max scale level can be only positive");
    }

    @Test
    void testPoliciesInVnfdWithNegativeScalingDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_negative_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("number of instance can be only positive");
    }

    @Test
    void testPoliciesInVnfdWithNegativeInitialDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_negative_initial_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("number of instance can be only positive");
    }

    @Test
    void testPoliciesInVnfdWithNoInitialDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_no_initial_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageEndingWith(Constants.INITIAL_DELTA_NOT_PRESENT_IN_VNFD);
    }

    @Test
    void testPoliciesInVnfdWithNoScalingAspect() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_no_scaling_aspect.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageEndingWith(Constants.SCALING_ASPECT_NOT_PRESENT_IN_VNFD);
    }

    @Test
    void testPoliciesInVnfdWithNoAspectDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_no_aspect_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageEndingWith(Constants.ASPECT_DELTA_NOT_PRESENT_IN_VNFD);
    }

    @Test
    void testValidateMissingVduInInitialDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_" +
                                                                                              "vnfd_with_missing_vdu_in_initial_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageMatching(Constants.MISSING_VDU);
    }

    @Test
    void testValidateDuplicateVduInInitialDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_" +
                                                                                              "vnfd_with_duplicate_vdu_in_initial_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageMatching(Constants.DUPLICATE_VDU_IN_INITIAL_DELTA);
    }

    @Test
    void testValidateMissingVduScalingAspectDelta() {
        JSONObject parsed = VnfdUtility
                .validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_missing_vdu_" +
                                                                       "scaling_aspect_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageMatching(String.format(Constants.VDU_SCALING_DELTA_NOT_DEFINED, "Payload_2"));
    }

    @Test
    void testValidateMissingScalingDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_" +
                                                                                              "vnfd_with_missing_scaling_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageMatching(String.format(Constants.SCALING_DELTA_NOT_DEFINED, "delta_2", "Payload",
                                                  "Payload_ScalingAspectDeltas"));
    }

    @Test
    void testValidateMissingPropertiesInScalingAspect() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_" +
                                                                                              "with_no_properties_in_scaling_aspect.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("ScalingAspect Properties can't be null");
    }

    @Test
    void testValidateMissingPropertiesInScalingDelta() {
        JSONObject parsed = VnfdUtility
                .validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_missing_properties_in_scaling_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("properties can't be null for scaling aspect deltas");
    }

    @Test
    void testValidateMissingPropertiesInInitialDelta() {
        JSONObject parsed = VnfdUtility
                .validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_vnfd_with_missing_properties_in_initial_delta.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("properties can't be null for initial deltas");
    }

    @Test
    void testPoliciesInVnfdWithRollbackPolicies() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies.yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllVnfPackageChangePolicy()).hasSize(1);
        assertThat(policies.getAllInitialDelta()).hasSize(2);

        assertThat(policies.getAllScalingAspectDelta()).hasSize(2);
        assertThat(policies.getAllScalingAspects()).hasSize(1);
    }

    @Test
    void testPoliciesInVnfdWithInvalidValueInModificationQualifier() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_with_invalid_value_in_modification_qualifier.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessage("properties.modificationQualifier: modification_qualifier only " +
                                    "supports 'up' and 'down' value");
    }

    @Test
    void testPoliciesInVnfdWithInvalidValueInComponentTypeOfComponentMapping() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_with_invalid_value_in_component_type.yaml"));
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessage("properties.componentMappings[1].componentType: component_type " +
                                    "only support's vdu, cp, virtual_link, virtual_storage, deployment_flavour, " +
                                    "instantiation_level and scaling_aspect value");
    }

    @Test
    void testPoliciesInVnfdWithMissingRequiredFieldsInVnfPackageChangeSelector() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_with_missing_fields_in_change_policy_selector.yaml"));
        assertThatThrownBy(() -> PolicyUtility.validateVnfPackageChangePoliciesSelectorsAndPatterns(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format(NONE_OF_CCVP_OPTIONS_HAS_REQUIRED_FIELDS_SET, "change_from_version_1"))
                .hasMessageContaining(String.format(NONE_OF_CCVP_OPTIONS_HAS_REQUIRED_FIELDS_SET, "change_to_version_1"));
    }

    @Test
    void testPoliciesInVnfdWithBothCcvpOptionsInVnfPackageChangeSelector() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_with_both_ccvp_options_in_change_policy_selector.yaml"));
        assertThatThrownBy(() -> PolicyUtility.validateVnfPackageChangePoliciesSelectorsAndPatterns(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format(ONLY_ONE_CCVP_OPTION_ALLOWED, "change_from_version_1"));
    }

    @Test
    void testPoliciesInVnfdWithMultipleNotUniqueSelectors() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_with_multiple_non_unique_selectors_in_change_policies.yaml"));
        assertThatThrownBy(() -> PolicyUtility.validateVnfPackageChangePoliciesSelectorsAndPatterns(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format(ONLY_ONE_SELECTOR_ALLOWED_FOR_POLICY, "change_from_version_1"))
                .hasMessageContaining(SELECTORS_IN_POLICIES_NOT_UNIQUE);
    }

    @Test
    void testGetDowngradeAdditionalParameters() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_instantiate_helm_package_as_json_object.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "ebc68e34-0cfa-40ba-8b45-9caa31f9dcb5",
                "b1bb0ce7-ebca-4fa7-95ed-4840d70a1177", null, null, descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).hasSize(2).containsKeys("server.service.clusterIP", "data_conversion_identifier");
    }

    @Test
    void testGetDowngradeAdditionalParametersWithRegex() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_tosca-rel4-up_with_regex.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "ebc68e34-0cfa-40ba-8b45-9caa31f9dcb5",
                "b1bb0ce7-ebca-4fa7-95ed-4840d70a1177", "1.0.1s", "1.1.1s", descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).isNotEmpty();
    }

    @Test
    void testGetDowngradeAdditionalParametersTosca1dot3() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_multi_b.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "multi-chart-569d-xyz3-5g15f7h500",
                "multi-chart-477c-aab3-2b04e6a383", null, null, descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).hasSize(1).containsKeys("data_conversion_identifier");
    }

    @Test
    void testGetDowngradeAdditionalParametersWithDestinationIdNotPresent() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_instantiate_helm_package_as_json_object.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "ebc68e34-0cfa-40ba-8b45-9caa31f9dcb5",
                "descriptor_id_not_present", null, null, descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).isNull();
    }

    @Test
    void testGetDowngradeAdditionalParametersWithSourceIdNotPresent() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_instantiate_helm_package_as_json_object.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "descriptor_id_not_present",
                "b1bb0ce7-ebca-4fa7-95ed-4840d70a1177", null, null, descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).isNull();
    }

    @Test
    void testGetDowngradeAdditionalParametersWithNullTopologyTemplate() {
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "descriptor_id_not_present",
                "b1bb0ce7-ebca-4fa7-95ed-4840d70a1177", null, null, null);
        assertThat(allProperty).isNull();
    }

    @Test
    void testGetDowngradeAdditionalParametersWithWildcardDestinationId() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_wildcard_destination_id_for_rollback_patterns.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "multi-chart-569d-xyz3-5g15f7h499",
                "any_destination_id", null, null, descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).hasSize(1).containsKeys("tags.all");
    }

    @Test
    void testGetSpecificDowngradeAdditionalParametersWhenWildcardDestinationExist() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_wildcard_destination_id_for_rollback_patterns.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, Property> allProperty = PolicyUtility.getDowngradeAdditionalParameters(
                "multi-chart-569d-xyz3-5g15f7h499",
                "multi-chart-477c-aab3-2b04e6a383", null, null, descriptorDetails.getDefaultFlavour()
                        .getTopologyTemplate());
        assertThat(allProperty).hasSize(1).containsKeys("tags.all");
    }

    @Test
    void testGetActionFromOperation() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        String action = PolicyUtility.getActionFromOperation(
                "2ce9484e-85e5-49b7-ac97-445379754e37", "36ff67a9-0de4-48f9-97a3-4b0661670933",
                descriptorDetails.getDefaultFlavour().getTopologyTemplate(), "EricssonChangeCurrentVnfPackage");
        assertThat(action).isEqualTo("EricssonChangeCurrentVnfPackage.rollback_from_package6_to_package4");
    }

    @Test
    void testGetTriggersAction() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        List<String> action = PolicyUtility.getTriggersAction(descriptorDetails.getDefaultFlavour().getTopologyTemplate(),
                                                              "2ce9484e-85e5-49b7-ac97-445379754e37", "36ff67a9-0de4-48f9-97a3-4b0661670933");
        assertThat(action).contains("EricssonChangeCurrentVnfPackage.rollback_from_package6_to_package4");
    }

    @Test
    void testGetTriggersActionWithEmptyTopologyTemplate() {
        String sourceDescriptorId = "testSourceDescriptorId";
        String destinationDescriptorId = "testDestinationDescriptorId";

        List<String> actual = PolicyUtility.getTriggersAction(null, sourceDescriptorId, destinationDescriptorId);

        assertThat(actual).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetTriggersActionWithEmptySourceDescriptorId() {
        String destinationDescriptorId = "testDestinationDescriptorId";

        assertThatThrownBy(() -> PolicyUtility.getTriggersAction(new TopologyTemplate(), null, destinationDescriptorId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("source id can't be null");

    }

    @Test
    void testGetTriggersActionWithEmptyDestinationDescriptorId() {
        String sourceDescriptorId = "testSourceDescriptorId";

        assertThatThrownBy(() -> PolicyUtility.getTriggersAction(new TopologyTemplate(), sourceDescriptorId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target id can't be null");

    }

    @Test
    void testPoliciesInVnfdWithVduInstantiationLevels() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_with_vdu_instantiation_levels.yaml")
        );
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();

        Map<String, VduInstantiationLevels> allVduInstantiationLevel = policies.getAllVduInstantiationLevels();
        assertThat(allVduInstantiationLevel).isNotNull().isNotEmpty();

        VduInstantiationLevels dbInstantiationLevels = allVduInstantiationLevel.get("db_instantiation_levels");
        assertThat(dbInstantiationLevels).isNotNull();
        assertThat(dbInstantiationLevels.getProperties()).isNotNull();
        assertThat(dbInstantiationLevels.getType()).isNotBlank();
        assertThat(dbInstantiationLevels.getTargets()).isNotNull().isNotEmpty();
        assertThat(dbInstantiationLevels.getProperties().getInstantiationLevels()).isNotNull().isNotEmpty();
    }

    @Test
    void testPoliciesInVnfdWithVduInstantiationLevelsNegativeInstanceNumber() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_vdu_instantiation_levels_negative_number_instance.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).
                isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("number of instance can be only positive");
    }

    @Test
    void testPoliciesInVnfdWithVduInstantiationLevelsWithoutLevels() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_vdu_instantiation_levels_without_levels.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).
                isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("vdu levels can't be null");
    }

    @Test
    void testPoliciesInVnfdWithVduInstantiationLevelsWithoutProperties() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_vdu_instantiation_levels_without_properties.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).
                isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("properties can't be null for vdu instantiation levels");
    }

    @Test
    void testPoliciesInVnfdWithVduInstantiationLevelsZeroTargets() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_vdu_instantiation_levels_zero_targets.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).
                isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("at least one target must be present");
    }

    @Test
    void testAllEvnfmSupportedPolicies() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_with_all_evnfm_supported_policies.yaml")
        );

        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();

        assertThat(policies.getAllVduInstantiationLevels()).hasSize(1);
        assertThat(policies.getAllInstantiationLevels()).hasSize(1);
        assertThat(policies.getAllVnfPackageChangePolicy()).hasSize(1);
        assertThat(policies.getAllScalingAspects()).hasSize(1);
        assertThat(policies.getAllScalingAspectDelta()).hasSize(3);
        assertThat(policies.getAllInitialDelta()).hasSize(2);
    }

    @Test
    void testAllEvnfmSupportedPoliciesCustomToscaType() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_with_all_evnfm_supported_policies_custom_tosca_type.yaml")
        );

        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();

        assertThat(policies.getAllVduInstantiationLevels()).hasSize(1);
        assertThat(policies.getAllInstantiationLevels()).hasSize(1);
        assertThat(policies.getAllVnfPackageChangePolicy()).hasSize(2);
        assertThat(policies.getAllScalingAspects()).hasSize(1);
        assertThat(policies.getAllScalingAspectDelta()).hasSize(3);
        assertThat(policies.getAllInitialDelta()).hasSize(2);
    }

    @Test
    void testPoliciesInVnfdWithInstantiationLevels() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_with_instantiation_levels.yaml")
        );

        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();

        Map<String, InstantiationLevels> allInstantiationLevel = policies.getAllInstantiationLevels();
        assertThat(allInstantiationLevel).isNotNull().isNotEmpty();

        InstantiationLevels instantiationLevels = allInstantiationLevel.get("instantiation_levels");
        assertThat(instantiationLevels).isNotNull();
        assertThat(instantiationLevels.getType()).isNotBlank();
        assertThat(instantiationLevels.getProperties()).isNotNull();
        assertThat(instantiationLevels.getProperties().getInstantiationLevelsDataInfo()).isNotNull().isNotEmpty();

        InstantiationLevelsDataInfo instantiationLevelFirst =
                instantiationLevels.getProperties().getInstantiationLevelsDataInfo().get("instantiation_level_1");
        assertThat(instantiationLevelFirst).isNotNull();
        assertThat(instantiationLevelFirst.getScaleInfo()).isNotNull().isNotEmpty();

        InstantiationLevelsDataInfo instantiationLeveSecond =
                instantiationLevels.getProperties().getInstantiationLevelsDataInfo().get("instantiation_level_2");
        assertThat(instantiationLeveSecond).isNotNull();
        assertThat(instantiationLeveSecond.getScaleInfo()).isNotNull().isNotEmpty();
    }

    @Test
    void testValidationWillErrorMultipleInstantiationLevelsNoDefault() {
        final JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/vnfd_with_invalid_instantiation_level_no_default.yaml").toAbsolutePath());
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed)).
                isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Default level must be defined if multiple Instantiation levels are present in Policies");
    }

    @Test
    void testWillSetDefaultForSingleInstantiationLevel() {
        final JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/vnfd_with_single_instantiation_level_no_default.yaml").toAbsolutePath());
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies.getAllInstantiationLevels()).hasSize(1);
        String actualDefault = policies.getAllInstantiationLevels().entrySet().iterator().next().getValue().getProperties().getDefaultLevel();
        assertThat(actualDefault).isEqualTo("instantiation_level_1");
    }

    @Test
    void testValidationTosca1Dot3() {
        final JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/vnfd_tosca_1_3_multi_b.yaml").toAbsolutePath());
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllInitialDelta()).isNotNull().hasSize(6);
        assertThat(policies.getAllScalingAspectDelta()).isNotNull().hasSize(3);
        assertThat(policies.getAllScalingAspects()).isNotNull().hasSize(1);
        assertThat(policies.getAllVnfPackageChangePolicy()).isNotNull().hasSize(1);
        assertThat(policies.getAllVduInstantiationLevels()).isNotNull().hasSize(1);
        assertThat(policies.getAllInstantiationLevels()).isNotNull().hasSize(1);
    }

    @Test
    void testValidationTosca1Dot3Sol001Rel4() {
        final JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/vnfd_tosca_1_3_multi_b_rel4.yaml").toAbsolutePath());
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        assertThat(policies.getAllInitialDelta()).isNotNull().hasSize(6);
        assertThat(policies.getAllScalingAspectDelta()).isNotNull().hasSize(4);
        assertThat(policies.getAllScalingAspects()).isNotNull().hasSize(1);
        assertThat(policies.getAllVnfPackageChangePolicy()).isNotNull().hasSize(2);
        assertThat(policies.getAllVduInstantiationLevels()).isNotNull().hasSize(1);
        assertThat(policies.getAllInstantiationLevels()).isNotNull().hasSize(1);
    }

    @Test
    void testValidationTosca1Dot3Fail() {
        final JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/vnfd_tosca_1_3_multi_b_wrong_policies.yaml").toAbsolutePath());
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageEndingWith("VduScalingDelta not defined for aspect Aspect4");
    }

    @Test
    void testPoliciesInVnfdWithInstantiationLevelsWithoutLevels() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_instantiation_levels_without_levels.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("instantiation levels can't be null");
    }

    @Test
    void testPoliciesInVnfdWithInstantiationLevelsWithoutProperties() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_instantiation_levels_without_properties.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("properties can't be null for instantiation levels");
    }

    @Test
    void testPoliciesInVnfdWithInstantiationLevelsWithoutScaleInfo() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_instantiation_levels_without_scale_info.yaml")
        );

        assertThat(parsed).isNotNull();
    }

    @Test
    void testPoliciesInVnfdWithInstantiationLevelsWithoutDescription() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_instantiation_levels_without_description.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageEndingWith("instantiation levels description must not be blank");
    }

    @Test
    void testPoliciesInVnfdWithInstantiationLevelsAllScalingAspectsInInstantiationLevels() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_instantiation_levels_not_all_scaling_aspects.yaml")
        );
        assertThatThrownBy(() -> PolicyUtility.createAndValidatePolicies(parsed))
                .isInstanceOf(ScalingInfoValidationException.class)
                .hasMessageEndingWith(Constants.SCALING_ASPECTS_NOT_PRESENT_IN_INSTANTIATION_LEVELS);
    }

    @Test
    void testPoliciesInVnfdWithTargetsHavingOnlyInitialDelta() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/vnfd_with_policies_only_initial_delta.yaml"));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        assertThat(policies).isNotNull();
        Map<String, ScalingAspects> allScalingAspects = policies.getAllScalingAspects();
        assertThat(allScalingAspects).isNotNull().isNotEmpty();
        for (String scalingAspectKey : allScalingAspects.keySet()) {
            Map<String, ScalingAspectDataType> allAspects = allScalingAspects.get(scalingAspectKey).getProperties().getAllAspects();
            assertThat(allAspects).isNotNull().isNotEmpty();
            for (String aspectDataTypeKey : allAspects.keySet()) {
                Map<String, ScalingAspectDeltas> allScalingAspectDelta = allAspects.get(aspectDataTypeKey).getAllScalingAspectDelta();
                assertThat(allScalingAspectDelta).isNotNull().isNotEmpty();
                for (String scalingAspectDeltaKey : allScalingAspectDelta.keySet()) {
                    Map<String, InitialDelta> allInitialDelta = allScalingAspectDelta.get(scalingAspectDeltaKey).getAllInitialDelta();
                    assertThat(allInitialDelta).isNotNull().isNotEmpty();
                    assertThat(allInitialDelta).hasSize(1);
                }
            }
        }
        Map<String, ScalingAspectDeltas> allScalingAspectDelta = policies.getAllScalingAspectDelta();
        assertThat(allScalingAspectDelta).isNotEmpty();
        for (String allScalingAspectDeltaKey : allScalingAspectDelta.keySet()) {
            Map<String, InitialDelta> allInitialDelta = allScalingAspectDelta.get(allScalingAspectDeltaKey).getAllInitialDelta();
            assertThat(allInitialDelta).hasSize(1);
        }
        Map<String, InitialDelta> allInitialDelta = policies.getAllInitialDelta();
        assertThat(allInitialDelta).hasSize(2);
    }

    @Test
    public void testPoliciesInVnfdWithHelmParamsMapping() {
        Map<String, HelmParamsMapping> expectedHelmParamsMapping = buildHelmParamsMapping();
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(SAMPLE_VNFD_WITH_NETWORK_DATA_TYPES));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);

        assertThat(policies).isNotNull();
        Map<String, HelmParamsMapping> helmParamsMappingMap = policies.getAllHelmParamsMappings();
        assertThat(helmParamsMappingMap).isNotNull().isNotEmpty();
        assertThat(helmParamsMappingMap).isEqualTo(expectedHelmParamsMapping);

    }

    private Map<String, HelmParamsMapping> buildHelmParamsMapping() {
        Map<String, HelmParamsMapping> helmParamsMappingMap = new HashMap<>();

        HelmParamsMapping helmPackage1 = new HelmParamsMapping();
        Map<String, HelmParamsVdu> helmParamsVduMap1 = new HashMap<>();
        helmParamsVduMap1.put("Search-Engine", new HelmParamsVdu(
              new Multus("string",
                         "Search-Engine",
                         "networks",
                         null,
                         null,
                         null,
                         List.of("networkAttachmentDefinition=true", "type=nil"))));
        helmPackage1.setVdus(helmParamsVduMap1);

        HelmParamsMapping helmPackage2 = new HelmParamsMapping();
        Map<String, HelmParamsVdu> helmParamsVduMap2 = new HashMap<>();
        helmParamsVduMap2.put("Search-Engine-DB",  new HelmParamsVdu(
              new Multus("string",
                         "Search-Engine-DB",
                         "networks",
                         null,
                         null,
                         null,
                         null)));
        helmPackage2.setVdus(helmParamsVduMap2);

        helmParamsMappingMap.put("helm_package1", helmPackage1);
        helmParamsMappingMap.put("helm_package2", helmPackage2);

        return helmParamsMappingMap;
    }

    @Test
    void shouldNotValidateMissingScalingDeltaWithDisabledValidation() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource("vnfd/invalid_" +
                "vnfd_with_missing_scaling_delta.yaml"));
        Policies policies = PolicyUtility.createPolicies(parsed);
        assertThat(policies).isNotNull();
    }

    private TopologyTemplate buildTopologyTemplateFromVnfd(JSONObject vnfd) {
        NodeType nodeType = NodeTypeUtility.buildNodeType(vnfd);
        Flavour defaultFlavour = FlavourUtility.getDefaultFlavour(vnfd, nodeType);
        return defaultFlavour.getTopologyTemplate();
    }
}
