package io.kurrent.dbclient.serialization;

import java.util.Optional;

/// <summary>
/// Defines the core serialization capabilities required by the KurrentDB client.
/// Implementations of this interface handle the conversion between Java objects and their
/// binary representation for storage in and retrieval from the event store.
/// <br />
/// The client ships default Jackson implementation, but custom implementations can be provided or other formats.
/// </summary>
public interface Serializer {
    /// <summary>
    /// Converts a Java object to its binary representation for storage in the event store.
    /// </summary>
    /// <param name="value">The object to serialize. This could be an event, command, or metadata object.</param>
    /// <returns>
    /// A binary representation of the object that can be stored in KurrentDB.
    /// </returns>
    byte[] serialize(Object value);

    /// <summary>
    /// Reconstructs a Java object from its binary representation retrieved from the event store.
    /// </summary>
    /// <param name="data">The binary data to deserialize, typically retrieved from a KurrentDB event.</param>
    /// <param name="type">The target Java type to deserialize the data into, determined from message type mappings.</param>
    /// <returns>
    /// The deserialized object cast to the specified type, or null if the data cannot be deserialized.
    /// The returned object will be an instance of the specified type or a compatible subtype.
    /// </returns>
    <MessageType> Optional<MessageType> deserialize(Class<MessageType> eventClass, byte[] data);
}
