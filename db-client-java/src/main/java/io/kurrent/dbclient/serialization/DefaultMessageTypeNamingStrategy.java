package io.kurrent.dbclient.serialization;

import java.util.Optional;

/**
 * Default implementation of MessageTypeNamingStrategy.
 */
public class DefaultMessageTypeNamingStrategy implements MessageTypeNamingStrategy {
    private final Class<?> defaultMetadataType;

    /**
     * Creates a new strategy with the specified default metadata type.
     *
     * @param defaultMetadataType The default metadata type
     */
    public DefaultMessageTypeNamingStrategy(Class<?> defaultMetadataType) {
        this.defaultMetadataType = defaultMetadataType != null ? defaultMetadataType : TracingMetadata.class;
    }

    @Override
    public String resolveTypeName(Class<?> messageType, MessageTypeNamingResolutionContext resolutionContext) {
        return resolutionContext.getCategoryName() + "-" + messageType.getName();
    }

    @Override
    public Optional<Class<?>> tryResolveJavaClass(String messageTypeName) {
        int categorySeparatorIndex = messageTypeName.indexOf('-');

        if (categorySeparatorIndex == -1 || categorySeparatorIndex == messageTypeName.length() - 1) {
            return Optional.empty();
        }

        String clrTypeName = messageTypeName.substring(categorySeparatorIndex + 1);
        return ClassProvider.getClassByName(clrTypeName);
    }

    @Override
    public Optional<Class<?>> tryResolveMetadataJavaClass(String messageTypeName) {
        return Optional.of(defaultMetadataType);
    }
}
