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
package com.ericsson.am.shared.vnfd.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.utils.TestUtils;

class OperationDetailTest {

    @Test
    void testOperationDetailShouldBeMapping() throws IOException {
        List<OperationDetail> operationDetails = List.of(
                new OperationDetail.Builder()
                        .operationName("name_1")
                        .supported(true)
                        .build(),
                new OperationDetail.Builder()
                        .operationName("name_2")
                        .supported(true)
                        .build()
        );
        String operationDetailsStr =
                "[{\"operationName\":\"name_1\",\"supported\":true,\"errorMessage\":null}," +
                        "{\"operationName\":\"name_2\",\"supported\":true,\"errorMessage\":null}]";
        String convertedListToJson = TestUtils.getObjectMapper().writeValueAsString(operationDetails);
        assertEquals(operationDetailsStr, convertedListToJson);

        var convertedJsonToList = TestUtils.readListFromJson(operationDetailsStr, OperationDetail.class);
        assertThat(convertedJsonToList).hasSize(2);
    }
}

