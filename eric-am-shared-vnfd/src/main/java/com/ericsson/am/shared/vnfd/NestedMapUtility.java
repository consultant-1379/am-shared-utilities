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


import java.util.Map;

/**
 * Utility class for maps that can have several levels of nesting,
 * that is, store more maps as values.
 */
public final class NestedMapUtility {

    private NestedMapUtility() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method to convert an object to a map without using warning suppression.
     * Also indicates that the map can be nested.
     * @param mapObject - map object to cast.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> castToNestedMap(Object mapObject) {
        return (Map<String, Object>) mapObject;
    }

    /**
     * Removes old values in the nested map and substitutes new ones in them.
     * Method replaces the value not only in the map that will be returned, but
     * also in the map that is passed by the parameter.
     * @param nestedMap - map with values to replace.
     * @param oldValue - value to replace.
     * @param newValue - new value.
     * @return - map with values that were replaced.
     */
    public static Map<String, Object> replaceValues(Map<String, Object> nestedMap, Object oldValue, Object newValue) {
        for (Map.Entry<String, Object> entry: nestedMap.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue == null && oldValue == null
                    || oldValue != null && oldValue.equals(entryValue)) {
                entry.setValue(newValue);
            } else if (entryValue instanceof Map) {
                replaceValues(castToNestedMap(entryValue), oldValue, newValue);
            }
        }
        return nestedMap;
    }

    /**
     * Replaces the values in the nested map after the specified key. Method replaces
     * the value not only in the map that will be returned, but also in the map that
     * is passed by the parameter.
     * @param map - map with values to replace.
     * @param key - key after which the values will be replaced.
     * @param oldValue - value to replace.
     * @param newValue -  new value.
     * @return - map with values that were replaced.
     */
    public static Map<String, Object> replaceValuesAfterKey(Map<String, Object> map, Object key,
                                                            Object oldValue, Object newValue)  {
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            Object entryValue = entry.getValue();
            boolean isMap = entryValue instanceof Map;

            if (key.equals(entry.getKey()) && isMap) {
                replaceValues(castToNestedMap(entryValue), oldValue, newValue);
            } else if (isMap) {
                replaceValuesAfterKey(castToNestedMap(entryValue), key, oldValue, newValue);
            }
        }
        return map;
    }
}
