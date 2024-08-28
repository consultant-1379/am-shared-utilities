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

import com.ericsson.am.shared.vnfd.model.CustomInterfaceInputs;
import com.ericsson.am.shared.vnfd.model.CustomOperation;
import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.EntrySchema;
import com.ericsson.am.shared.vnfd.model.HelmChart;
import com.ericsson.am.shared.vnfd.model.HelmChartType;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.model.InterfaceTypeImpl;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.Property;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.am.shared.vnfd.model.lcmoperation.LCMOperationsEnum;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.HelmPackage;
import com.ericsson.am.shared.vnfd.model.nestedvnfd.VnfmLcmInterface;
import com.ericsson.am.shared.vnfd.utils.Constants;
import com.ericsson.am.shared.vnfd.utils.TestUtils;
import com.ericsson.am.shared.vnfd.utils.VnfdUtils;
import com.ericsson.am.shared.vnfd.validation.vnfd.DeployableModuleValidator;
import com.ericsson.am.shared.vnfd.validation.vnfd.VnfdValidationException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.am.shared.vnfd.VnfdUtility.buildVnfDescriptorDetails;
import static com.ericsson.am.shared.vnfd.VnfdUtility.getAllHelmPackagesFromLcmOperation;
import static com.ericsson.am.shared.vnfd.VnfdUtility.getInterfaceFromNodeTemplate;
import static com.ericsson.am.shared.vnfd.VnfdUtility.getVnflcmInterfaceAsJsonObject;
import static com.ericsson.am.shared.vnfd.VnfdUtility.getVnflcmInterfaces;
import static com.ericsson.am.shared.vnfd.VnfdUtility.isNestedVnfd;
import static com.ericsson.am.shared.vnfd.model.HelmChartType.CNF;
import static com.ericsson.am.shared.vnfd.model.HelmChartType.CRD;
import static com.ericsson.am.shared.vnfd.utils.Constants.ADDITIONAL_PARAMETERS_NOT_PROVIDED_FOR_INPUTS_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACTS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.ARTIFACT_KEYS;
import static com.ericsson.am.shared.vnfd.utils.Constants.ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.CNF_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEPLOYABLE_MODULES_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_NOT_DEFINED_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.DERIVED_FROM_VALUE_NOT_SUPPORTED;
import static com.ericsson.am.shared.vnfd.utils.Constants.DESTINATION_DESCRIPTOR_ID_MUST_NOT_BE_EMPTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.DOCKER_IMAGES_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.ENTRY_SCHEMA_CONSTRAINT_CANT_BE_BLANK_FOR_EXTENSIONS_PROPERTIES;
import static com.ericsson.am.shared.vnfd.utils.Constants.ENTRY_SCHEMA_NULL_FOR_EXTENSIONS_PROPERTIES;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSIONS_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSION_CANT_BE_NULL;
import static com.ericsson.am.shared.vnfd.utils.Constants.EXTENSION_PROPERTIES_INVALID_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.HELM_CHARTS_NOT_PRESENT;
import static com.ericsson.am.shared.vnfd.utils.Constants.INSTANTIATE_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INTERFACES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_DEPLOYABLE_MODULES_ENTRY_SCHEMA_CONSTRAINTS_VALUE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_ENTRY_SCHEMA_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_EXTENSION_DATA_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_INPUT_PROVIDED_FOR_INPUTS;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_NODE_DETAILS_PROVIDED;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_PROPERTY_KEY_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_VALUE_FOR_ASPECT_IN_VNF_CONTROLLED_SCALING_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_VALUE_FOR_KEY_IN_DEPLOYABLE_MODULES_DEFAULTS_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.INVALID_VNF_CONTROLLED_SCALING_ENTRY_SCHEMA_CONSTRAINTS_VALUE;
import static com.ericsson.am.shared.vnfd.utils.Constants.MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_PROPERTY_MISSING_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_CANT_BE_NULL_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_IN_DEPLOYABLE_MODULE_ARE_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_NULL_FOR_DATATYPE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTY_NOT_FOUND_IN_VNFD;
import static com.ericsson.am.shared.vnfd.utils.Constants.ROLLBACK;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNKNOWN_OPERATION;
import static com.ericsson.am.shared.vnfd.utils.Constants.UNSUPPORTED_OPERATION_TYPE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULES_FOR_ASSOCIATED_ARTIFACTS_DUPLICATES_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULES_FOR_DUPLICATES_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULES_FOR_NAMES_DUPLICATES_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS_HAS_FAILED_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_DEPLOYABLE_MODULE_RELATION_HAS_FAILED_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_EMPTY_DEPLOYABLE_MODULE_NAME_HAS_FAILED;
import static com.ericsson.am.shared.vnfd.utils.Constants.VALIDATION_OF_OPTIONALITY_OF_DEPLOYABLE_MODULE_HAS_FAILED;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_NOT_MAP_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFD_NOT_PRESENT_IN_THE_PATH_SPECIFIED;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_DEFAULT_VALUE_BLANK_FOR_ASPECT_FORMAT;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_DEFAULT_VALUE_MISSING;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_DEFAULT_VALUE_NOT_MAP;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_PROPERTY;
import static com.ericsson.am.shared.vnfd.utils.Constants.VNF_CONTROLLED_SCALING_PROPERTY_MISSING;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.PARENT_VNFD_YAML;
import static com.ericsson.am.shared.vnfd.utils.TestConstants.VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES;
import static com.ericsson.am.shared.vnfd.utils.TestUtils.getResource;
import static com.ericsson.am.shared.vnfd.utils.VnfdUtils.*;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnfdUtilityTest {

    private static final String INVALID_VNFD_FILE_SINGLE_CRD = "vnfd/invalid_vnfd_with_single_crd_chart.yaml";
    private static final String VALID_VNFD_FILE_HEAL = "vnfd/valid_vnfd_heal.yaml";
    private static final String INVALID_VNFD_FILE_FORMAT = "vnfd/invalid_vnfd_file_format.yaml";
    private static final String EMPTY_VNFD_FILE = "vnfd/empty_vnfd_file.yaml";
    private static final String NON_EXISTING_VNFD_FILE = "vnfd/non_existing_vnfd_file.yaml";
    private static final String INVALID_FILE_WITHOUT_HELMC_HARTS = "vnfd/invalid_vnfd_file_without_helm_Charts.yaml";
    private static final String INVALID_FILE_WITHOUT_IMAGES
            = "vnfd/invalid_vnfd_file_without_images.yaml";
    private static final String INVALID_FILE_WITHOUT_IMAGE_FILE_DETAILS
            = "vnfd/invalid_vnfd_without_image_file_provided.yaml";
    private static final String INVALID_VNFD_WITH_ADDITIONAL_PROPERTIES_NOT_PROVIDED
            = "vnfd/invalid_vnfd_with_additional_properties_not_provided.yaml";
    private static final String INVALID_VNFD_WITH_INVALID_INTERFACE_INPUT
            = "vnfd/invalid_vnfd_with_invalid_interface_input.yaml";
    private static final String INVALID_VNFD_WITH_TYPE_NOT_PROVIDED_FOR_ADDITIONAL_PROPERTIES
            = "vnfd/invalid_vnfd_with_type_not_provided_for_additional_properites.yaml";
    private static final String INVALID_VNFD_WITH_DATA_TYPE_NOT_DEFINED
            = "vnfd/invalid_vnfd_with_data_type_not_defined.yaml";
    private static final String VALID_VNFD_REL4 = "vnfd/valid_vnfd_rel4_with_scale_mapping.yaml";
    public static final String INVALID_VNFD_REL4 = "vnfd/invalid_vnfd_rel4_with_incorrect_aspects.yaml";
    public static final String VALID_VNFD_NO_REL4 = "vnfd/valid_vnfd_with_correct_aspects.yaml";
    public static final String INVALID_VNFD_NO_REL4  = "vnfd/invalid_vnfd_with_incorrect_aspects.yaml";
    public static final String VALID_VNFD_REL4_MIXED_VDU  = "vnfd/valid_vnfd_rel4_mixed_vdu.yaml";
    private static final String INVALID_VNFD_WITHOUT_NODE_TYPE = "vnfd/invalid_vnfd_without_node_type.yaml";
    private static final String INVALID_VNFD_WITH_TWO_NODE_TYPE = "vnfd/invalid_vnfd_with_2_node_type.yaml";
    private static final String INVALID_VNFD_WITHOUT_NODE_PROPERTIES = "vnfd/invalid_vnfd_without_node_properties.yaml";
    private static final String INVALID_VNFD_WITHOUT_NODE_TYPE_DETAILS = "vnfd/invalid_vnfd_without_node_type_details.yaml";
    private static final String INVALID_VNFD_WITHOUT_VNFD_ID = "vnfd/invalid_vnfd_without_vnfdid.yaml";
    private static final String VALID_VNFD_FILE_WITH_EMPTY_INTERFACE_DEF
            = "vnfd/valid_vnfd_with_blank_usecase_definition.yaml";
    private static final String VALID_VNFD_WITH_MULTIPLE_HELM_CHARTS
            = "vnfd/valid_vnfd_with_multiple_helm_charts.yaml";
    private static final String VALID_VNFD_REL4_WITH_MULTIPLE_MCIOP_HELM_CHARTS
            = "vnfd/valid_vnfd_rel4_with_multiple_mciop_helm_charts.yaml";
    private static final String VALID_VNFD_REL4_WITH_DUPLICATED_MCIOP_HELM_CHARTS
            = "vnfd/valid_vnfd_rel4_with_duplicated_mciop_helm_charts.yaml";
    private static final String VALID_VNFD_REL4_WITH_ALL_DUPLICATED_MCIOP_HELM_CHARTS
            = "vnfd/valid_vnfd_rel4_with_all_duplicated_mciop_helm_charts.yaml";
    private static final String INVALID_VNFD_WITH_MULTIPLE_CRD_ONLY_HELM_CHARTS
            = "vnfd/invalid_vnfd_with_multiple_crd_only_charts.yaml";
    private static final String MULTIPLE_HELM_CHARTS_IN_NODE_TEMPLATE_YAML
            = "vnfd/valid_vnfd_multiple_helm_charts_in_node_template.yaml";
    private static final String MULTIPLE_HELM_CHARTS_IN_NODE_TYPE_WITH_TOPOLOGY_TEMPLATE
            = "vnfd/valid_vnfd_multiple_helm_charts_in_note_type_with_topology_template.yaml";
    private static final String MULTIPLE_HELM_CHARTS_IN_NODE_TEMPLATE_AND_NODE_TYPE
            = "vnfd/valid_vnfd_multiple_helm_charts_in_node_template_and_node_type.yaml";
    private static final String VALID_VNFD_HEAL_WITH_MULTI_HELM = "vnfd/valid_vnfd_multi_heal.yaml";
    private static final String VALID_VNFD_WITH_NULL_VALUES = "vnfd/valid_vfnd_with_map_null_values.yaml";
    private static final String INVALID_VNFD_WITH_INVALID_EXTENSIONS
            = "vnfd/invalid_vnfd_with_invalid_entry_schema_definition_for_extensions.yaml";
    private static final String VALID_VNFD_WITH_NULL_HELM_PACKAGE = "vnfd/valid_vnfd_with_helm_packages_is_null.yaml";
    private static final String INVALID_VNFD_WITH_NULL_HELM_PACKAGE_CRD
            = "vnfd/invalid_vnfd_with_helm_packages_is_null_crd.yaml";
    private static final String INVALID_VNFD_NODE_TYPES_VIOLATES_CONSTRAINT = "vnfd/invalid_vnfd_node_type_violates_constraint.yaml";
    private static final String VALID_VNFD_WITH_OLD_STRUCTURE = "vnfd/valid_vnfd_with_old_vnfd_structure.yaml";
    private static final String VALID_VNF_PCC = "vnfd/valid_vnf_pcc.yaml";
    private static final String ERIC_PC_CONTROLLER_R6 = "vnfd/eric-pc-controllerR6.yaml";
    private static final String ERIC_PC_CONTROLLER_R7 = "vnfd/eric-pc-controllerR7.yaml";
    private static final String ERIC_PC_GATEWAY_R7 = "vnfd/eric-pc-gatewayR7.yaml";
    private static final String R6B_PCG_DESCRIPTOR = "vnfd/r6b_pcg_descriptor.yaml";
    private static final String INVALID_CHART_REFERENCE_IN_DEPLOYABLE_MODULE = "[non_existent_chart]";
    private static final String DEPLOYABLE_MODULE_WITH_DUPLICATED_NAMES = "deployable module with duplicated values";
    private static final String DEPLOYABLE_MODULE_WITH_DUPLICATED_CHARTS = "[\"crd_package1\",\"helm_package1\"]";
    private static final String DEPLOYABLE_MODULE_CRD_CHART = "[deployable_module_crd_3]";


    private final DeployableModuleValidator deployableModuleValidator = new DeployableModuleValidator();

    @Test
    void testValidateYamlCanBeParsed() {
        VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE).toAbsolutePath());
    }

    @Test
    void testFailureForInvalidCharacters() {
        final JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/eric-sc-1.1.0+808_sc_vnf_descriptor.yaml"));
        VnfdUtility.validateNodeType(vnfd);
        JSONObject nodeType = vnfd.getJSONObject(NODE_TYPES_KEY);
        assertThatThrownBy(() -> VnfdUtility.validateAndGetNodeProperties(nodeType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to parse VNFD")
                .hasMessageContaining("Cannot deserialize value of type `java.util.ArrayList<java.lang.Object>`");
    }

    @Test
    void testValidateYamlCanBeParsedWithInvalidYaml() {
        final Path invalidVnfdFormat = TestUtils.getResource(INVALID_VNFD_FILE_FORMAT).toAbsolutePath();
        assertThatThrownBy(() -> VnfdUtility.validateYamlCanBeParsed(invalidVnfdFormat))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE);
    }

    @Test
    void testValidateYamlCanBeParsedWithEmptyYaml() {
        final Path emptyVnfd = TestUtils.getResource(EMPTY_VNFD_FILE).toAbsolutePath();
        assertThatThrownBy(() -> VnfdUtility.validateYamlCanBeParsed(emptyVnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(VNFD_NOT_MAP_FORMAT);
    }

    @Test
    void testValidateYamlCanBeParsedWithNotExistingFile() {
        Path nonExistingVnfd = Paths.get(NON_EXISTING_VNFD_FILE);
        assertThatThrownBy(() -> VnfdUtility.validateYamlCanBeParsed(nonExistingVnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(VNFD_NOT_PRESENT_IN_THE_PATH_SPECIFIED);
    }

    @Test
    void testValidateYamlAndConvertToJsonObjectWithValidYaml() throws IOException {
        String vnfd = TestUtils.readDataFromFile(VALID_VNFD_FILE);

        JSONObject actual = VnfdUtility.validateYamlAndConvertToJsonObject(vnfd);

        assertThat(actual).isNotNull();
        assertThat(actual.length()).isEqualTo(6);
    }

    @Test
    void testValidateYamlAndConvertToJsonObjectWithInvalidYaml() {
        String invalidVnfd = "%invalid_yaml";

        assertThatThrownBy(() -> VnfdUtility.validateYamlAndConvertToJsonObject(invalidVnfd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE);
    }

    @Test
    void testToscaVnfd() {
        final Path vnfdTosca = TestUtils.getResource("vnfd/vnfd_tosca.yaml");
        assertThatThrownBy(() -> VnfdUtility.validateYamlCanBeParsed(vnfdTosca))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(VNFD_NOT_MAP_FORMAT);
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_FILE, INVALID_VNFD_FILE_SINGLE_CRD })
    void testValidateArtifactsAndInterfaceDetails(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath).toAbsolutePath());

        if (vnfdPath.equals(VALID_VNFD_FILE)) {
            VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
            assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
            assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(1);
            HelmChartType chartType = descriptorDetails.getHelmCharts().get(0).getChartType();
            assertThat(chartType).isEqualTo(CNF);
        } else {
            assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(CNF_CHARTS_NOT_PRESENT);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {VALID_NOT_DM_VNFD_FILE, VALID_DM_VNFD_FILE, ALL_OPERATION_EMPTY_HELM_CHARTS_VNFD})
    void testValidateHelmChartsCountSuccess(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath).toAbsolutePath());

        if (VALID_NOT_DM_VNFD_FILE.equals(vnfdPath) || VALID_DM_VNFD_FILE.equals(vnfdPath)
                || ALL_OPERATION_EMPTY_HELM_CHARTS_VNFD.equals(vnfdPath)) {
            VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
            assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
            List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
            assertThat(helmCharts).isNotNull().isNotEmpty().hasSize(5);
            long cnfChartCount = helmCharts.stream().filter(helmChart -> CNF.equals(helmChart.getChartType())).count();
            long crdChartCount = helmCharts.stream().filter(helmChart -> CRD.equals(helmChart.getChartType())).count();
            assertThat(cnfChartCount).isEqualTo(2);
            assertThat(crdChartCount).isEqualTo(3);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {INVALID_COUNT_HELM_CHARTS_VNFD_FILE_SCALE,
        INVALID_COUNT_HELM_CHARTS_VNFD_FILE_SCALE_SAME_HELM})
    void testValidateHelmChartsScaleCountFail(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath).toAbsolutePath());
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
        assertThat(helmCharts).isNotNull().isNotEmpty().hasSize(5);
        long cnfChartCount = helmCharts.stream().filter(helmChart -> CNF.equals(helmChart.getChartType())).count();
        long crdChartCount = helmCharts.stream().filter(helmChart -> CRD.equals(helmChart.getChartType())).count();
        assertThat(cnfChartCount).isEqualTo(2);
        assertThat(crdChartCount).isEqualTo(3);
    }

    @Test
    void testParseVnfdTwice() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/vnfd_tosca_1_3_multi_b.yaml").toAbsolutePath());
        assertThat(jsonData).isNotNull();
        assertThat(jsonData.length()).isEqualTo(8);
        assertThat(jsonData.has(ARTIFACT_KEYS)).isTrue();
        assertThat(jsonData.getJSONArray(ARTIFACT_KEYS).toList()).hasSize(7);
        String vnfdWithArtifactList = jsonData.toString();
        JSONObject jsonObject = VnfdUtility.validateYamlAndConvertToJsonObject(vnfdWithArtifactList);
        assertThat(jsonObject).isNotNull();
        assertThat(jsonObject.length()).isEqualTo(8);
        assertThat(jsonObject.has(ARTIFACT_KEYS)).isTrue();
        assertThat(jsonObject.getJSONArray(ARTIFACT_KEYS).toList()).hasSize(7);
    }

    @Test
    void testValidateHelmChartsCCVPCountFail() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(INVALID_COUNT_HELM_CHARTS_VNFD_FILE_CCVP).toAbsolutePath());
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
        assertThat(helmCharts).isNotNull().isNotEmpty().hasSize(5);
        long cnfChartCount = helmCharts.stream().filter(helmChart -> CNF.equals(helmChart.getChartType())).count();
        long crdChartCount = helmCharts.stream().filter(helmChart -> CRD.equals(helmChart.getChartType())).count();
        assertThat(cnfChartCount).isEqualTo(2);
        assertThat(crdChartCount).isEqualTo(3);
    }

    @Test
    void testValidateHelmChartsTerminateCountFail() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(INVALID_COUNT_HELM_CHARTS_VNFD_FILE_TERMINATE).toAbsolutePath());
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
        assertThat(helmCharts).isNotNull().isNotEmpty().hasSize(5);
        long cnfChartCount = helmCharts.stream().filter(helmChart -> CNF.equals(helmChart.getChartType())).count();
        long crdChartCount = helmCharts.stream().filter(helmChart -> CRD.equals(helmChart.getChartType())).count();
        assertThat(cnfChartCount).isEqualTo(2);
        assertThat(crdChartCount).isEqualTo(3);
    }

    @Test
    void testValidateHelmChartsInstantiateCountFail() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(INVALID_COUNT_HELM_CHARTS_VNFD_FILE_INSTANTIATE).toAbsolutePath());
        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(VnfdValidationException.class)
                .hasMessageContaining("Error due to incorrect reference to chart: [crd_package2] in associatedArtifacts of deployable_module");
    }
    @Test
    void testValidateHelmChartsInstantiateWithSkippedCrdNotDM() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/_invalid_vnfd_tosca_1_3_multi_b.yaml").toAbsolutePath());
        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
        assertThat(helmCharts).isNotNull().isNotEmpty().hasSize(3); // Impl GAP - Should be 5
        long cnfChartCount = helmCharts.stream().filter(helmChart -> CNF.equals(helmChart.getChartType())).count();
        long crdChartCount = helmCharts.stream().filter(helmChart -> CRD.equals(helmChart.getChartType())).count();
        assertThat(cnfChartCount).isEqualTo(2);
        assertThat(crdChartCount).isEqualTo(1); // Impl GAP - Should be 3
    }

    @Test
    void testValidateVnfdWithoutInstantiateOperationNodeTypes() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.VNFD_INVALID_DM_VNFD_WITH_OUT_INSTANTIATE).toAbsolutePath());
        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Instantiate use-case not defined in the interfaces section");
    }

    @Test
    void testValidateVnfdWithoutVnfLcmInNodeTypes() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VnfdUtils.VNFD_INVALID_DM_VNFD_WITH_OUT_VNFLCM).toAbsolutePath());
        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tosca.interfaces.nfv.Vnflcm interface not defined");
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithoutImageFileDetails() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                .getResource(INVALID_FILE_WITHOUT_IMAGE_FILE_DETAILS)
                .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DOCKER_IMAGES_NOT_PRESENT);
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithoutHelmCharts() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(INVALID_FILE_WITHOUT_HELMC_HARTS).toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(HELM_CHARTS_NOT_PRESENT);
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithoutImageDetails() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(INVALID_FILE_WITHOUT_IMAGES).toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DOCKER_IMAGES_NOT_PRESENT);
    }

    @Test
    void testBuildVnfDescriptorDetailsWithoutNodeTypes() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITHOUT_NODE_TYPE_DETAILS).toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INVALID_NODE_DETAILS_PROVIDED);
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_WITH_MULTIPLE_HELM_CHARTS, INVALID_VNFD_WITH_MULTIPLE_CRD_ONLY_HELM_CHARTS })
    void testValidateArtifactsAndInterfaceDetailsWithMultipleHelmCharts(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(vnfdPath).toAbsolutePath());

        if (vnfdPath.equals(VALID_VNFD_WITH_MULTIPLE_HELM_CHARTS)) {
            VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
            assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
            assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(3);

            final List<HelmChart> cnfCharts = collectChartsByType(descriptorDetails, CNF);
            final List<HelmChart> crdCharts = collectChartsByType(descriptorDetails, CRD);

            assertEquals(2, cnfCharts.size());
            assertThat(crdCharts).hasSize(1);
        } else {
            assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(CNF_CHARTS_NOT_PRESENT);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_REL4_WITH_MULTIPLE_MCIOP_HELM_CHARTS })
    void testValidateArtifactsAndInterfaceDetailsVFNToscaRel4(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath).toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(descriptorDetails.getImagesDetails().get(0).getPath()).isEqualTo("Files/images/docker.tar");
        assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(3);

        final List<HelmChart> cnfCharts = collectChartsByType(descriptorDetails, CNF);
        final List<HelmChart> crdCharts = collectChartsByType(descriptorDetails, CRD);

        assertThat(cnfCharts).hasSize(2);
        assertThat(crdCharts).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_REL4_WITH_DUPLICATED_MCIOP_HELM_CHARTS })
    void testToscaRel4VnfdWithDuplicatedHelmChartsInArtifacts(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(vnfdPath).toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);

        assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(4);

        final List<HelmChart> cnfCharts = collectChartsByType(descriptorDetails, CNF);
        final List<HelmChart> crdCharts = collectChartsByType(descriptorDetails, CRD);

        assertThat(cnfCharts).hasSize(3);
        assertThat(crdCharts).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_REL4_WITH_MULTIPLE_MCIOP_HELM_CHARTS })
    void testToscaRel4Vnfd(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(vnfdPath).toAbsolutePath());

        assertThat(VnfdUtility.isRel4Vnfd(jsonData)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_FILE })
    void testNotToscaRel4Vnfd(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(vnfdPath).toAbsolutePath());

        assertThat(VnfdUtility.isRel4Vnfd(jsonData)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_REL4_WITH_ALL_DUPLICATED_MCIOP_HELM_CHARTS })
    void testToscaRel4VnfdWithAllDuplicatedHelmChartsInArtifacts(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(vnfdPath).toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);

        assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(4);

        final List<HelmChart> cnfCharts = collectChartsByType(descriptorDetails, CNF);
        final List<HelmChart> crdCharts = collectChartsByType(descriptorDetails, CRD);

        assertThat(cnfCharts).hasSize(3);
        assertThat(crdCharts).hasSize(1);
    }

    private List<HelmChart> collectChartsByType(final VnfDescriptorDetails descriptorDetails, final HelmChartType crd) {
        return descriptorDetails.getHelmCharts()
                .stream()
                .filter(chart -> crd.equals(chart.getChartType()))
                .collect(Collectors.toList());
    }

    @Test
    void testMultipleHelmChartsInNodeTemplate() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(MULTIPLE_HELM_CHARTS_IN_NODE_TEMPLATE_YAML)
                                                                                .toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getHelmCharts()).hasSize(3);
        assertThat(descriptorDetails.getHelmCharts().get(0).getChartType()).isEqualTo(CNF);
        assertThat(descriptorDetails.getHelmCharts().get(2).getChartType()).isEqualTo(CRD);
        assertThat(descriptorDetails.getImagesDetails()).isNotEmpty().hasSize(1);
    }

    @Test
    void testMultipleHelmChartsInNodeTemplateAndNodeType() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(MULTIPLE_HELM_CHARTS_IN_NODE_TEMPLATE_AND_NODE_TYPE)
                                                                                .toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        List<HelmChart> helmCharts = descriptorDetails.getHelmCharts();
        long cnfChartCount = helmCharts.stream().filter(helmChart -> CNF.equals(helmChart.getChartType())).count();
        long crdChartCount = helmCharts.stream().filter(helmChart -> CRD.equals(helmChart.getChartType())).count();
        assertThat(helmCharts).hasSize(4);
        assertThat(cnfChartCount).isEqualTo(3);
        assertThat(crdChartCount).isEqualTo(1);
        assertThat(descriptorDetails.getImagesDetails()).isNotEmpty();
    }

    @Test
    void testMultipleHelmChartsInNodeTypeWithNodeTemplate() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                .getResource(MULTIPLE_HELM_CHARTS_IN_NODE_TYPE_WITH_TOPOLOGY_TEMPLATE)
                                                                                .toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getHelmCharts()).hasSize(4);
        assertThat(descriptorDetails.getHelmCharts().get(0).getChartType()).isEqualTo(CNF);
        assertThat(descriptorDetails.getHelmCharts().get(3).getChartType()).isEqualTo(CRD);
        assertThat(descriptorDetails.getImagesDetails()).isNotEmpty().hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_VNFD_WITH_NULL_HELM_PACKAGE, INVALID_VNFD_WITH_NULL_HELM_PACKAGE_CRD })
    void testValidateArtifactsAndInterfaceDetailsWithHelmPackagesAsNull(String vnfdPath) {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath).toAbsolutePath());

        if (vnfdPath.equals(VALID_VNFD_WITH_NULL_HELM_PACKAGE)) {
            VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
            assertThat(descriptorDetails.getImagesDetails()).isNotEmpty().hasSize(1);
            assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(1);
            HelmChartType chartType = descriptorDetails.getHelmCharts().get(0).getChartType();
            assertThat(chartType).isEqualTo(CNF);
        } else {
            assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(CNF_CHARTS_NOT_PRESENT);
        }
    }

    @Test
    void testInvalidArtifactsRollbackValidationFailed() {
        String vnfdPath = "vnfd/invalid_all_empty_helm_charts.yaml";
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath).toAbsolutePath());
        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Helmchart helm_package1 with command rollback defined " +
                        "in vnf package change pattern not found in artifacts section of vnfd");
    }

    @Test
    void testGetChartsFromArtifactsBlock() {
        String vnfdPath = "vnfd/valid_all_empty_helm_charts.yaml";
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(vnfdPath).toAbsolutePath());
        VnfDescriptorDetails vnfDescriptorDetails = buildVnfDescriptorDetails(jsonData);
        List<HelmChart> helmCharts = vnfDescriptorDetails.getHelmCharts();
        assertThat(helmCharts).isNotNull().hasSize(5);
        assertThat(helmCharts.get(0).getChartKey()).isEqualTo("crd_package1");
        assertThat(helmCharts.get(1).getChartKey()).isEqualTo("helm_package1");
        assertThat(helmCharts.get(2).getChartKey()).isEqualTo("crd_package3");
        assertThat(helmCharts.get(3).getChartKey()).isEqualTo("helm_package2");
        assertThat(helmCharts.get(4).getChartKey()).isEqualTo("crd_package2");
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithOneHelmCharts() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/interfaceParsing/valid_vnfd_with_one_helm_chart_in_helm_packages.yaml").toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(descriptorDetails.getImagesDetails()).isNotNull();

        assertThat(descriptorDetails.getHelmCharts()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(descriptorDetails.getHelmCharts().get(0).getChartType()).isEqualTo(CNF);
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithoutHelmPackagesAndHelmPackageAttributeInArtifacts() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_helm_packages_and_without_halm_package_attribute_" +
                        "in_artifacts.yaml").toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(HELM_CHARTS_NOT_PRESENT);
    }

    @Test
    void testBuildVnfDescriptorDetailsWithInvalidScalingAspectsRel4Vnfd() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(getResource(INVALID_VNFD_REL4));
        Path csarBasePath = getResource("");

        assertThatThrownBy(() -> VnfdUtility.validateAspects(parsed, csarBasePath))
                .isInstanceOf(VnfdValidationException.class)
                .hasMessageEndingWith("invalid vnfd, as one of the VDUs is under the two DMs");
    }

    @Test
    void testBuildVnfDescriptorDetailsWithInvalidScalingAspectsNoRel4Vnfd() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(getResource(INVALID_VNFD_NO_REL4));
        Path csarBasePath = getResource("");

        assertThatThrownBy(() -> VnfdUtility.validateAspects(parsed, csarBasePath))
                .isInstanceOf(VnfdValidationException.class)
                .hasMessageEndingWith("invalid vnfd, as one of the VDUs is under the two DMs");
    }

    @Test
    void testBuildVnfDescriptorDetailsWithValidScalingAspectsRel4Vnfd() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(getResource(VALID_VNFD_REL4));
        Path csarBasePath = getResource("");

        VnfdUtility.validateAspects(parsed, csarBasePath);
    }

    @Test
    void testBuildVnfDescriptorDetailsWithValidScalingAspectsRel4MixedVduVnfd() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(getResource(VALID_VNFD_REL4_MIXED_VDU));
        Path csarBasePath = getResource("");

        VnfdUtility.validateAspects(parsed, csarBasePath);
    }

    @Test
    void testBuildVnfDescriptorDetailsWithValidScalingAspectsNoRel4Vnfd() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(getResource(VALID_VNFD_NO_REL4));
        Path csarBasePath = getResource("");

        VnfdUtility.validateAspects(parsed, csarBasePath);
    }

    @Test
    void testValidateInterfaces() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE).toAbsolutePath());
        JSONObject interfaceJsonObject = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME).getJSONObject(INTERFACES_KEY);
        JSONObject dataTypes = jsonData.getJSONObject(DATA_TYPES_KEY);
        VnfdUtility.validateInterfaces(interfaceJsonObject, dataTypes);
    }

    @Test
    void testValidateInterfacesWithInvalidJSONData() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE)
                                                                          .toAbsolutePath());
        final JSONObject jsonObject = jsonData.getJSONObject(DATA_TYPES_KEY);

        assertThatThrownBy(() -> VnfdUtility.validateInterfaces(jsonData, jsonObject))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(VNFLCM_INTERFACE_NOT_PRESENT_ERROR_MESSAGE);
    }

    @Test
    void testValidateInterfacesWithInvalidInterfaceInput() {
        final JSONObject jsonData = VnfdUtility
                .validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITH_INVALID_INTERFACE_INPUT)
                                                 .toAbsolutePath());
        JSONObject interfaceData = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME)
                .getJSONObject(INTERFACES_KEY);
        JSONObject dataTypes = jsonData.getJSONObject(DATA_TYPES_KEY);
        assertThatThrownBy(() -> VnfdUtility.validateInterfaces(interfaceData, dataTypes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(format(INVALID_INPUT_PROVIDED_FOR_INPUTS, "instantiate"));
    }

    @Test
    void testValidateInterfacesWithAdditionalPropertiesNotProvided() {
        JSONObject interfaceData = null;
        JSONObject dataTypes = null;
        try {
            JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                    TestUtils.getResource(INVALID_VNFD_WITH_ADDITIONAL_PROPERTIES_NOT_PROVIDED).toAbsolutePath());
            interfaceData = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME)
                    .getJSONObject(INTERFACES_KEY);
            dataTypes = jsonData.getJSONObject(DATA_TYPES_KEY);
        } catch (final JSONException e) {
            fail(StringUtils.EMPTY);
        }
        try {
            VnfdUtility.validateInterfaces(interfaceData, dataTypes);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (final IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(
                    format(ADDITIONAL_PARAMETERS_NOT_PROVIDED_FOR_INPUTS_ERROR_MESSAGE, "instantiate"));
        }
    }

    @Test
    void testValidateInterfacesWithTypeNotProvidedForAdditionalProperties() {
        JSONObject interfaceData = null;
        JSONObject dataTypes = null;
        try {
            JSONObject jsonData = VnfdUtility
                    .validateYamlCanBeParsed(TestUtils.getResource(
                            INVALID_VNFD_WITH_TYPE_NOT_PROVIDED_FOR_ADDITIONAL_PROPERTIES).toAbsolutePath());
            interfaceData = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME)
                    .getJSONObject(INTERFACES_KEY);
            dataTypes = jsonData.getJSONObject(DATA_TYPES_KEY);
        } catch (final JSONException ex) {
            fail(StringUtils.EMPTY);
        }
        try {
            VnfdUtility.validateInterfaces(interfaceData, dataTypes);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (final IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(format(Constants.TYPE_NOT_DEFINED_FOR_ADDITIONAL_PARAMETERS, "instantiate"));
        }
    }

    @Test
    void testValidateInterfacesWithDataTypeNotDefined() {
        JSONObject interfaceData = null;
        try {
            final JSONObject jsonData = VnfdUtility
                    .validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITH_DATA_TYPE_NOT_DEFINED).toAbsolutePath());
            interfaceData = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME).getJSONObject(INTERFACES_KEY);
        } catch (final JSONException ex) {
            fail(StringUtils.EMPTY);
        }
        try {
            VnfdUtility.validateInterfaces(interfaceData, null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (final IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(format(DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE,
                                                          "ericsson.datatypes.nfv.InstantiateVnfOperationAdditionalParameters"));
        }
    }

    @Test
    void testValidateNodeType() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE).toAbsolutePath());
        VnfdUtility.validateNodeType(jsonData);
    }

    @Test
    void testValidateWithMultipleHelmCharts() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_WITH_MULTIPLE_HELM_CHARTS).toAbsolutePath());
        VnfdUtility.validateNodeType(jsonData);
    }

    @Test
    void testValidateNodeTypeWithoutNodeTypeInVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITHOUT_NODE_TYPE).toAbsolutePath());
        try {
            VnfdUtility.validateNodeType(jsonData);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (final IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(NODE_TYPE_NOT_PRESENT_ERROR_MESSAGE);
        }
    }

    @Test
    void testValidateNodeTypeWithMoreThanOneNodeType() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITH_TWO_NODE_TYPE).toAbsolutePath());
        try {
            VnfdUtility.validateNodeType(jsonData);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (final IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(MORE_THAN_ONE_NODE_TYPE_ERROR_MESSAGE);
        }
    }

    @Test
    void testValidateNodeTypeWithoutNodeTypeDetails() {
        Path invalidVnfd = TestUtils.getResource(INVALID_VNFD_WITHOUT_NODE_TYPE_DETAILS);
        JSONObject invalidVnfdJson = VnfdUtility.validateYamlCanBeParsed(invalidVnfd);
        assertThatThrownBy(() -> VnfdUtility.validateNodeType(invalidVnfdJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NODE_TYPE_DETAILS_NOT_PRESENT_ERROR_MESSAGE);
    }

    @Test
    void testGetImagePathWithoutDockerImagesPresent() {
        assertThatThrownBy(() -> VnfdUtility.getImagePath(new JSONObject(), new JSONObject()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(DOCKER_IMAGES_NOT_PRESENT);
    }

    @Test
    void testValidateNodeProperties() {
        JSONObject nodeTypeData = null;
        try {
            final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE).toAbsolutePath());
            nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);
        } catch (final JSONException ex) {
            fail(StringUtils.EMPTY);
        }
        assertThat(nodeTypeData).isNotNull();
        final NodeProperties nodeProperties = VnfdUtility.validateAndGetNodeProperties(nodeTypeData);
        assertThat(nodeProperties).isNotNull();
    }

    @Test
    void testValidateNodePropertiesWithMultipleHelmCharts() {
        JSONObject nodeTypeData = null;
        try {
            final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                                    .getResource(VALID_VNFD_WITH_MULTIPLE_HELM_CHARTS)
                                                                                    .toAbsolutePath());
            nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);
        } catch (final JSONException ex) {
            fail(StringUtils.EMPTY);
        }
        assertThat(nodeTypeData).isNotNull();
        final NodeProperties nodeProperties = VnfdUtility.validateAndGetNodeProperties(nodeTypeData);
        assertThat(nodeProperties).isNotNull();
    }

    @Test
    void testGetScalingMappingFile() {
        JSONObject parsedVNFD = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                            .getResource("vnfd/valid_vnfd_no_scale_info.yaml"));
        final String scalingMappingFileArtifactPath = VnfdUtility.getScalingMappingFileArtifactPathFromVNFD(parsedVNFD);
        assertFalse(scalingMappingFileArtifactPath.isEmpty());
        assertThat(scalingMappingFileArtifactPath).isEqualTo("Definitions/OtherTemplates/scaling_mapping.yaml");
    }

    @Test
    void testGetScalingMappingFileWithoutScalingMappingKeyFail() {
        JSONObject parsedVNFD = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                            .getResource("vnfd/valid_vnfd_no_scaling_mapping_key_in_artifacts.yaml"));

        final String scalingMappingFileArtifactPath = VnfdUtility.getScalingMappingFileArtifactPathFromVNFD(parsedVNFD);
        assertNull(scalingMappingFileArtifactPath);
    }

    @Test
    void testGetScalingMappingFileWithoutArtifactsKeyFail() {
        JSONObject parsedVNFD = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                            .getResource("vnfd/invalid_vnfd_no_artifacts_key_in_node_types.yaml"));
        assertThatThrownBy(() -> VnfdUtility.getScalingMappingFileArtifactPathFromVNFD(parsedVNFD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(format(PROPERTY_NOT_FOUND_IN_VNFD, ARTIFACTS_KEY));
    }

    @Test
    void testValidateNodePropertiesWithoutDerivedForm() {
        Path vnfdPath = TestUtils.getResource(VnfdUtils.INVALID_VNFD_WITHOUT_DERIVED_FORM_ATTRIBUTE).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(vnfdPath);
        JSONObject nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);

        assertThatThrownBy(() -> VnfdUtility.validateAndGetNodeProperties(nodeTypeData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(DERIVED_FROM_NOT_DEFINED_ERROR_MESSAGE, NODE_TYPE_NAME));
    }

    @Test
    void testValidateNodePropertiesWithInvalidDerivedForm() {
        Path vnfdPath = TestUtils.getResource(VnfdUtils.INVALID_VNFD_WITH_WRONG_DERIVED_FORM_VALUE).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(vnfdPath);
        JSONObject nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);

        assertThatThrownBy(() -> VnfdUtility.validateAndGetNodeProperties(nodeTypeData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DERIVED_FROM_VALUE_NOT_SUPPORTED);
    }

    @Test
    void testValidateNodePropertiesWithoutProperties() {
        JSONObject nodeTypeData = null;
        try {
            final JSONObject jsonData = VnfdUtility
                    .validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITHOUT_NODE_PROPERTIES).toAbsolutePath());
            nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);
        } catch (final JSONException ex) {
            fail(StringUtils.EMPTY);
        }
        assertThat(nodeTypeData).isNotNull();
        try {
            VnfdUtility.validateAndGetNodeProperties(nodeTypeData);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (final IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(NODE_PROPERTY_MISSING_ERROR_MESSAGE + NODE_TYPE_NAME);
        }
    }

    @Test
    void testValidateNodePropertiesWithoutVnfdId() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(INVALID_VNFD_WITHOUT_VNFD_ID).toAbsolutePath());
        JSONObject nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);
        assertThat(nodeTypeData).isNotNull();

        final ConstraintViolationException thrown = assertThrows(
                ConstraintViolationException.class, () -> VnfdUtility.validateAndGetNodeProperties(nodeTypeData),
                "Expected validateAndGetNodeProperties() to throw ConstraintViolationException but it did not");
        assertEquals("descriptorId: DESCRIPTOR_ID_REQUIRED", thrown.getMessage());
    }

    @Test
    void testValidateNodePropertyWhichViolatesConstraint() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(INVALID_VNFD_NODE_TYPES_VIOLATES_CONSTRAINT).toAbsolutePath());

        JSONObject nodeTypeData = jsonData.getJSONObject(NODE_TYPES_KEY);
        assertThat(nodeTypeData).isNotNull();

        final ConstraintViolationException thrown = assertThrows(
                ConstraintViolationException.class, () -> VnfdUtility.validateAndGetNodeProperties(nodeTypeData),
                "Expected validateAndGetNodeProperties() to throw ConstraintViolationException but it did not");
        assertThat(thrown.getConstraintViolations()).hasSize(5);
    }

    @Test
    void testValidateInterfacesWithBlankUseCase() {
        Path validVnfdPath = TestUtils.getResource(VALID_VNFD_FILE_WITH_EMPTY_INTERFACE_DEF).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(validVnfdPath);
        JSONObject interfaceJsonObject = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME)
                .getJSONObject(INTERFACES_KEY);
        JSONObject dataTypes = jsonData.getJSONObject(DATA_TYPES_KEY);
        VnfdUtility.validateInterfaces(interfaceJsonObject, dataTypes);
    }

    @Test
    void testValidateInterfacesWithInputMissing() {
        JSONObject interfaceJsonObject;
        JSONObject dataTypes;
        Path validVnfdPath = TestUtils.getResource("vnfd/invalid_vnfd_with_invalid_usecase_parameter.yaml")
                .toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(validVnfdPath);
        interfaceJsonObject = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME)
                .getJSONObject(INTERFACES_KEY);
        dataTypes = jsonData.getJSONObject(DATA_TYPES_KEY);
        assertThatThrownBy(() -> VnfdUtility.validateInterfaces(interfaceJsonObject, dataTypes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_INPUT_PROVIDED_FOR_INPUTS, "instantiate"));
    }

    @Test
    void testCheckIsNestedVnfdWithNestedVnfd() {
        Path pathToNode = TestUtils.getResource(PARENT_VNFD_YAML).toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        assertThat(isNestedVnfd(jsonData)).isTrue();
    }

    @Test
    void testCheckIsNestedVnfdWithNormalVnfd() {
        Path pathToNode = TestUtils.getResource("nestedVnfd/spider-app-label-verification-2.193.100.yaml").toAbsolutePath();
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(pathToNode);
        assertThat(isNestedVnfd(jsonData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("providedData")
    void testValidateArtifactsAndInterfaceDetailsWithCustomerInterfaceDefinition(String testName, String vnfdPath, int expectedAddDataTypesSize,
                                                                                 int expectedAllOperationsSize) {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath));

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        assertThat(descriptorDetails.getDefaultFlavour()).isNotNull();
        assertThat(descriptorDetails.getAllDataTypes()).hasSize(expectedAddDataTypesSize);
        Map<String, InterfaceType> allInterfaceType = descriptorDetails.getAllInterfaceTypes();
        Map<String, DataType> allDataType = descriptorDetails.getAllDataTypes();
        assertThat(allInterfaceType).hasSize(1);
        for (String key : allInterfaceType.keySet()) {
            InterfaceType interfaceType = allInterfaceType.get(key);
            Map<String, InterfaceTypeImpl> customInterface = descriptorDetails.getDefaultFlavour()
                    .getTopologyTemplate().getNodeTemplate().getCustomInterface();
            for (String customInterfaceKey : customInterface.keySet()) {
                assertThat(customInterface.get(customInterfaceKey).getInterfaceType())
                        .isEqualTo(key);
            }
            assertThat(interfaceType.getDerivedFrom()).isEqualTo(TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE);
            Map<String, CustomOperation> allOperation = interfaceType.getOperation();
            assertThat(allOperation).hasSize(expectedAllOperationsSize);
            for (String operationKey : allOperation.keySet()) {
                CustomOperation operation = allOperation.get(operationKey);
                CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                assertThat(allDataType.get(dataTypeName)).isNotNull();
            }
        }
    }

    private static Stream<Arguments> providedData() {
        return Stream.of(
                Arguments.of("CustomerInterfaceDefinition", VnfdUtils.VNFD_VALID_VNFD_WITH_ROLLBACK_POLICIES, 2, 1),
                Arguments.of("CustomerInterfaceDefinitionWithoutNodeTemplate",
                        VnfdUtils.VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE, 2, 1),
                Arguments.of("HelmPackagesAsJsonObject", VnfdUtils.VNFD_VALID_VNFD_WITH_INSTANTIATE_HELM_PACKAGE_AS_JSON_OBJECT, 2, 1),
                Arguments.of("MultipleOperationForRollbackInterface", VnfdUtils.VNFD_VALID_VNFD_WITH_MULTIPLE_ROLLBACK_OPERATION, 3, 2),
                Arguments.of("NodeTypeDefinitionInNodeTemplate",
                        VnfdUtils.VNFD_VALID_VNFD_WITH_NODE_TYPE_DEFINITION_IN_NODE_TEMPLATES, 3, 2),
                Arguments.of("NodeTypeDefinitionInNodeTemplate",
                        VnfdUtils.VNFD_VALID_VNFD_WITH_NODE_TYPE_DEFINITION_IN_NODE_TEMPLATES, 3, 2)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testValidateArtifactsAndInterfaceDetailsWithCustomInterfaceDefinitionNewVnfdFormat() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format.yaml"));

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        assertThat(descriptorDetails.getDefaultFlavour()).isNotNull();
        assertThat(descriptorDetails.getAllDataTypes()).hasSize(4);
        Map<String, InterfaceType> allInterfaceType = descriptorDetails.getAllInterfaceTypes();
        Map<String, DataType> allDataType = descriptorDetails.getAllDataTypes();
        assertThat(allInterfaceType).hasSize(1);
        for (String key : allInterfaceType.keySet()) {
            InterfaceType interfaceType = allInterfaceType.get(key);
            Map<String, InterfaceTypeImpl> customInterface = descriptorDetails.getDefaultFlavour()
                    .getTopologyTemplate().getNodeTemplate().getCustomInterface();
            for (String customInterfaceKey : customInterface.keySet()) {
                InterfaceTypeImpl custom = customInterface.get(customInterfaceKey);
                assertThat(custom.getInterfaceType())
                        .isEqualTo(key);
                HashMap<String, Object> inputs = (HashMap<String, Object>) custom.getInputs();
                assertThat(inputs).size().isEqualTo(5);
                assertThat(inputs).containsKeys("spring", "rollback_pattern", "rollback_at_failure_pattern",
                                                "listParam", "scalarStringParam");
                Map<String, CustomOperation> allOperation = custom.getOperation();
                assertThat(allOperation).hasSize(2);
                for (String operationKey : allOperation.keySet()) {
                    CustomOperation operation = allOperation.get(operationKey);
                    CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                    String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                    assertThat(allDataType.get(dataTypeName)).isNotNull();
                    if ("rollback_from_package6_to_package4".equals(operationKey)) {
                        Map<String, Object> staticAdditionalParams = rollbackInput.getStaticAdditionalParams();
                        assertThat(staticAdditionalParams).size().isEqualTo(1);
                        assertThat(staticAdditionalParams).containsKey("rollback_pattern");
                    } else {
                        Map<String, Object> staticAdditionalParams = rollbackInput.getStaticAdditionalParams();
                        assertThat(staticAdditionalParams).containsKeys("scalarStringParam", "spring",
                                                                        "listParamRollBack");
                    }
                }
            }
            assertThat(interfaceType.getDerivedFrom()).isEqualTo(TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE);
            Map<String, CustomOperation> allOperation = interfaceType.getOperation();
            assertThat(allOperation).hasSize(2);
            for (String operationKey : allOperation.keySet()) {
                CustomOperation operation = allOperation.get(operationKey);
                CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                assertThat(allDataType.get(dataTypeName)).isNotNull();
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testValidateArtifactsAndInterfaceDetailsWithCustomInterfaceNoPatternsDefined() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_with_rollback_policies_new_format_no_patterns.yaml"));

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        assertThat(descriptorDetails.getDefaultFlavour()).isNotNull();
        assertThat(descriptorDetails.getAllDataTypes()).hasSize(4);
        Map<String, InterfaceType> allInterfaceType = descriptorDetails.getAllInterfaceTypes();
        Map<String, DataType> allDataType = descriptorDetails.getAllDataTypes();
        assertThat(allInterfaceType).hasSize(1);
        for (String key : allInterfaceType.keySet()) {
            InterfaceType interfaceType = allInterfaceType.get(key);
            Map<String, InterfaceTypeImpl> customInterface = descriptorDetails.getDefaultFlavour()
                    .getTopologyTemplate().getNodeTemplate().getCustomInterface();
            for (String customInterfaceKey : customInterface.keySet()) {
                InterfaceTypeImpl custom = customInterface.get(customInterfaceKey);
                assertThat(custom.getInterfaceType())
                        .isEqualTo(key);
                HashMap<String, Object> inputs = (HashMap<String, Object>) custom.getInputs();
                assertThat(inputs).size().isEqualTo(1);
                assertThat(inputs).containsKeys("temp");
                Map<String, CustomOperation> allOperation = custom.getOperation();
                assertThat(allOperation).hasSize(2);
                for (String operationKey : allOperation.keySet()) {
                    CustomOperation operation = allOperation.get(operationKey);
                    CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                    String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                    assertThat(allDataType.get(dataTypeName)).isNotNull();
                    Map<String, Object> staticAdditionalParams = rollbackInput.getStaticAdditionalParams();
                    assertThat(staticAdditionalParams).isNull();
                }
            }
            assertThat(interfaceType.getDerivedFrom()).isEqualTo(TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE);
            Map<String, CustomOperation> allOperation = interfaceType.getOperation();
            assertThat(allOperation).hasSize(2);
            for (String operationKey : allOperation.keySet()) {
                CustomOperation operation = allOperation.get(operationKey);
                CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                assertThat(allDataType.get(dataTypeName)).isNotNull();
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testValidateArtifactsAndInterfaceDetailsPatternMissingInputs() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_with_rollback_policies_failure_pattern_missing_inputs.yaml"));

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        assertThat(descriptorDetails.getDefaultFlavour()).isNotNull();
        assertThat(descriptorDetails.getAllDataTypes()).hasSize(4);
        Map<String, InterfaceType> allInterfaceType = descriptorDetails.getAllInterfaceTypes();
        Map<String, DataType> allDataType = descriptorDetails.getAllDataTypes();
        assertThat(allInterfaceType).hasSize(1);
        for (String key : allInterfaceType.keySet()) {
            InterfaceType interfaceType = allInterfaceType.get(key);
            Map<String, InterfaceTypeImpl> customInterface = descriptorDetails.getDefaultFlavour()
                    .getTopologyTemplate().getNodeTemplate().getCustomInterface();
            for (String customInterfaceKey : customInterface.keySet()) {
                InterfaceTypeImpl custom = customInterface.get(customInterfaceKey);
                assertThat(custom.getInterfaceType())
                        .isEqualTo(key);
                HashMap<String, Object> inputs = (HashMap<String, Object>) custom.getInputs();
                assertThat(inputs).isNull();
                Map<String, CustomOperation> allOperation = custom.getOperation();
                assertThat(allOperation).hasSize(2);
                for (String operationKey : allOperation.keySet()) {
                    CustomOperation operation = allOperation.get(operationKey);
                    CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                    String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                    assertThat(allDataType.get(dataTypeName)).isNotNull();
                    Map<String, Object> staticAdditionalParams = rollbackInput.getStaticAdditionalParams();
                    assertThat(staticAdditionalParams).isNull();
                }
            }
            assertThat(interfaceType.getDerivedFrom()).isEqualTo(TOSCA_INTERFACES_NFV_CHANGE_CURRENT_VNF_PACKAGE_TYPE);
            Map<String, CustomOperation> allOperation = interfaceType.getOperation();
            assertThat(allOperation).hasSize(2);
            for (String operationKey : allOperation.keySet()) {
                CustomOperation operation = allOperation.get(operationKey);
                CustomInterfaceInputs rollbackInput = (CustomInterfaceInputs) operation.getInput();
                String dataTypeName = rollbackInput.getAdditionalParams().getDataTypeName();
                assertThat(allDataType.get(dataTypeName)).isNotNull();
            }
        }
    }

    @Test
    void testPoliciesInVnfdWithMissingInputInRollbackInterfaceDefinition() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_input_in_rollback_interface_definition.yaml"));

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("inputs missing for property change_to_version_1");
    }

    @Test
    void testPoliciesInVnfdWithMissingAdditionalParametersInRollbackInterfaceDefinition() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_additional_parameter_in_rollback_interface_definition.yaml"));

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Property additional_parameters was not found in VNFD");
    }

    @Test
    void testPoliciesInVnfdWithMissingTypeInAdditionalParametersInRollbackInterfaceDefinition() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_type_in_additional_parameter.yaml"));

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot get type from Vnfd for operation change_to_version_1");
    }

    @Test
    void testPoliciesInVnfdWithMissingDataTypeInAdditionalParametersInRollbackInterfaceDefinition() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_data_type_definition_in_rollback.yaml"));

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(parsed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data type not defined for MyCompany.datatypes.nfv.VnfChangeToVersion1AdditionalParameters");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        VALID_VNFD_WITH_OLD_STRUCTURE, VALID_VNF_PCC, ERIC_PC_CONTROLLER_R6,
        ERIC_PC_CONTROLLER_R7, ERIC_PC_GATEWAY_R7, R6B_PCG_DESCRIPTOR})
    void testValidateArtifactsAndInterfaceDetailsWithDiffStructure(String vnfdPath) {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfdPath));

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(parsed);
        assertThat(descriptorDetails.getDefaultFlavour()).isNotNull();
        assertThat(descriptorDetails.getAllDataTypes()).hasSize(1);
        Map<String, InterfaceType> allInterfaceType = descriptorDetails.getAllInterfaceTypes();
        assertThat(allInterfaceType).isEmpty();
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithHeal() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VALID_VNFD_FILE_HEAL).toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        List<VnfmLcmInterface> interfaces = descriptorDetails.getDefaultFlavour()
                .getTopologyTemplate().getNodeTemplate()
                .getInterfaces();
        assertThat(interfaces.stream()
                           .anyMatch(vnfmLcmInterface -> vnfmLcmInterface.getType() == VnfmLcmInterface.Type.HEAL))
                .isTrue();
    }

    @Test
    void testValidateArtifactsAndInterfaceDetailsWithMultiHelmHeal() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VALID_VNFD_HEAL_WITH_MULTI_HELM).toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        List<VnfmLcmInterface> interfaces = descriptorDetails.getDefaultFlavour()
                .getTopologyTemplate().getNodeTemplate()
                .getInterfaces();
        assertThat(interfaces.stream()
                           .anyMatch(vnfmLcmInterface -> vnfmLcmInterface.getType() == VnfmLcmInterface.Type.HEAL))
                .isTrue();
    }

    @Test
    void testValidateYamlParseNullValues() {
        String expectedDefaultValueWithNulls = "{\"key1\":null,\"key2\":null}";

        final JSONObject actualParsedResult = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VALID_VNFD_WITH_NULL_VALUES).toAbsolutePath());
        String actualStringParsedResult = actualParsedResult.getJSONObject("data_types")
                .getJSONObject("ericsson.datatypes.nfv.InstantiateVnfOperationAdditionalParameters")
                .getJSONObject("properties")
                .getJSONObject("myMap.null")
                .getJSONObject("default")
                .toString();

        assertEquals(expectedDefaultValueWithNulls, actualStringParsedResult);
    }

    @Test
    void testVnfInfoModifiableAttributesInDataTypes() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(VALID_VNFD_WITH_NEW_SCALE_DATA_TYPES).toAbsolutePath());

        VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);
        Map<String, DataType> allDataTypes = descriptorDetails.getAllDataTypes();
        int count = 0;
        for (Map.Entry<String, DataType> entry : allDataTypes.entrySet()) {
            if (entry.getValue().getDerivedFrom().equals(TOSCA_DATATYPE_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_TYPE)) {
                count++;
                Property extensions = entry.getValue().getProperties().get(EXTENSIONS_KEY);
                assertNotNull(extensions);
                assertNotNull(extensions.getType());
                DataType extensionType = extensions.getTypeValue();
                assertNotNull(extensionType);
                assertThat(extensionType.getDerivedFrom()).isEqualTo(TOSCA_DATATYPES_NFV_VNF_INFO_MODIFIABLE_ATTRIBUTES_EXTENSIONS_TYPE);
                Map<String, Property> extensionsProperty = extensionType.getProperties();
                assertThat(extensionsProperty).isNotEmpty();
                Property vnfControllingScalingProperty = extensionsProperty.get(VNF_CONTROLLED_SCALING_PROPERTY);
                assertNotNull(vnfControllingScalingProperty);
                assertThat(vnfControllingScalingProperty.getDefaultValue()).isNotBlank();
                assertThat(vnfControllingScalingProperty.getType()).isEqualTo("map");
                EntrySchema entrySchema = vnfControllingScalingProperty.getEntrySchema();
                assertThat(entrySchema).isNotNull();
                assertThat(entrySchema.getType()).isNotBlank();
                assertThat(entrySchema.getConstraints()).isNotBlank();
            }
        }
        assertThat(count).isNotZero();
    }

    @Test
    void testVnfInfoModifiableAttributesInDataTypesWithoutEntrySchemaInExtensions() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(INVALID_VNFD_WITH_INVALID_EXTENSIONS).toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_ENTRY_SCHEMA_ERROR_MESSAGE, VNF_CONTROLLED_SCALING_PROPERTY));
    }

    @Test
    void testVnfInfoModifiableAttributesInDataTypesWithMissingTypeInExtensions() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_for_no_type_for_vnfControlledScaling.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("type missing for property vnfControlledScaling");
    }

    @Test
    void testVnfdWithInvalidPropertyAttribute() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_unknown_property_in_data_type.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_PROPERTY_KEY_ERROR_MESSAGE, "invalidPropertyAttribute",
                                   "ossTopology.transportProtocol"));
    }

    @Test
    void testVnfdWithInvalidPropertyIndentation() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_invalid_indentation_in_data_type_properties.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_PROPERTY_KEY_ERROR_MESSAGE, "ossTopology.transportProtocol",
                                   "ossTopology.timeZone"));
    }

    @Test
    void testVnfInfoModifiableAttributesInDataTypesMissingExtensions() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_extensions_type_missing.yaml").toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(DATA_TYPE_NOT_DEFINED_ERROR_MESSAGE,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsPropertiesMissing() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_properties_missing_in_extensions.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(PROPERTIES_NULL_FOR_DATATYPE_ERROR_MESSAGE,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsMissing() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_extensions_missing_in_VnfInfoModifiableAttributes.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(EXTENSION_CANT_BE_NULL, "ericsson.datatypes.nfv.VnfInfoModifiableAttributes"));
    }

    @Test
    void testInvalidTypeInProperty() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_type_in_property.yaml")
                        .toAbsolutePath());

        assertDoesNotThrow(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData));
    }

    @Test
    void testMissingFieldsInArtifacts() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/valid_vnfd_multiple_helm_charts_in_node_template.yaml")
                        .toAbsolutePath());

        assertDoesNotThrow(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData));
    }

    @Test
    void testExtensionsInvalidDerivedForm() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_wrong_extensions_derived_from.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_EXTENSION_DATA_TYPE_ERROR_MESSAGE,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributes"));
    }

    @Test
    void testExtensionsPropertiesMissingInDataType() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_properties_missing_in_VnfInfoModifiableAttributesExtensions.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(PROPERTIES_CANT_BE_NULL_ERROR_MESSAGE,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithMissingPropertyVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_vnf_controlled_scaling_missing_in_extensions.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(VNF_CONTROLLED_SCALING_PROPERTY_MISSING,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithInvalidTypeOfVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_vnfControlledScaling_invalid_type.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(EXTENSION_PROPERTIES_INVALID_TYPE_ERROR_MESSAGE,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithInvalidTypeOfDeployableModules() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_deployableModules_invalid_type.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(EXTENSION_PROPERTIES_INVALID_TYPE_ERROR_MESSAGE,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithDefaultValueMissingForVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_default_value_missing_for_vnfcontrolledscaling.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(VNF_CONTROLLED_SCALING_DEFAULT_VALUE_MISSING,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithDefaultValueInvalidFormatVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_default_value_invalid_format_for_vnfControlledScaling.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(VNF_CONTROLLED_SCALING_DEFAULT_VALUE_NOT_MAP);
    }

    @Test
    void testExtensionsWithDefaultValueIsBlankForAspectInVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/invalid_vnfd_with_default_of_aspect_is_blank.yaml")
                                                                                .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(VNF_CONTROLLED_SCALING_DEFAULT_VALUE_BLANK_FOR_ASPECT_FORMAT, "Aspect1"));
    }

    @Test
    void testExtensionsWithDefaultValueIsBlankForKeyInDeployableModules() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/validation/deployableModules/invalid_vnfd_with_default_of_key_is_blank.yaml")
                                                                                .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(DEPLOYABLE_MODULES_DEFAULT_VALUE_BLANK_FOR_KEY_FORMAT, "depModule_crd1"));
    }

    @Test
    void testExtensionsWithDefaultValueIsInvalidForAspectInVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_invalid_value_for_aspect_in_vnfControlledScaling.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_VALUE_FOR_ASPECT_IN_VNF_CONTROLLED_SCALING_FORMAT, "Aspect1", "test"));
    }

    @Test
    void testExtensionsWithDefaultValueIsInvalidForKeyInDeployableModules() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/deployableModules/invalid_vnfd_with_invalid_value_for_key_in_deployableModules.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(INVALID_VALUE_FOR_KEY_IN_DEPLOYABLE_MODULES_DEFAULTS_FORMAT, "depModule_crd1", "test"));
    }

    @Test
    void testExtensionsWithEntrySchemaNullForVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_entry_schema_null_for_vnfControlledScaling.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(ENTRY_SCHEMA_NULL_FOR_EXTENSIONS_PROPERTIES,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithEntrySchemaConstraintNullForVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/invalid_vnfd_with_entry_schema_constraint_null_for_vnfControlledScaling.yaml")
                        .toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(ENTRY_SCHEMA_CONSTRAINT_CANT_BE_BLANK_FOR_EXTENSIONS_PROPERTIES,
                                   "ericsson.datatypes.nfv.VnfInfoModifiableAttributesExtensions"));
    }

    @Test
    void testExtensionsWithEntrySchemaMissingConstraintForVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/invalid_vnfd_with_missing_constraints.yaml").toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_VNF_CONTROLLED_SCALING_ENTRY_SCHEMA_CONSTRAINTS_VALUE);
    }

    @Test
    void testExtensionsWithEntrySchemaMissingConstraintForDeployableModules() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_missing_constraints.yaml").toAbsolutePath());

        assertThatThrownBy(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_DEPLOYABLE_MODULES_ENTRY_SCHEMA_CONSTRAINTS_VALUE);
    }

    @Test
    void testExtensionsWithEntrySchemaSingleConstraintForVnfControlledScaling() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/valid_vnfd_with_single_constraints.yaml").toAbsolutePath());

        final VnfDescriptorDetails descriptorDetails = VnfdUtility.buildVnfDescriptorDetails(jsonData);

        assertThat(descriptorDetails).isNotNull();
    }

    @Test
    void testValidateInterfaceTypeNotFoundIsIgnored() {
        JSONObject parsed = VnfdUtility.validateYamlCanBeParsed(TestUtils
                                                                        .getResource("vnfd/ccsm/eric-ccsm-vnfd_orig.yaml"));

        assertDoesNotThrow(() -> VnfdUtility.buildVnfDescriptorDetails(parsed));
    }

    @Test
    void testGetAdditionalParamsFromVnfdForInstantiateOperation() throws IOException {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_test_get_additional_params.yaml"));
        String expectedAdditionalParamsString = TestUtils.readDataFromFile(
                "additionalParameters/additional_parameters_for_instantiate.json");
        JSONObject expectedAdditionalParameters = new JSONObject(expectedAdditionalParamsString);

        JSONObject additionalParams = VnfdUtility.getAdditionalParametersFromVnfd(
                vnfd, INSTANTIATE_KEY, StringUtils.EMPTY);
        assertTrue(additionalParams.similar(expectedAdditionalParameters));
    }

    @Test
    void testGetAdditionalParamsFromVnfdForChangePackageOperationTosca1Dot2() throws IOException {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_test_get_additional_params.yaml"));
        String expectedAdditionalParamsString = TestUtils.readDataFromFile(
                "additionalParameters/additional_parameters_for_change_package_1_2.json");
        JSONObject expectedAdditionalParameters = new JSONObject(expectedAdditionalParamsString);

        JSONObject additionalParams = VnfdUtility.getAdditionalParametersFromVnfd(
                vnfd, LCMOperationsEnum.CHANGE_VNFPKG.getOperation(), StringUtils.EMPTY);
        assertTrue(additionalParams.similar(expectedAdditionalParameters));
    }

    @Test
    void testGetAdditionalParamsFromVnfdForChangePackageOperationTosca1Dot3() throws IOException {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_multi_b.yaml"));

        String expectedAdditionalParamsString = TestUtils.readDataFromFile(
                "additionalParameters/additional_parameters_for_change_package_1_3.json");
        JSONObject expectedAdditionalParameters = new JSONObject(expectedAdditionalParamsString);

        JSONObject additionalParams = VnfdUtility.getAdditionalParametersFromVnfd(
                vnfd, LCMOperationsEnum.CHANGE_CURRENT_PACKAGE.getOperation(), StringUtils.EMPTY);
        assertTrue(additionalParams.similar(expectedAdditionalParameters));
    }

    @Test
    void testGetEmptyAdditionalParamsFromVnfdForChangePackageOperationTosca1Dot2() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_test_get_additional_params.yaml"));
        JSONObject additionalParams = VnfdUtility.getAdditionalParametersFromVnfd(
                vnfd, LCMOperationsEnum.CHANGE_CURRENT_PACKAGE.getOperation(), StringUtils.EMPTY);
        assertThat(additionalParams).hasToString("{}");
    }

    @Test
    void testGetEmptyAdditionalParamsFromVnfdForChangePackageOperationTosca1Dot3() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_multi_b.yaml"));

        JSONObject additionalParams = VnfdUtility.getAdditionalParametersFromVnfd(
                vnfd, LCMOperationsEnum.CHANGE_VNFPKG.getOperation(), StringUtils.EMPTY);
        assertThat(additionalParams).hasToString("{}");
    }

    @Test
    void testGetAdditionalParamsFromVnfdForInstantiateOperationWithInvalidInterfaces() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_invalid_interfaces_in_node_types.yaml"));
        JSONObject additionalParameters =
                VnfdUtility.getAdditionalParametersFromVnfd(vnfd, INSTANTIATE_KEY, StringUtils.EMPTY);
        assertThat(additionalParameters.keySet()).isEmpty();
    }

    @Test
    void testGetAdditionalParamsFromVnfdFailOnInvalidTypeInInterface() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_invalid_interfaces_type_in_node_types.yaml"));
        assertThatThrownBy(() -> VnfdUtility.getAdditionalParametersFromVnfd(vnfd, INSTANTIATE_KEY, StringUtils.EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ADDITIONAL_PARAMETERS_TYPE_KEY_MUST_BE_OF_TYPE_STRING);
    }

    @ParameterizedTest
    @MethodSource("providedAdditionalParamsData")
    void testGetAdditionalParamsFromVnfdForRollback(String testName, String vnfdPath, String expectedAdditionalParams,
                                                    String destinationDescriptorId) throws IOException {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                vnfdPath));
        String expectedAdditionalParametersString = TestUtils.readDataFromFile(
                expectedAdditionalParams);
        JSONObject expectedAdditionalParameters = new JSONObject(expectedAdditionalParametersString);

        JSONObject additionalParameters = VnfdUtility.getAdditionalParametersFromVnfd(
                vnfd, ROLLBACK, destinationDescriptorId);

        assertTrue(additionalParameters.similar(expectedAdditionalParameters));
    }

    private static Stream<Arguments> providedAdditionalParamsData() {
        return Stream.of(
                Arguments.of("RollbackWithTriggers", "vnfd/valid_vnfd_test_get_additional_params.yaml",
                             "additionalParameters/additional_parameters_for_rollback_with_triggers.json", "multi-chart-477c-aab3-2b04e6a383"),
                Arguments.of("RollbackToAnyWithTriggers", "vnfd/valid_vnfd_test_get_additional_params.yaml",
                             "additionalParameters/additional_parameters_for_rollback_to_any.json", "any-vnfd-if"),
                Arguments.of("Tosca1Dot3ForRollback", "vnfd/vnfd_tosca_1_3_multi_b.yaml",
                             "additionalParameters/additional_parameters_for_tosca_1_dot_3_rollback.json", "multi-chart-477c-aab3-2b04e6a383"),
                Arguments.of("RollbackWithoutTriggers", "vnfd/valid_vnfd_test_get_additional_params_without_triggers.yaml",
                             "additionalParameters/additional_parameters_for_rollback_without_triggers.json", "36ff67a9-0de4-48f9-97a3-4b0661670934")
        );
    }

    @Test
    void testFailGetAdditionalParamsFromVnfdForRollbackWithOutDestinationId() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_test_get_additional_params.yaml"));
        assertThatThrownBy(() -> VnfdUtility.getAdditionalParametersFromVnfd(vnfd, ROLLBACK, StringUtils.EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DESTINATION_DESCRIPTOR_ID_MUST_NOT_BE_EMPTY);
    }

    @Test
    void testGetAdditionalParamsFromVnfdUnsupportedOperationType() {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/valid_vnfd_test_get_additional_params.yaml"));
        assertThatThrownBy(() -> VnfdUtility.getAdditionalParametersFromVnfd(vnfd,
                                                                             UNSUPPORTED_OPERATION_TYPE, StringUtils.EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(format(UNKNOWN_OPERATION, UNSUPPORTED_OPERATION_TYPE));
    }

    @Test
    void testSuccessfulValidationDeployableModules() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/valid_vnfd_with_deployable_modules.yaml").toAbsolutePath());

        assertTrue(deployableModuleValidator.validate(jsonData).isValid());
    }

    @Test
    void testSuccessfulValidationDeployableModulesForRel2package() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/valid_rel2_vnfd_with_deployable_modules.yaml").toAbsolutePath());

        assertTrue(deployableModuleValidator.validate(jsonData).isValid());
    }

    @Test
    void testSuccessfulValidationDeployableModulesForRel3package() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/valid_rel3_vnfd_with_deployable_modules.yaml").toAbsolutePath());

        assertTrue(deployableModuleValidator.validate(jsonData).isValid());
    }

    @Test
    void testSuccessfulValidationOfRel4VnfdWithoutDeployableModulesFor() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/valid_rel4_without_deployable_modules.yaml").toAbsolutePath());

        assertTrue(deployableModuleValidator.validate(jsonData).isValid());
    }

    @Test
    void testFailedValidationOfVnfdWithDeployableModuleWhichNotPresentedInModifiableAttributes() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/validation/deployableModules/invalid_vnfd_with_deployable_module_which_not_presented_in_modifiable_attributes.yaml")
                                                                          .toAbsolutePath());

        assertEquals(
                VALIDATION_OF_OPTIONALITY_OF_DEPLOYABLE_MODULE_HAS_FAILED,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testSuccessValidationWithMissingDefaultParameterInExtensionsForDeployableModules() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/validation/deployableModules/valid_vnfd_with_missing_default_parameter_in_extensions_for_deployable_module.yaml")
                                                                          .toAbsolutePath());

        assertTrue(deployableModuleValidator.validate(jsonData).isValid());
    }

    @Test
    void testFailedValidationOfDeployableModulesWithMissingAssociatedArtifactsValuesVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/deployableModules/invalid_vnfd_with_deployable_modules_associated_artifacts_value_is_empty.yaml")
                        .toAbsolutePath());

        assertEquals(
                ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfDeployableModulesWithMissingAssociatedArtifactsField() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/deployableModules/invalid_vnfd_with_deployable_modules_associated_artifacts_field_is_missing.yaml")
                        .toAbsolutePath());

        assertEquals(
                ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfDeployableModulesWithNullAssociatedArtifactsVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_deployable_modules_associated_artifacts_are_null.yaml")
                        .toAbsolutePath());

        assertEquals(
                ASSOCIATED_ARTIFACTS_ARE_NULL_OR_MISSING,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfDeployableModulesWithIncorrectReferenceInAssociatedArtifactsVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/validation/deployableModules/invalid_vnfd_with_deployable_modules_with_incorrect_reference_in_associated_artifacts.yaml")
                                                                          .toAbsolutePath());

        assertEquals(
                format(VALIDATION_OF_DEPLOYABLE_MODULE_ASSOCIATED_ARTIFACTS_HAS_FAILED_FORMAT, INVALID_CHART_REFERENCE_IN_DEPLOYABLE_MODULE),
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfDeployableModulesWithEmptyNameVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_empty_deployable_module_name.yaml").toAbsolutePath());

        assertEquals(
                VALIDATION_OF_EMPTY_DEPLOYABLE_MODULE_NAME_HAS_FAILED,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfDeployableModulesWithMissingNameVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_deployable_module_name_missing.yaml").toAbsolutePath());

        assertEquals(
                VALIDATION_OF_EMPTY_DEPLOYABLE_MODULE_NAME_HAS_FAILED,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfVnfdWithIncorrectDeployableModulesRelation() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_incorrect_relation_of_deployable_modules.yaml")
                        .toAbsolutePath());

        assertEquals(
                format(VALIDATION_OF_DEPLOYABLE_MODULE_RELATION_HAS_FAILED_FORMAT, DEPLOYABLE_MODULE_CRD_CHART),
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationPropertiesDoNotExistInDeployableModules() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_properties_are_missing_in_deployable_module.yaml")
                        .toAbsolutePath());

        assertEquals(
                PROPERTIES_IN_DEPLOYABLE_MODULE_ARE_MISSING,
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationOfYamlWithDuplicateDeployableModulesKeys() {
        final Path vnfdPath = TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_duplicate_deployable_module.yaml")
                .toAbsolutePath();

        assertThatThrownBy(() -> VnfdUtility.validateYamlCanBeParsed(
                vnfdPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE);
    }

    @Test
    void testFailedValidationOfYamlWithDuplicateDeployableModulesInExtensions() {
        final Path vnfdPath = TestUtils.getResource("vnfd/validation/deployableModules/invalid_vnfd_with_dm_modules_in_extensions.yaml")
                .toAbsolutePath();

        assertThatThrownBy(() -> VnfdUtility.validateYamlCanBeParsed(
                vnfdPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(UNABLE_TO_PARSE_YAML_FILE_ERROR_MESSAGE);
    }

    @Test
    void testFailedValidationWithDuplicatedNamesAndChartsOfDeployableModules() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/deployableModules/invalid_vnfd_with_duplicated_names_and_charts_in_deployable_modules.yaml")
                        .toAbsolutePath());

        assertEquals(
                format(VALIDATION_OF_DEPLOYABLE_MODULES_FOR_DUPLICATES_FORMAT, DEPLOYABLE_MODULE_WITH_DUPLICATED_NAMES,
                       DEPLOYABLE_MODULE_WITH_DUPLICATED_CHARTS),
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testFailedValidationWithDuplicatedNamesOfDeployableModules() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/deployableModules/invalid_vnfd_with_deployable_modules_with_duplicated_names.yaml")
                        .toAbsolutePath());

        assertEquals(
                format(VALIDATION_OF_DEPLOYABLE_MODULES_FOR_NAMES_DUPLICATES_FORMAT, DEPLOYABLE_MODULE_WITH_DUPLICATED_NAMES),
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    private static void validateVnfLcmHelmPackages(VnfmLcmInterface vnfmLcmInterface, String helmPackage1, String helmPackage3) {
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).isNotNull();
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).hasSize(3);
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages().get(0).getId()).isEqualTo(helmPackage1);
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages().get(1).getId()).isEqualTo("helm_package2");
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages().get(2).getId()).isEqualTo(helmPackage3);
    }

    @Test
    void testFailedValidationWithDuplicatedChartsInDeployableModules() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/deployableModules/invalid_vnfd_with_duplicated_associated_artifacts_in_deployable_modules.yaml")
                        .toAbsolutePath());

        assertEquals(
                format(VALIDATION_OF_DEPLOYABLE_MODULES_FOR_ASSOCIATED_ARTIFACTS_DUPLICATES_FORMAT, DEPLOYABLE_MODULE_WITH_DUPLICATED_CHARTS),
                deployableModuleValidator.validate(jsonData).getErrorMessage()
        );
    }

    @Test
    void testGetInterfaceFromNodeTemplate() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/validation/deployableModules/valid_vnfd_with_missing_default_parameter_in_extensions_for_deployable_module.yaml")
                .toAbsolutePath());
        final JSONObject interfaceFromVnfd = getInterfaceFromNodeTemplate(jsonData);

        assertThat(interfaceFromVnfd).isNotNull();
        assertThat(interfaceFromVnfd.getJSONObject(INSTANTIATE_KEY)).isNotNull();
    }

    @Test
    void testGetVnflcmInterfaceFromVnfd() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/validation/deployableModules/valid_vnfd_with_missing_default_parameter_in_extensions_for_deployable_module.yaml")
                .toAbsolutePath());
        JSONObject vnflcmNodeTemplate = getInterfaceFromNodeTemplate(jsonData);
        final JSONObject vnflcmInterface = getVnflcmInterfaceAsJsonObject(vnflcmNodeTemplate);

        assertThat(vnflcmInterface).isNotNull();
        assertThat(vnflcmInterface.getJSONObject(INSTANTIATE_KEY)).isNotNull();
        assertThat(vnflcmInterface.getJSONObject("change_package")).isNotNull();
    }

    @Test
    void testGetAllHelmArtifactsKeyFromInterface() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        "vnfd/validation/deployableModules/valid_vnfd_with_missing_default_parameter_in_extensions_for_deployable_module.yaml")
                .toAbsolutePath());
        JSONObject vnflcmNodeTemplate = getInterfaceFromNodeTemplate(jsonData);
        final JSONObject vnflcmInterface = getVnflcmInterfaceAsJsonObject(vnflcmNodeTemplate);

        final List<HelmPackage> packagesFromInstantiate = getAllHelmPackagesFromLcmOperation(vnflcmInterface, INSTANTIATE_KEY);


        assertThat(packagesFromInstantiate).hasSize(5);
        assertThat(packagesFromInstantiate.get(0).getId()).isEqualTo("crd_package1");
        assertThat(packagesFromInstantiate.get(4).getId()).isEqualTo("crd_package3");
    }

    @Test
    void testGetAllHelmArtifactsKeyFromInterfaceWithoutNodeTemplate() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        VnfdUtils.VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE)
                .toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        assertThat(vnflcmInterfaces).isNotNull().hasSize(4);
        vnflcmInterfaces.stream()
                .filter(i -> !LCMOperationsEnum.INSTANTIATE.getOperation().equals(i.getType().getLabel()))
                .forEach(vnfmLcmInterface -> {
                    assertThat(vnfmLcmInterface.getInputs()).isNull();
                });
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).isNotNull();
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).hasSize(2);
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages().get(0).getId()).isEqualTo("helm_package1");
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages().get(1).getId()).isEqualTo("helm_package2");
    }

    @Test
    void testGetAllHelmArtifactsKeyFromInterfaceWithoutNodeTemplateHelmPackages() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        VnfdUtils.VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE_HELM_PACKAGES)
                .toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        assertThat(vnflcmInterfaces).isNotNull().hasSize(4);
        vnflcmInterfaces.stream()
                .filter(i -> !LCMOperationsEnum.INSTANTIATE.getOperation().equals(i.getType().getLabel())
                && !LCMOperationsEnum.CHANGE_VNFPKG.getOperation().equals(i.getType().getLabel()))
                .forEach(vnfmLcmInterface -> {
                    assertThat(vnfmLcmInterface.getInputs()).isNull();
                });
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).isNotNull();
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).hasSize(0);

        VnfmLcmInterface vnfmLcmInterfaceCcvp = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.CHANGE_VNFPKG);
        assertThat(vnfmLcmInterfaceCcvp.getInputs().getHelmPackages()).isNotNull();
        assertThat(vnfmLcmInterfaceCcvp.getInputs().getHelmPackages()).hasSize(0);
    }

    @Test
    void testGetAllHelmArtifactsKeyFromInterfaceWithoutNodeTemplateAndTypes() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        VnfdUtils.VNFD_INTERFACE_PARSING_VALID_VNFD_WITHOUT_NODE_TEMPLATE_AND_NOT_DEFINED_HELM_PACKAGES)
                .toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).isEmpty();


        VnfmLcmInterface vnfmLcmInterfaceTerminate = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.TERMINATE);
        assertThat(vnfmLcmInterfaceTerminate.getInputs()).isNull();
    }

    @Test
    void testGetAllHelmArtifactsKeyFromInterfaceWithNodeTemplateAndTypes() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        VnfdUtils.VNFD_VNFD_WITH_VIRTUAL_CP_YAML)
                .toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);

        validateVnfLcmHelmPackages(vnfmLcmInterface, "helm_package1", "helm_package3");
    }

    @Test
    void testGetAllHelmArtifactsKeyFromInterfaceMixed() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                        VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_WITH_MIXED_HELM_ARTIFACTS_IN_INTERFACES)
                .toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        assertThat(vnflcmInterfaces).isNotNull();
        assertThat(vnflcmInterfaces).hasSize(3);
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        validateVnfLcmHelmPackages(vnfmLcmInterface, "helm_package1", "helm_package3");

        VnfmLcmInterface vnfmLcmInterfaceScale = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.SCALE);
        validateVnfLcmHelmPackages(vnfmLcmInterfaceScale, "helm_package3", "helm_package1");

        VnfmLcmInterface vnfmLcmInterfaceTerminate = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.TERMINATE);
        assertThat(vnfmLcmInterfaceTerminate.getInputs()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_OUT_OPERATIONS,
        VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_EMPTY_INPUTS_AND_HELM_PACKAGES,
        VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_OUT_INSTANTIATE_CCVP})
    void testGetAllHelmArtifactsKeyFromInterfaceEmptySomeSectionsIn1Dot3(String vnfd) {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfd).toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        assertThat(vnflcmInterfaces).isNotNull();
        assertThat(vnflcmInterfaces).hasSize(5);

        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        assertThat(vnfmLcmInterface.getInputs()).isNotNull();
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).hasSize(0);

        VnfmLcmInterface vnfmLcmInterfaceCcvp = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.CHANGE_CURRENT_PACKAGE);
        assertThat(vnfmLcmInterfaceCcvp.getInputs()).isNotNull();
        assertThat(vnfmLcmInterfaceCcvp.getInputs().getHelmPackages()).hasSize(0);

        VnfmLcmInterface vnfmLcmInterfaceTerminate = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.TERMINATE);
        assertThat(vnfmLcmInterfaceTerminate.getInputs()).isNull();

        VnfmLcmInterface vnfmLcmInterfaceHeal = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.HEAL);
        assertThat(vnfmLcmInterfaceHeal.getInputs()).isNotNull();

        assertThat(vnfmLcmInterfaceHeal.getInputs().getHelmPackages()).hasSize(0);
        VnfmLcmInterface vnfmLcmInterfaceScale = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.SCALE);
        assertThat(vnfmLcmInterfaceScale.getInputs()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_2_WITHOUT_VNFLCM_IN_NODE_TYPES,
        VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_EMPTY_VNFLCM_IN_NODE_TYPES,
        VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_EMPTY_INTERFACE_NODE_TEMPLATE,
        VnfdUtils.VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_OUT_INTERFACE_NODE_TEMPLATE})
    void testGetAllHelmArtifactsKeyFromInterfaceEmptySomeSectionsIn1Dot2(String vnfd) {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfd).toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        assertThat(vnflcmInterfaces).isNotNull();
        assertThat(vnflcmInterfaces).hasSize(3);
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        assertThat(vnfmLcmInterface.getInputs()).isNotNull();
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).hasSize(0);

        VnfmLcmInterface vnfmLcmInterfaceCcvp = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.CHANGE_VNFPKG);
        assertThat(vnfmLcmInterfaceCcvp.getInputs()).isNotNull();
        assertThat(vnfmLcmInterfaceCcvp.getInputs().getHelmPackages()).hasSize(0);

        VnfmLcmInterface vnfmLcmInterfaceTerminate = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.TERMINATE);
        assertThat(vnfmLcmInterfaceTerminate.getInputs()).isNotNull();
        assertThat(vnfmLcmInterfaceTerminate.getInputs().getHelmPackages()).hasSize(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_NODE_TEMPLATE,
        VNFD_INTERFACE_PARSING_VNFD_1_2_WITH_NODE_TEMPLATE})
    void testGetAllHelmArtifactsKeyFromInterfaceBothTosca(String vnfd) {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(vnfd).toAbsolutePath());
        List<VnfmLcmInterface> vnflcmInterfaces = getVnflcmInterfaces(jsonData);
        assertThat(vnflcmInterfaces).isNotNull();
        assertThat(vnflcmInterfaces).hasSize(5);
        VnfmLcmInterface vnfmLcmInterface = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.INSTANTIATE);
        assertThat(vnfmLcmInterface.getInputs()).isNotNull();
        assertThat(vnfmLcmInterface.getInputs().getHelmPackages()).hasSize(5);

        if (vnfd.equals(VNFD_INTERFACE_PARSING_VNFD_1_3_WITH_NODE_TEMPLATE)) {
            VnfmLcmInterface vnfmLcmInterfaceCcvp = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.CHANGE_CURRENT_PACKAGE);
            assertThat(vnfmLcmInterfaceCcvp.getInputs()).isNotNull();
            assertThat(vnfmLcmInterfaceCcvp.getInputs().getHelmPackages()).hasSize(5);
        } else {
            VnfmLcmInterface vnfmLcmInterfaceCcvp = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.CHANGE_VNFPKG);
            assertThat(vnfmLcmInterfaceCcvp.getInputs()).isNotNull();
            assertThat(vnfmLcmInterfaceCcvp.getInputs().getHelmPackages()).hasSize(5);
        }

        VnfmLcmInterface vnfmLcmInterfaceTerminate = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.TERMINATE);
        assertThat(vnfmLcmInterfaceTerminate.getInputs()).isNull();
        VnfmLcmInterface vnfmLcmInterfaceHeal = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.HEAL);
        assertThat(vnfmLcmInterfaceHeal.getInputs()).isNotNull();
        assertThat(vnfmLcmInterfaceHeal.getInputs().getHelmPackages()).hasSize(0);
        VnfmLcmInterface vnfmLcmInterfaceScale = getVnfLcmInterfaceByOperation(vnflcmInterfaces, LCMOperationsEnum.SCALE);
        assertThat(vnfmLcmInterfaceScale.getInputs()).isNull();
    }

    @Test
    void testSuccessfulValidationWithPartOfExtensionsOfStringType() {
        JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(
                TestUtils.getResource(
                                "vnfd/validation/valid_vnfd_with_extensions_with_different_types.yaml")
                        .toAbsolutePath());

        assertDoesNotThrow(() -> VnfdUtility.buildVnfDescriptorDetails(jsonData));
    }

    private static VnfmLcmInterface getVnfLcmInterfaceByOperation(List<VnfmLcmInterface> vnflcmInterfaces, LCMOperationsEnum instantiate) {
        return vnflcmInterfaces.stream()
                .filter(i -> instantiate.getOperation().equals(i.getType().getLabel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item should not be empty"));
    }
}
