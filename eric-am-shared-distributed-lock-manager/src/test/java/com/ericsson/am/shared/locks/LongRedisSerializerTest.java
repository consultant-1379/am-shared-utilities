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
package com.ericsson.am.shared.locks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LongRedisSerializerTest {

    @Test
    public void testLongRedisSerializer() {
        LongRedisSerializer lrs = new LongRedisSerializer();
        long now = System.currentTimeMillis();
        byte[] value = lrs.serialize(now);
        assertNotNull(value);
        assertTrue(value.length <= 16);
        Long deserialized = lrs.deserialize(value);
        assertNotNull(deserialized);
        assertEquals(now, deserialized.longValue());
    }
}
