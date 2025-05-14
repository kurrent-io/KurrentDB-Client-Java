package io.kurrent.dbclient.v2;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * Functional interface for deserializing bytes into an object.
 */
@FunctionalInterface
public interface DeserializeFunction {
    /**
     * Deserializes the given bytes into an object according to the provided metadata.
     *
     * @param data The bytes to be deserialized.
     * @param metadata The metadata providing additional information for the deserialization process.
     * @return A CompletableFuture that resolves to the deserialized object.
     */
    CompletableFuture<Object> deserialize(ByteBuffer data, Metadata metadata);
}