package io.kurrent.dbclient.serialization;

import io.kurrent.dbclient.Message;
import io.kurrent.dbclient.MessageData;

import java.util.List;
import java.util.stream.Collectors;

class MessageSerializerImpl implements MessageSerializer {
    Serializer serializer = new JacksonSerializer();
    
    @Override
    public MessageSerializer with(OperationSerializationSettings serializationSettings) {
        return this;
    }

    @Override
    public MessageData serialize(Message value, MessageSerializationContext context) {
        return MessageData
                .builderAsJson(value.getData().getClass().getTypeName(), serializer.serialize(value))
                .build();
    }

    @Override
    public List<MessageData> serialize(List<Message> messages, MessageSerializationContext serializationContext) {
        return messages.stream().map(m -> serialize(m, serializationContext)).collect(Collectors.toList());
    }

}
