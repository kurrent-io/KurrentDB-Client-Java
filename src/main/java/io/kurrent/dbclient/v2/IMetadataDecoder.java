package io.kurrent.dbclient.v2;

import io.kurrent.dbclient.StreamMetadata;

/**
 * Interface for decoding metadata from a byte array.
 */
public interface IMetadataDecoder {
    /**
     * Decodes metadata from a byte array.
     *
     * @param bytes The byte array containing the encoded metadata.
     * @return The decoded metadata.
     */
    StreamMetadata decode(byte[] bytes);
}