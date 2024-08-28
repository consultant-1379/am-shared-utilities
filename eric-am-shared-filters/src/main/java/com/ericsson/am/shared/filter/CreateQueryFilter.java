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

import org.apache.commons.lang3.EnumUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.ordinalIndexOf;

import com.ericsson.am.shared.filter.model.DataType;
import com.ericsson.am.shared.filter.model.FilterExpressionMultiValue;
import com.ericsson.am.shared.filter.model.FilterExpressionOneValue;
import com.ericsson.am.shared.filter.model.MappingData;
import com.ericsson.am.shared.filter.model.OperandMultiValue;
import com.ericsson.am.shared.filter.model.OperandOneValue;
import com.ericsson.am.shared.filter.model.SpecificationMultiValue;
import com.ericsson.am.shared.filter.model.SpecificationOneValue;

public abstract class CreateQueryFilter<T, L extends JpaRepository<T, ?> & JpaSpecificationExecutor<T>> {

    private static final String FILTER_VALUE_SEPARATOR = ",";
    private static final String FILTER_SEPARATOR = ");(";
    private static final Pattern NUMBER_EXPRESSION = Pattern.compile("\\d+(\\.\\d+)?");
    private static final Map<String, List<DataType>> OPERATION_TO_DATA_TYPE_MAPPING = new HashMap<>();
    private final Map<String, MappingData> mapping;

    protected final L jpaRepository;

    protected CreateQueryFilter(Map<String, MappingData> mapping, L jpaRepository) {
        if (mapping.isEmpty()) {
            throw new IllegalArgumentException(FilterErrorMessage.PARAMETER_MAPPING_NOT_PROVIDED_ERROR_MESSAGE);
        }
        if (jpaRepository == null) {
            throw new IllegalArgumentException(FilterErrorMessage.JPA_IMPLEMENTATION_NOT_PROVIDED_ERROR_MESSAGE);
        }
        this.mapping = mapping;
        this.jpaRepository = jpaRepository;
    }

