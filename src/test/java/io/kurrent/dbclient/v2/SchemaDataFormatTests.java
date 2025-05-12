package io.kurrent.dbclient.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchemaDataFormatTests {
    
    @Test
    public void testEnumValues() {
        // Verify enum values
        Assertions.assertEquals(0, SchemaDataFormat.UNSPECIFIED.getValue());
        Assertions.assertEquals(1, SchemaDataFormat.JSON.getValue());
        Assertions.assertEquals(2, SchemaDataFormat.PROTOBUF.getValue());
        Assertions.assertEquals(3, SchemaDataFormat.AVRO.getValue());
        Assertions.assertEquals(4, SchemaDataFormat.BYTES.getValue());
    }
    
    @Test
    public void testFromValue() {
        // Verify fromValue method
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, SchemaDataFormat.fromValue(0));
        Assertions.assertEquals(SchemaDataFormat.JSON, SchemaDataFormat.fromValue(1));
        Assertions.assertEquals(SchemaDataFormat.PROTOBUF, SchemaDataFormat.fromValue(2));
        Assertions.assertEquals(SchemaDataFormat.AVRO, SchemaDataFormat.fromValue(3));
        Assertions.assertEquals(SchemaDataFormat.BYTES, SchemaDataFormat.fromValue(4));
        
        // Test invalid value
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, SchemaDataFormat.fromValue(99));
    }
}