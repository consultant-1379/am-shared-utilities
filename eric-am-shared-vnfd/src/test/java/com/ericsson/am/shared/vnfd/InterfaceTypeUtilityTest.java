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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.ericsson.am.shared.vnfd.InterfaceTypeUtility.getInterfaceTypeFromVnfd;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ericsson.am.shared.vnfd.model.DataType;
import com.ericsson.am.shared.vnfd.model.InterfaceType;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class InterfaceTypeUtilityTest {

    @ParameterizedTest
    @MethodSource("provideFilePaths")
    void testGetInterfaceTypesToscaShouldReturnExpectedAdditionalParams(String testName, String  vnfdObjectPath,
                                                                                String additionalParamPath) throws IOException {
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                vnfdObjectPath));
        String expectedAdditionalParamsString = TestUtils.readDataFromFile(
                additionalParamPath);
        JSONObject expectedAdditionalParameters = new JSONObject(expectedAdditionalParamsString);

        Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfdObject);
        Map<String, InterfaceType> interfaceTypeFromVnfd = getInterfaceTypeFromVnfd(vnfdObject, allDataType);
        JSONObject actualInterfaceTypes = new JSONObject(interfaceTypeFromVnfd);
        assertTrue(actualInterfaceTypes.similar(expectedAdditionalParameters));
    }

    private static Stream<Arguments> provideFilePaths() {
        return Stream.of(
                Arguments.of("Tosca1Dot2WithOutOperations", "vnfd/vnfd_tosca_1_2_25_multi_b.yaml", "interfaceTypes/interface_types.json"),
                Arguments.of("Tosca1Dot3WithOperations", "vnfd/vnfd_tosca_1_3_multi_b.yaml", "interfaceTypes/interface_types_1_3.json"),
                Arguments.of("Tosca1Dot2WithOperations", "vnfd/vnfd_tosca_1_2_single_c.yaml", "interfaceTypes/interface_types_1_2.json")
        );
    }

    @Test
    void testGetNotDefinedInterfaceTypesTosca1Dot2() {
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/vnfd_tosca_1_3_single_b.yaml"));
        Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfdObject);
        Map<String, InterfaceType> interfaceTypeFromVnfd = getInterfaceTypeFromVnfd(vnfdObject, allDataType);
        assertThat(interfaceTypeFromVnfd).isEmpty();
    }
    @Test
    void testGetNotDefinedDataTypesInterfaceTypesTosca1Dot2() {
        JSONObject vnfdObject = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(
                "vnfd/invalid_vnfd_without_data_type_definition_in_rollback.yaml"));
        Map<String, DataType> allDataType = DataTypeUtility.buildDataTypesFromVnfd(vnfdObject);
        assertThatThrownBy(() -> getInterfaceTypeFromVnfd(vnfdObject, allDataType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data type not defined for MyCompany.datatypes.nfv.VnfChangeToVersion1AdditionalParameters");
    }
}