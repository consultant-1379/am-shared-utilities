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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import static com.ericsson.am.shared.vnfd.NestedMapUtility.castToNestedMap;
import static com.ericsson.am.shared.vnfd.NestedMapUtility.replaceValues;
import static com.ericsson.am.shared.vnfd.NestedMapUtility.replaceValuesAfterKey;
import static com.ericsson.am.shared.vnfd.utils.Constants.DEFAULT_KEY;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class NestedMapUtilityTest {

    public static final String TEST_NEW_VALUE = "new-value";
    public static final Object JSON_OBJECT_NULL = JSONObject.NULL;

    @Test
    void shouldReplaceValuesInNOTNestedMap() {
        String expectedKeyToReplace = "key1";
        String expectedValueToReplace = "value1";
        Map<String, Object> testMap = createDefaultMap();

        Map<String, Object> actualMap = replaceValues(testMap, expectedValueToReplace, TEST_NEW_VALUE);

        assertEquals(TEST_NEW_VALUE, actualMap.get(expectedKeyToReplace));
    }

    @Test
    void shouldReplaceOnlyEqualValues() {
        String expectedKeyToLeave = "key2";
        String expectedValueToLeave = "value2";
        Map<String, Object> testMap = createDefaultMap();

        Map<String, Object> actualMap = replaceValues(testMap, "value1", TEST_NEW_VALUE);

        assertEquals(expectedValueToLeave, actualMap.get(expectedKeyToLeave));
    }

    @Test
    void shouldReplaceValuesInNestedMap() {
        Map<String, Object> testNestedMap = new HashMap<>();
        testNestedMap.put("lvl-1-key-1", "lvl-1-value-1");
        testNestedMap.put("lvl-2-key-2", createDefaultMap());

        Map<String, Object> actualMap = replaceValues(testNestedMap, "value1", TEST_NEW_VALUE);

        Map<String, Object> innerMap = castToNestedMap(actualMap.get("lvl-2-key-2"));
        assertEquals(TEST_NEW_VALUE, innerMap.get("key1"));
    }

    @Test
    void shouldSupportNUllValues() {
        String expectedKeyToReplace = "key1";
        Map<String, Object> testMap = createDefaultMap();
        testMap.put(expectedKeyToReplace, null);

        Map<String, Object> actualMap = replaceValues(testMap, null, TEST_NEW_VALUE);

        assertEquals(TEST_NEW_VALUE, actualMap.get(expectedKeyToReplace));
    }

    @Test
    void shouldReplaceNullValueWhenDefaultKeyAppear() {
        Map<String, Object> mapWithDefaultKey = new HashMap<>();
        mapWithDefaultKey.put("default", createNullContainingMap());
        mapWithDefaultKey.put("test", "test-value");

        Map<String, Object> actual = replaceValuesAfterKey(mapWithDefaultKey, DEFAULT_KEY, null, JSON_OBJECT_NULL);

        Map<String, Object> actualDefaultValuesMap = castToNestedMap(actual.get("default"));
        assertEquals(JSON_OBJECT_NULL, actualDefaultValuesMap.get("key1"));
        assertEquals(JSON_OBJECT_NULL, actualDefaultValuesMap.get("key2"));
    }

    @Test
    void shouldNotReplaceNullValueWhenNoDefaultKey() {
        Map<String, Object> mapWithDefaultKey = new HashMap<>();
        mapWithDefaultKey.put("not-default", createNullContainingMap());

        Map<String, Object> actual = replaceValuesAfterKey(mapWithDefaultKey, DEFAULT_KEY, null, JSON_OBJECT_NULL);
        Map<String, Object> actualDefaultValuesMap = castToNestedMap(actual.get("not-default"));
        assertNull(actualDefaultValuesMap.get("key1"));
        assertNull(actualDefaultValuesMap.get("key2"));
    }

    private Map<String, Object> createDefaultMap() {
        Map<String, Object> mapWithNulls = new HashMap<>();
        mapWithNulls.put("key1", "value1");
        mapWithNulls.put("key2", "value2");
        return mapWithNulls;
    }

    private Map<String, Object> createNullContainingMap() {
        Map<String, Object> mapWithNulls = new HashMap<>();
        mapWithNulls.put("key3", null);
        mapWithNulls.put("key4", null);
        return mapWithNulls;
    }

}
