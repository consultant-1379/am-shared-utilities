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
package com.ericsson.am.shared.vnfd.utils;

import static java.util.Objects.requireNonNull;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public final class TestUtils {

    private static final Logger LOGGER = getLogger(TestUtils.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Path TEST_RESOURCES_DIR = Path.of("", "src/test/resources");

    private TestUtils() {
    }

    public static Path getResource(String fileToLocate) {
        File file = new File(fileToLocate);
        if (!file.exists()) {
            LOGGER.error("The file {} does not exist", file.getAbsolutePath());
        }
        if (file.isFile()) {
            LOGGER.info("Full path to file is: {}", file.getAbsolutePath());
            return file.toPath();
        }
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        file = new File(requireNonNull(classLoader.getResource(fileToLocate)).getFile());
        LOGGER.info("Full path to file is: {}", file.getAbsolutePath());
        return file.toPath();
    }

    public static String readDataFromFile(String fileName) throws IOException {
        return Files.lines(getResource(fileName)).collect(Collectors.joining("\n"));
    }

    private static String readJsonFromFile(String file) throws IOException {
        return Files.readString(TEST_RESOURCES_DIR.resolve(file));
    }

    public static <T> T readObjectFromFile(String file, Class<T> objectType) throws IOException {
        return OBJECT_MAPPER.readValue(Files.readString(TEST_RESOURCES_DIR.resolve(file)), objectType);
    }

    public static <T> T readObjectFromJson(String json, Class<T> objectType) throws IOException {
        return OBJECT_MAPPER.readValue(json, objectType);
    }

    public static <T> List<T> readListFromFile(String file, Class<T> objectType) throws IOException {
        return readListFromJson(readJsonFromFile(file), objectType);
    }

    public static <T> List<T> readListFromJson(String json, Class<T> objectType) throws IOException {
        CollectionType javaType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, objectType);
        return OBJECT_MAPPER.readValue(json, javaType);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

}
