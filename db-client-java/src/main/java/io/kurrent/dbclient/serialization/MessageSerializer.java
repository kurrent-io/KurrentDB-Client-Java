package io.kurrent.dbclient.serialization;

import io.kurrent.dbclient.Message;
import io.kurrent.dbclient.MessageData;

import java.util.List;

public interface MessageSerializer {
    MessageSerializer with(OperationSerializationSettings serializationSettings);

    MessageData serialize(Message value, MessageSerializationContext context);

    List<MessageData> serialize(List<Message> messages, MessageSerializationContext serializationContext);
}

