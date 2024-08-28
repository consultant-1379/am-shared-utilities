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

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.ByteBuffer;

public class LongRedisSerializer implements RedisSerializer<Long> {
    @Override
    public byte[] serialize(Long value) throws SerializationException {
        byte[] result =  new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(result);
        byteBuffer.putLong(value);
        return result;
    }

    @Override
    public Long deserialize(byte[] bytes) throws SerializationException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getLong();
    }
}
