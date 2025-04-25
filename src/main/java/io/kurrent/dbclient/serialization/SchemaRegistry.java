package io.kurrent.dbclient.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SchemaRegistry {
    private final Map<ContentType, Serializer> serializers;
    private final MessageTypeNamingStrategy messageTypeNamingStrategy;

    public SchemaRegistry(
            Map<ContentType, Serializer> serializers,
            MessageTypeNamingStrategy messageTypeNamingStrategy) {
        this.serializers = serializers;
        this.messageTypeNamingStrategy = messageTypeNamingStrategy;
    }

    public Serializer getSerializer(ContentType schemaType) {
        return serializers.get(schemaType);
    }

    public String resolveTypeName(Class<?> messageClass, MessageTypeNamingResolutionContext resolutionContext) {
        return messageTypeNamingStrategy.resolveTypeName(messageClass, resolutionContext);
    }
    
    public Optional<Class<?>> tryResolveDataJavaClass(String messageTypeName) {
        return messageTypeNamingStrategy.tryResolveJavaClass(messageTypeName);
    }

    public Optional<Class<?>> tryResolveMetadataJavaClass(String messageTypeName) {
        return messageTypeNamingStrategy.tryResolveMetadataJavaClass(messageTypeName);
    }

    public static SchemaRegistry from(KurrentDBClientSerializationSettings settings) {
        MessageTypeNamingStrategy messageTypeNamingStrategy =
                settings.messageTypeNamingStrategy()
                        .orElse(new DefaultMessageTypeNamingStrategy(settings.defaultMetadataType()));

        Map<Class<?>, String> categoriesTypeMap = resolveMessageTypeUsingNamingStrategy(
                settings.categoryMessageTypesMap(),
                messageTypeNamingStrategy
        );

        MessageTypeRegistry messageTypeRegistry = new MessageTypeRegistryImpl();
        messageTypeRegistry.register(settings.messageTypeMap());
        messageTypeRegistry.register(categoriesTypeMap);

        Map<ContentType, Serializer> serializers = new HashMap<>();

        serializers.put(
                ContentType.JSON,
                settings.jsonSerializer().orElse(new JacksonSerializer())
        );

        serializers.put(
                ContentType.BYTES,
                settings.bytesSerializer().orElse(new JacksonSerializer())
        );

        return new SchemaRegistry(
                serializers,
                new MessageTypeNamingStrategyWrapper(
                        messageTypeRegistry,
                        settings.messageTypeNamingStrategy()
                                .orElse(new DefaultMessageTypeNamingStrategy(settings.defaultMetadataType()))
                )
        );
    }

    private static Map<Class<?>, String> resolveMessageTypeUsingNamingStrategy(
            Map<String, Class<?>[]> categoryMessageTypesMap,
            MessageTypeNamingStrategy messageTypeNamingStrategy
    ) {
        Map<Class<?>, String> result = new HashMap<>();

        for (Map.Entry<String, Class<?>[]> entry : categoryMessageTypesMap.entrySet()) {
            String category = entry.getKey();
            for (Class<?> type : entry.getValue()) {
                String typeName = messageTypeNamingStrategy.resolveTypeName(
                        type,
                        new MessageTypeNamingResolutionContext(category)
                );
                result.put(type, typeName);
            }
        }

        return result;
    }
}
