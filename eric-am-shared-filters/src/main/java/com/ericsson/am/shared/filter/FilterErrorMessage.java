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
package com.ericsson.am.shared.filter;

public final class FilterErrorMessage {

    public static final String PARAMETER_MAPPING_NOT_PROVIDED_ERROR_MESSAGE = "Rest to entity mapping not provided";
    public static final String OPERATION_NOT_SUPPORTED_FOR_KEY_ERROR_MESSAGE = "Operation %s not supported for " +
            "parameter %s";
    public static final String FILTER_NOT_SUPPORTED_ERROR_MESSAGE = "Filter %s not supported";
    public static final String INVALID_OPERATION_ERROR_MESSAGE = "Invalid operation provided %s";
    public static final String INVALID_NUMBER_VALUE_ERROR_MESSAGE = "Invalid number value provided %s";
    public static final String INVALID_DATE_VALUE_ERROR_MESSAGE = "Invalid date format provided %s, Provide " +
            "date in yyyy-MM-ddTHH:mm:ss format eg. 2019-07-05T16:58:00";
    public static final String INVALID_BOOLEAN_VALUE_ERROR_MESSAGE = "Invalid boolean value provided %s";
    public static final String INVALID_ENUMERATION_VALUE_ERROR_MESSAGE = "Invalid value provided for type %s, " +
            "valid values are %s";
    public static final String JPA_IMPLEMENTATION_NOT_PROVIDED_ERROR_MESSAGE = "jpa implementation not provided for " +
            "the entity";

    private FilterErrorMessage() {
    }
}
