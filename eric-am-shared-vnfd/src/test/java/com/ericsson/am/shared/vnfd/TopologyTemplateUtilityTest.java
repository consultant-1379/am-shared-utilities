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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static com.ericsson.am.shared.vnfd.utils.Constants.MODIFIABLE_ATTRIBUTES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.MODIFIABLE_ATTRIBUTE_EXTENSION_KEY;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_POLICIES;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.NodeType;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.TopologyTemplate;
import com.ericsson.am.shared.vnfd.model.policies.Policies;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class TopologyTemplateUtilityTest {

    private static final String VALID_VNFD_WITHOUT_MODIFIABLE_ATTRIBUTES = VALID_VNFD_WITH_POLICIES;
    private static final String VALID_VNFD_WITH_MODIFIABLE_ATTRIBUTES_AND_VALID_EXTENSION_ASPECTS
            = VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES;
    private static final String INVALID_VNFD_FILE_WITHOUT_MODIFIABLE_ATTRIBUTES_IN_DATA_TYPES
            = "vnfd/invalid_vnfd_without_vnfinfomodifiableattributes_in_data_types.yaml";
    private static final String INVALID_VNFD_WITH_MODIFIABLE_ATTRIBUTES_AND_INVALID_EXTENSION_ASPECTS
            = "vnfd/invalid_vnfd_with_VnfInfoModifiableAttributesExtension_with_invalid_aspects.yaml";
    private static final String INVALID_VNFD_FOR_TESTING_VALIDATION_MISSING_INPUTS
            = "vnfd/invalid_vnfd_for_testing_validation_missing_inputs_in_topology_template.yaml";

    @Test
    void testValidateVnfInfoCreateTopologyTemplate() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES)
                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        assertThat(topologyTemplate.getInputs()).isNotNull().isNotEmpty().containsKey(MODIFIABLE_ATTRIBUTES);
    }

    @Test
    void testValidateVnfInfoModifiableAttributesPassed() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES)
                .toAbsolutePath());

        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        TopologyTemplateUtility.validateDataType(jsonData, topologyTemplate);
        assertThat(topologyTemplate.getInputs()).isNotNull().isNotEmpty().containsKey(MODIFIABLE_ATTRIBUTES);
    }

    @Test
    void testFailedWithoutModifiableAttributes() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                INVALID_VNFD_FILE_WITHOUT_MODIFIABLE_ATTRIBUTES_IN_DATA_TYPES).toAbsolutePath());
        TopologyTemplate topologyTemplate = TopologyTemplateUtility.createTopologyTemplate(jsonData, new NodeType());
        Executable resultOfValidateDataTypeWithException
                = () -> TopologyTemplateUtility.validateDataType(jsonData, topologyTemplate);
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, resultOfValidateDataTypeWithException);
        assertThat(illegalArgumentException.getMessage()).isEqualTo(
                "tosca.datatypes.nfv.VnfInfoModifiableAttributes not provided in the data_types");
    }

    @Test
    void testValidateModifiableAttributesDefaultsValid() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                VALID_VNFD_WITH_MODIFIABLE_ATTRIBUTES_AND_VALID_EXTENSION_ASPECTS));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, DataType> inputs = new HashMap<>();
        DataType modifiableAttribute = descriptorDetails.getAllDataTypes().get(MODIFIABLE_ATTRIBUTE_EXTENSION_KEY);
        inputs.put(MODIFIABLE_ATTRIBUTES, modifiableAttribute);
        TopologyTemplateUtility.validateModifiableAttributesDefaults(inputs, policies);
    }

    @Test
    void testValidateModifiableAttributesDefaultsInvalidAspects() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                INVALID_VNFD_WITH_MODIFIABLE_ATTRIBUTES_AND_INVALID_EXTENSION_ASPECTS));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, DataType> inputs = new HashMap<>();
        DataType modifiableAttribute = descriptorDetails.getAllDataTypes().get(MODIFIABLE_ATTRIBUTE_EXTENSION_KEY);
        inputs.put(MODIFIABLE_ATTRIBUTES, modifiableAttribute);
        assertThatIllegalArgumentException().isThrownBy(() -> TopologyTemplateUtility
                        .validateModifiableAttributesDefaults(inputs, policies))
                .withMessage("All default aspects in vnfdControlledScaling of extension are not defined in policies");
    }

    @Test
    void testValidateModifiableAttributesDefaultsVNFDWithoutModifiableAttributes() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                VALID_VNFD_WITHOUT_MODIFIABLE_ATTRIBUTES));
        Policies policies = PolicyUtility.createAndValidatePolicies(parsed);
        VnfdUtility.buildVnfDescriptorDetails(parsed);
        Map<String, DataType> inputs = new HashMap<>();
        assertThatCode(() -> TopologyTemplateUtility
                .validateModifiableAttributesDefaults(inputs, policies)).doesNotThrowAnyException();
    }

    @Test
    void testValidateTopologyTemplateForInputs() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                INVALID_VNFD_FOR_TESTING_VALIDATION_MISSING_INPUTS));
        assertThatThrownBy(() -> TopologyTemplateUtility.validateTopologyTemplateForInputs(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inputs must be defined in topology template section of VNFD");
    }
}
