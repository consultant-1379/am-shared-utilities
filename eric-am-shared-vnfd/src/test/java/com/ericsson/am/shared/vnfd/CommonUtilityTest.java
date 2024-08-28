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
import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.am.shared.vnfd.utils.Constants.PROPERTIES_KEY;
import static com.ericsson.am.shared.vnfd.utils.VnfdUtils.NODE_TYPE_NAME;
import static com.ericsson.am.shared.vnfd.utils.VnfdUtils.VALID_VNFD_FILE;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.utils.TestUtils;

class CommonUtilityTest {

    @Test
    void testGetPojo() {
        JSONObject propertiesJsonObject = null;
        try {
            final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE).toAbsolutePath());
            propertiesJsonObject = jsonData.getJSONObject(NODE_TYPES_KEY).getJSONObject(NODE_TYPE_NAME).getJSONObject(PROPERTIES_KEY);
        } catch (final JSONException ex) {
            fail(StringUtils.EMPTY);
        }

        final NodeProperties nodeProperties = CommonUtility.getPojo(propertiesJsonObject, NodeProperties.class);
        assertThat(nodeProperties.getDescriptorId()).isNotNull();
    }

    @Test
    void testGetPojoWithJsonDataNull() {
        try {
            CommonUtility.getPojo(null, NodeProperties.class);
            Assertions.fail("Data shouldn't be null");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void testGetPojoWithPojoNotPresent() {
        final JSONObject jsonData = VnfdUtility.validateYamlCanBeParsed(TestUtils.getResource(VALID_VNFD_FILE).toAbsolutePath());
        final NodeProperties nodeProperties = CommonUtility.getPojo(jsonData, NodeProperties.class);
        assertThat(nodeProperties.getDescriptorId()).isNull();
    }

}