package io.kurrent.dbclient.serialization;

import java.util.Optional;

/**
 * Strategy for naming message types.
 */
public interface MessageTypeNamingStrategy {
    /**
     * Resolves a type name for the given message class.
     *
     * @param messageClass The message class to resolve a name for
     * @param resolutionContext The context for resolution
     * @return The resolved type name
     */
    String resolveTypeName(Class<?> messageClass, MessageTypeNamingResolutionContext resolutionContext);

    /**
     * Tries to resolve a Java class from a message type name.
     *
     * @param messageTypeName Message type name to resolve
     * @return Optional with resolved class, or empty if class wasn't found
     */
    Optional<Class<?>> tryResolveJavaClass(String messageTypeName);

    /**
     * Tries to resolve a Java metadata class from a message type name.
     *
     * @param messageTypeName Message type name to resolve
     * @return Optional with resolved class, or empty if class wasn't found
     */
    Optional<Class<?>> tryResolveMetadataJavaClass(String messageTypeName);
}

/**
 * Wrapper for message type naming strategies.
 */
class MessageTypeNamingStrategyWrapper implements MessageTypeNamingStrategy {
    private final MessageTypeRegistry messageTypeRegistry;
    private final MessageTypeNamingStrategy messageTypeNamingStrategy;

    /**
     * Creates a new wrapper with the specified registry and strategy.
     *
     * @param messageTypeRegistry The message type registry
     * @param messageTypeNamingStrategy The strategy to wrap
     */
    public MessageTypeNamingStrategyWrapper(
            MessageTypeRegistry messageTypeRegistry,
            MessageTypeNamingStrategy messageTypeNamingStrategy) {
        this.messageTypeRegistry = messageTypeRegistry;
        this.messageTypeNamingStrategy = messageTypeNamingStrategy;
    }

    @Override
    public String resolveTypeName(Class<?> messageType, MessageTypeNamingResolutionContext resolutionContext) {
        return messageTypeRegistry.getOrAddTypeName(
                messageType,
                type -> messageTypeNamingStrategy.resolveTypeName(messageType, resolutionContext)
        );
    }

    @Override
    public Optional<Class<?>> tryResolveJavaClass(String messageTypeName) {
        return messageTypeRegistry.getOrAddJavaClass(
                messageTypeName,
                name -> messageTypeNamingStrategy.tryResolveMetadataJavaClass(messageTypeName)
        );
    }

    @Override
    public Optional<Class<?>> tryResolveMetadataJavaClass(String messageTypeName) {
        return messageTypeRegistry.getOrAddJavaClass(
                messageTypeName + "-metadata",
                name -> messageTypeNamingStrategy.tryResolveMetadataJavaClass(messageTypeName)
        );
    }
}

