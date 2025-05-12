package io.kurrent.dbclient.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class MessageTests {
    
    @Test
    public void testEmptyMessage() {
        Message empty = Message.EMPTY;
        
        Assertions.assertNull(empty.getValue());
        Assertions.assertNotNull(empty.getMetadata());
        Assertions.assertEquals(0, empty.getMetadata().size());
        Assertions.assertNotNull(empty.getRecordId());
        Assertions.assertEquals(SchemaDataFormat.JSON, empty.getDataFormat());
    }
    
    @Test
    public void testMessageConstructor() {
        Metadata metadata = new Metadata();
        metadata.put("key1", "value1");
        
        UUID recordId = UUID.randomUUID();
        
        Message message = new Message("test payload", metadata, recordId, SchemaDataFormat.PROTOBUF);
        
        Assertions.assertEquals("test payload", message.getValue());
        Assertions.assertSame(metadata, message.getMetadata());
        Assertions.assertEquals(1, message.getMetadata().size());
        Assertions.assertEquals("value1", message.getMetadata().get("key1"));
        Assertions.assertEquals(recordId, message.getRecordId());
        Assertions.assertEquals(SchemaDataFormat.PROTOBUF, message.getDataFormat());
    }
    
    @Test
    public void testMessageBuilder() {
        Metadata metadata = new Metadata();
        metadata.put("key1", "value1");
        
        UUID recordId = UUID.randomUUID();
        
        Message message = Message.builder()
                .value("test payload")
                .metadata(metadata)
                .recordId(recordId)
                .dataFormat(SchemaDataFormat.AVRO)
                .build();
        
        Assertions.assertEquals("test payload", message.getValue());
        Assertions.assertSame(metadata, message.getMetadata());
        Assertions.assertEquals(1, message.getMetadata().size());
        Assertions.assertEquals("value1", message.getMetadata().get("key1"));
        Assertions.assertEquals(recordId, message.getRecordId());
        Assertions.assertEquals(SchemaDataFormat.AVRO, message.getDataFormat());
    }
    
    @Test
    public void testMessageBuilderWithDefaults() {
        Message message = Message.builder()
                .value("test payload")
                .build();
        
        Assertions.assertEquals("test payload", message.getValue());
        Assertions.assertNotNull(message.getMetadata());
        Assertions.assertEquals(0, message.getMetadata().size());
        Assertions.assertNotNull(message.getRecordId());
        Assertions.assertEquals(SchemaDataFormat.JSON, message.getDataFormat());
    }
    
    @Test
    public void testMessageBuilderWithNullValues() {
        Message message = Message.builder()
                .value(null)
                .build();
        
        Assertions.assertNull(message.getValue());
        Assertions.assertNotNull(message.getMetadata());
        Assertions.assertEquals(0, message.getMetadata().size());
        Assertions.assertNotNull(message.getRecordId());
        Assertions.assertEquals(SchemaDataFormat.JSON, message.getDataFormat());
    }
}