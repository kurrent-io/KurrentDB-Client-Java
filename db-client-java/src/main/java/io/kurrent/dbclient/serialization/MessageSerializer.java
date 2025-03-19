package io.kurrent.dbclient.serialization;

import io.kurrent.dbclient.Message;
import io.kurrent.dbclient.MessageData;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface MessageSerializer {
    MessageSerializer with(OperationSerializationSettings serializationSettings);

    MessageData serialize(Message message, MessageSerializationContext context);

    List<MessageData> serialize(List<Message> messages, MessageSerializationContext serializationContext);
}

class MessageSerializerImpl implements MessageSerializer {
    private final SchemaRegistry schemaRegistry;
    private final KurrentDBClientSerializationSettings serializationSettings;
    // TODO: Ensure that settings are aligned between clients
    private final JacksonSerializer metadataSerializer;
    private final String contentType;

    public MessageSerializerImpl(SchemaRegistry schemaRegistry, KurrentDBClientSerializationSettings serializationSettings) {
        this.schemaRegistry = schemaRegistry;
        this.serializationSettings = serializationSettings;
        this.metadataSerializer = new JacksonSerializer();
        this.contentType = ContentTypeUtils.toMessageContentType(serializationSettings.defaultContentType());
    }

    public static MessageSerializer from(KurrentDBClientSerializationSettings settings) {
        settings = settings != null ? settings: KurrentDBClientSerializationSettings.get();

        return new MessageSerializerImpl(SchemaRegistry.from(settings), settings);
    }
    
    @Override
    public MessageSerializer with(OperationSerializationSettings serializationSettings) {
        return this;
    }

    @Override
    public MessageData serialize(Message message, MessageSerializationContext context) {
        Object data = message.data();
        Object metadata = message.metadata();
        UUID messageId = message.messageId();

        String messageType = schemaRegistry.resolveTypeName(
                data.getClass(),
                context.namingResolution()
        );

        byte[] serializedData = schemaRegistry
                .getSerializer(serializationSettings.defaultContentType())
                .serialize(data);

        byte[] serializedMetadata = metadata != null
                ? metadataSerializer.serialize(metadata)
                : new byte[0];

        return new MessageData(
                messageType,
                serializedData,
                serializedMetadata,
                messageId,
                contentType
        );
    }

    @Override
    public List<MessageData> serialize(List<Message> messages, MessageSerializationContext serializationContext) {
        return messages.stream().map(m -> serialize(m, serializationContext)).collect(Collectors.toList());
    }
}

class ContentTypeUtils {
    public static String toMessageContentType(ContentType contentType) {
        switch (contentType) {
            case JSON:
                return "application/json";
            case BYTES:
                return "application/octet-stream";
            default:
                throw new IllegalArgumentException("Unknown content type: " + contentType);
        }
    }

    public static ContentType fromMessageContentType(String contentTypeString) {
        if (contentTypeString == null || contentTypeString.isEmpty()) {
            return ContentType.JSON;
        }

        if ("application/json".equals(contentTypeString)) {
            return ContentType.JSON;
        } else if ("application/octet-stream".equals(contentTypeString)) {
            return ContentType.BYTES;
        } else {
            throw new IllegalArgumentException("Unknown content type: " + contentTypeString);
        }
    }
}