    static {
        List<DataType> eqAndNegSupportedDataType = new ArrayList<>();
        eqAndNegSupportedDataType.add(DataType.BOOLEAN);
        eqAndNegSupportedDataType.add(DataType.ENUMERATION);
        eqAndNegSupportedDataType.add(DataType.NUMBER);
        eqAndNegSupportedDataType.add(DataType.STRING);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandOneValue.EQUAL.getFilterOperation(), eqAndNegSupportedDataType);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandOneValue.NOT_EQUAL.getFilterOperation(), eqAndNegSupportedDataType);

        List<DataType> inAndNinSupportedDataType = new ArrayList<>();
        inAndNinSupportedDataType.add(DataType.STRING);
        inAndNinSupportedDataType.add(DataType.NUMBER);
        inAndNinSupportedDataType.add(DataType.ENUMERATION);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandMultiValue.IN.getFilterOperation(), inAndNinSupportedDataType);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandMultiValue.NOT_IN.getFilterOperation(), inAndNinSupportedDataType);

        List<DataType> gtGteLtAndLteSupportedDataType = new ArrayList<>();
        gtGteLtAndLteSupportedDataType.add(DataType.STRING);
        gtGteLtAndLteSupportedDataType.add(DataType.NUMBER);
        gtGteLtAndLteSupportedDataType.add(DataType.DATE);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandOneValue.GREATER_THAN.getFilterOperation(), gtGteLtAndLteSupportedDataType);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandOneValue.GREATER_THAN_EQUAL.getFilterOperation(),
                gtGteLtAndLteSupportedDataType);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandOneValue.LESS_THAN.getFilterOperation(), gtGteLtAndLteSupportedDataType);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandOneValue.LESS_THAN_EQUAL.getFilterOperation(),
                gtGteLtAndLteSupportedDataType);

        List<DataType> contAndNcontSupportedDataType = new ArrayList<>();
        contAndNcontSupportedDataType.add(DataType.STRING);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandMultiValue.CONTAINS.getFilterOperation(), contAndNcontSupportedDataType);
        OPERATION_TO_DATA_TYPE_MAPPING.put(OperandMultiValue.NOT_CONTAINS.getFilterOperation(), contAndNcontSupportedDataType);
    }

    public Map<String, MappingData> getMapping() {
        return mapping;
    }

    public abstract FilterExpressionOneValue<String> createFilterExpressionOneValue(String key,
                                                                                    String value,
                                                                                    String operand);

    public abstract FilterExpressionMultiValue<String> createFilterExpressionMultiValue(String key,
                                                                                        List<String> value,
                                                                                        String operand);

    /**
     * This method is used to get the entities matching the filters.
     *
     * @param filters string containing all filter values
     * @return List of entities satisfying the condition
     */
    public List<T> getAllWithFilter(String filters) {
        return getAllWithFilter(filters, Sort.unsorted());
    }

    /**
     * This method is used to get the entities matching the filters sorted accordingly.
     *
     * @param filters string containing all filter values
     * @param sort    a sorting specification defining a columns and direction a data should be sorted by
     * @return List of entities satisfying the condition
     */
    public List<T> getAllWithFilter(String filters, Sort sort) {
        Specification<T> allSpecification = getSpecification(filters);
        if (allSpecification != null) {
            return jpaRepository.findAll(allSpecification, sort);
        }
        return emptyList();
    }

    /**
     * This method is used to get requested subset of the entities matching the filters.
     * Entities may be sorted according to a {@link Sort} passed as part of {@code pageable}
     *
     * @param filter
     * @param pageable a {@link Pageable} instance defining a subset of entities returned
     *                 with optional sorting specification
     * @return List of entities satisfying the condition
     */
    public Page<T> getPageWithFilter(String filter, Pageable pageable) {
        Specification<T> allSpecification = getSpecification(filter);
        return jpaRepository.findAll(allSpecification, pageable);
    }

    /**
     * This method is used to build {@link Specification} object by a given filters
     *
     * @param filters string containing all filter values
     * @return built {@link Specification} object
     */
    public Specification<T> getSpecification(final String filters) {
        List<String> singleValueFilter;
        List<String> multiValueFilter;
        if (filters == null || filters.isEmpty()) { // Allow customized specification with empty filters
            singleValueFilter = emptyList();
            multiValueFilter = emptyList();
        } else {
            validateStartAndEndOfFilterString(filters);
            String strippedFilter = filters.substring(1, filters.length() - 1);
            singleValueFilter = getSingleValueFilter(strippedFilter);
            multiValueFilter = getMultiValueFilter(strippedFilter);
        }
        return createSpecification(createSingleValueFilter(singleValueFilter, false),
                createMultiValueFilter(multiValueFilter, false));
    }

    /**
     * Validates and returns the filter string that are present
     *
     * @param filters string containing all filter
     * @return String filter string containing all the filters that are present
     */
    @SuppressWarnings("squid:S4248")
    public String filterPresent(String filters) {
        validateStartAndEndOfFilterString(filters);
        String strippedFilter = filters.substring(1, filters.length() - 1);
        StringBuilder filterPresent = new StringBuilder();
        filterPresent.append("(");
        //checks if the filter provided is multiple or single
        if (strippedFilter.contains(FILTER_SEPARATOR)) {
            String[] allFilters = strippedFilter.split(Pattern.quote(FILTER_SEPARATOR));
            for (String tempFilter : allFilters) {
                validFilterString(tempFilter);
                //Checks if filter is present in the mapping
                if (mapping.get(tempFilter.split(FILTER_VALUE_SEPARATOR)[1]) != null) {
                    appendTheFilterString(filterPresent, tempFilter);
                }
            }

        } else {
            validFilterString(strippedFilter);
            if (mapping.get(strippedFilter.split(FILTER_VALUE_SEPARATOR)[1]) != null) {
                filterPresent.append(strippedFilter);
            }
        }
        filterPresent.append(")");
        if ("()".equals(filterPresent.toString())) {
            return "";
        }
        return filterPresent.toString();
    }

    /**
     * Validates and returns the filter string that are not present
     *
     * @param filters sting containing filters that are not supported
     * @return String filter string containing all the filters that are not present
     */
    @SuppressWarnings("squid:S4248")
    public String filterNotPresent(String filters) {
        validateStartAndEndOfFilterString(filters);
        String strippedFilter = filters.substring(1, filters.length() - 1);
        StringBuilder filterNotPresent = new StringBuilder();
        //checks if the filter provided is multiple or single
        filterNotPresent.append("(");
        if (strippedFilter.contains(FILTER_SEPARATOR)) {
            String[] allFilters = strippedFilter.split(Pattern.quote(FILTER_SEPARATOR));
            for (String tempFilter : allFilters) {
                validFilterString(tempFilter);
                //Checks if filter is present in the mapping
                if (mapping.get(tempFilter.split(FILTER_VALUE_SEPARATOR)[1]) == null) {
                    appendTheFilterString(filterNotPresent, tempFilter);
                }
            }

        } else {
            validFilterString(strippedFilter);
            if (mapping.get(strippedFilter.split(FILTER_VALUE_SEPARATOR)[1]) == null) {
                filterNotPresent.append(strippedFilter);
            }
        }
        filterNotPresent.append(")");
        if ("()".equals(filterNotPresent.toString())) {
            return "";
        }
        return filterNotPresent.toString();
    }

    private static void appendTheFilterString(StringBuilder filterAppender, String filter) {
        //Appends the filter separator ";" in the return filter string
        if (!"(".equals(filterAppender.toString())) {
            filterAppender.append(FILTER_SEPARATOR);
        }
        filterAppender.append(filter);
    }

    /**
     * validates the filter if it contains ',' as a separator  and after the split it should be having more than or
     * equal to three elements. This validation is due to ETSI filter structure.
     *
     * @param filter
     */
    @SuppressWarnings("squid:S4248")
    private static void validFilterString(String filter) {
        if (!filter.contains(FILTER_VALUE_SEPARATOR) || filter.split(FILTER_VALUE_SEPARATOR).length < 3) {
            throw new IllegalArgumentException("Invalid filter value provided " + filter);
        }
    }

    private static void validateStartAndEndOfFilterString(String filter) {
        if (!filter.startsWith("(") || !filter.endsWith(")")) {
            throw new IllegalArgumentException("Invalid filter value provided filter should be of format (filter) or " +
                    "(filter1);(filter2)");
        }
    }

    /**
     * Creates a filter list if single filter is provided.
     *
     * @param strippedFilter
     * @param skipValidation, skips the validation from the mapping if set to true
     * @return List<FilterExpressionOneValue>
     */
    @SuppressWarnings("squid:S4248")
    public List<FilterExpressionOneValue<String>> createSingleValueFilter(List<String> strippedFilter,
                                                                          boolean skipValidation) {
        List<FilterExpressionOneValue<String>> allFilterExpressionOne = new ArrayList<>();
        for (String filter : strippedFilter) {
            allFilterExpressionOne.add(createFilterExpressionOne(filter, skipValidation));
        }
        return allFilterExpressionOne;
    }

    public static List<String> getSingleValueFilter(String strippedFilter) {
        List<String> singleValueFilters = new ArrayList<>();
        if (strippedFilter.contains(FILTER_SEPARATOR)) {
            String[] allFilters = strippedFilter.split(Pattern.quote(FILTER_SEPARATOR));
            for (String tempFilter : allFilters) {
                if (isSingleValueFilterExpression(tempFilter)) {
                    singleValueFilters.add(tempFilter);
                }
            }
        } else {
            if (isSingleValueFilterExpression(strippedFilter)) {
                singleValueFilters.add(strippedFilter);
            }
        }
        return singleValueFilters;
    }

    /**
     * Creates a filter list if mullti value filter is provided.
     *
     * @param strippedFilter
     * @param skipVerification, skips the validation from the mapping if set to true
     * @return List<FilterExpressionMultiValue>
     */
    @SuppressWarnings("squid:S4248")
    public List<FilterExpressionMultiValue<String>> createMultiValueFilter(List<String> strippedFilter,
                                                                           boolean skipVerification) {
        List<FilterExpressionMultiValue<String>> filterExpressionMultiValue = new ArrayList<>();
        for (String filter : strippedFilter) {
            filterExpressionMultiValue.add(createFilterExpressionMulti(filter, skipVerification));
        }
        return filterExpressionMultiValue;
    }

    /**
     * Creates a filter list if multiple filters are provided.
     *
     * @param strippedFilter
     * @return List<String>
     */
    public static List<String> getMultiValueFilter(String strippedFilter) {
        List<String> multiValueFilters = new ArrayList<>();
        if (strippedFilter.contains(FILTER_SEPARATOR)) {
            String[] allFilters = strippedFilter.split(Pattern.quote(FILTER_SEPARATOR));
            for (String tempFilter : allFilters) {
                if (!isSingleValueFilterExpression(tempFilter)) {
                    multiValueFilters.add(tempFilter);
                }
            }
        } else {
            if (!isSingleValueFilterExpression(strippedFilter)) {
                multiValueFilters.add(strippedFilter);
            }
        }
        return multiValueFilters;
    }

    /**
     * Creates a single filter expression for the provided filter. Validates if the filter operation can be done.
     * Validates if the data type is supported. Also validates the value with the data type
     *
     * @param filter
     * @param skipValidation, skips the validation from the mapping if set to true
     * @return FilterExpressionOneValue
     */
    private FilterExpressionOneValue<String> createFilterExpressionOne(String filter, boolean skipValidation) {
        String operation = getOperation(filter);
        String parameter = getParameter(filter);
        String value = getValue(filter);
        if (!skipValidation) {
            validateOperationSupported(parameter, operation, filter);
        }
        return createFilterExpressionOneValue(parameter, value, operation);
    }

    public static String getValue(String filter) {
        return filter.substring(ordinalIndexOf(filter, FILTER_VALUE_SEPARATOR, 2) + 1);
    }

    @SuppressWarnings("squid:S4248")
    public static List<String> getValues(String filter) {
        List<String> allValues = new ArrayList<>();
        String values = filter.substring(ordinalIndexOf(filter, FILTER_VALUE_SEPARATOR, 2) + 1);
        if (values.contains("'")) {
            while (values != null) {
                values = checkAndResetFilterValue(filter, values, allValues);
            }
        } else {
            Collections.addAll(allValues, values.split(FILTER_VALUE_SEPARATOR));
        }
        return allValues;
    }

    @SuppressWarnings("squid:S4248")
    private static String checkAndResetFilterValue(String filter, String values, List<String> allValues) {
        if (values.startsWith("'")) {
            if (values.contains("',")) {
                String[] test = values.split("',");
                allValues.add(test[0].substring(1));
                return values.substring(test[0].length() + 2);
            } else {
                allValues.add(filter.substring(1, values.length() - 1));
                return null;
            }
        } else {
            if (values.contains(FILTER_VALUE_SEPARATOR)) {
                String[] test = values.split(FILTER_VALUE_SEPARATOR);
                allValues.add(test[0]);
                return values.substring(test[0].length() + 1);
            } else {
                allValues.add(values);
                return null;
            }
        }
    }

    /**
     * Validates if the operation provided is supported by the data type.
     *
     * @param dataTypeFromMapping
     * @param operation
     * @return boolean
     */
    private static boolean isOperationSupportedForDataType(DataType dataTypeFromMapping, String operation) {
        return OPERATION_TO_DATA_TYPE_MAPPING.get(operation).contains(dataTypeFromMapping);
    }

    /**
     * Creates a multi expression filter and adds it in the list. It validates the data type of the input provided
     * by the user. If checks if the operation is supported for the provided parameter. It also validates if the entity
     * parameter provided by the developer is present in the Entity, If the Entity parameter is present then it fetches
     * the column name.
     *
     * @param filter
     * @return
     */
    private FilterExpressionMultiValue<String> createFilterExpressionMulti(String filter, boolean skipVerfication) {
        String operation = getOperation(filter);
        String parameter = getParameter(filter);
        List<String> values = getValues(filter);
        if (!skipVerfication) {
            validateOperationSupported(parameter, operation, filter);
        }
        return createFilterExpressionMultiValue(parameter, values, operation);
    }

    private void validateOperationSupported(String key, String operation, String filter) {
        MappingData mappingData = mapping.get(key);
        //Checks if the filter is supported, This is validated from the mapping provided by developer
        if (mappingData != null) {
            //Validates if the data type is supported for the provided parameter
            if (!isOperationSupportedForDataType(mappingData.getDataType(), operation)) {
                throw new IllegalArgumentException(String.format(FilterErrorMessage
                        .OPERATION_NOT_SUPPORTED_FOR_KEY_ERROR_MESSAGE, operation, key));
            }

        } else {
            throw new IllegalArgumentException(String.format(FilterErrorMessage.FILTER_NOT_SUPPORTED_ERROR_MESSAGE,
                    filter));
        }
    }

    /**
     * This method is used to determine if the filter is One Multi or an invalid filter.
     * If an invalid filter is provided IllegalArgumentException is thrown
     *
     * @param filter
     * @return boolean
     */
    private static boolean isSingleValueFilterExpression(String filter) {
        validFilterString(filter);
        String operation = getOperation(filter);
        if (OperandOneValue.fromFilterOperation(operation) != null) {
            return true;
        } else if (OperandMultiValue.fromFilterOperation(operation) != null) {
            return false;
        } else {
            throw new IllegalArgumentException(String.format(FilterErrorMessage.INVALID_OPERATION_ERROR_MESSAGE,
                    filter));
        }

    }

    public static String getOperation(String filter) {
        return filter.substring(0, ordinalIndexOf(filter, FILTER_VALUE_SEPARATOR, 1));
    }

    public static String getParameter(String filter) {
        return filter.substring(ordinalIndexOf(filter, FILTER_VALUE_SEPARATOR, 1) + 1,
                ordinalIndexOf(filter, FILTER_VALUE_SEPARATOR, 2));
    }

    /**
     * Creates initial Specification from single value filters list.
     *
     * @param allFilterExpressionOne
     * @return Specification of single value filters
     */
    private Specification<T> createOneValueSpecification(List<FilterExpressionOneValue<String>> allFilterExpressionOne) {
        List<SpecificationOneValue<T>> allSpecificationOneValue = new ArrayList<>();
        for (FilterExpressionOneValue<String> filterExpressionOneValue : allFilterExpressionOne) {
            allSpecificationOneValue.add(new SpecificationOneValue<>(filterExpressionOneValue));
        }
        Specification<T> specification = null;
        for (SpecificationOneValue<T> oneValue : allSpecificationOneValue) {
            if (specification == null) {
                specification = Specification.where(oneValue);
            } else {
                specification = specification.and(oneValue);
            }
        }
        return specification;
    }

    /**
     * Creates the final specification to be queried from database.
     *
     * @param specification            Specification created from single value filters.
     * @param allMultiFilterExpression List of all multi filter expressions found.
     * @return Final specification
     */
    private Specification<T> createMultiValueSpecification(Specification<T> specification,
                                                           List<FilterExpressionMultiValue<String>> allMultiFilterExpression) {
        List<SpecificationMultiValue<T>> allSpecificationMultiValue = new ArrayList<>();
        for (FilterExpressionMultiValue<String> filterExpressionMultiValue : allMultiFilterExpression) {
            allSpecificationMultiValue.add(new SpecificationMultiValue<>(filterExpressionMultiValue));
        }
        Specification<T> finalSpecification = specification;
        for (SpecificationMultiValue<T> multipleValue : allSpecificationMultiValue) {
            if (specification == null) {
                finalSpecification = Specification.where(multipleValue);
            } else {
                finalSpecification = specification.and(multipleValue);
            }
        }
        return finalSpecification;
    }

    public Specification<T> createSpecification(List<FilterExpressionOneValue<String>> allFilterExpressionOne,
                                                List<FilterExpressionMultiValue<String>> allMultiFilterExpression) {
        final Specification<T> oneValueSpecification = createOneValueSpecification(allFilterExpressionOne);
        return createMultiValueSpecification(oneValueSpecification, allMultiFilterExpression);
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> void validateDataType(DataType dataType, String data, Class<E> enumeration) {
        if (dataType == DataType.NUMBER && !NUMBER_EXPRESSION.matcher(data).find()) {
            throw new IllegalArgumentException(String.format(FilterErrorMessage.INVALID_NUMBER_VALUE_ERROR_MESSAGE,
                    data));
        } else if (dataType == DataType.DATE) {
            try {
                LocalDateTime.parse(data.trim());
            } catch (DateTimeParseException pe) {
                throw new IllegalArgumentException(String.format(FilterErrorMessage
                        .INVALID_DATE_VALUE_ERROR_MESSAGE, data));
            }
        } else if (dataType == DataType.BOOLEAN && !"true".equalsIgnoreCase(data) && !"false".equalsIgnoreCase(data)) {
            throw new IllegalArgumentException(String.format(FilterErrorMessage.INVALID_BOOLEAN_VALUE_ERROR_MESSAGE,
                    data));
        } else if (dataType == DataType.ENUMERATION && !EnumUtils.isValidEnum(enumeration, data)) {
            throw new IllegalArgumentException(String.format(FilterErrorMessage
                    .INVALID_ENUMERATION_VALUE_ERROR_MESSAGE, enumeration, EnumSet.allOf(enumeration)));
        }
    }

}
