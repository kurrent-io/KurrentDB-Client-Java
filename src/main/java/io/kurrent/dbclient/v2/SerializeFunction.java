package io.kurrent.dbclient.v2;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Functional interface for serializing an object into bytes.
 */
@FunctionalInterface
public interface SerializeFunction {
    /**
     * Serializes the given value into bytes according to the provided metadata.
     *
     * @param value The object to be serialized.
     * @param metadata The metadata providing additional information for the serialization process.
     * @return A CompletableFuture that resolves to a ByteBuffer containing the serialized data.
     */
    CompletableFuture<ByteBuffer> serialize(Object value, Metadata metadata);
}