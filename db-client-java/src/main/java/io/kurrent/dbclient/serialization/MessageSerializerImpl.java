package io.kurrent.dbclient.serialization;

import io.kurrent.dbclient.Message;
import io.kurrent.dbclient.MessageData;

import java.util.List;
import java.util.stream.Collectors;

class MessageSerializerImpl implements MessageSerializer {
    @Override
    public MessageSerializer with(OperationSerializationSettings serializationSettings) {
        return null;
    }

    @Override
    public MessageData serialize(Message value, MessageSerializationContext context) {
        return null;
    }

    @Override
    public List<MessageData> serialize(List<Message> messages, MessageSerializationContext serializationContext) {
        return messages.stream().map(m -> serialize(m, serializationContext)).collect(Collectors.toList());
    }

}
