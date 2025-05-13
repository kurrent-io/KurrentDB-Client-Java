package io.kurrent.dbclient.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

public class RegisteredSchemaTests {

    @Test
    public void testNoneRegisteredSchema() {
        RegisteredSchema none = RegisteredSchema.NONE;
        
        Assertions.assertNull(none.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, none.getDataFormat());
        Assertions.assertNull(none.getSchemaVersionId());
        Assertions.assertNull(none.getDefinition());
        Assertions.assertEquals(0, none.getVersionNumber());
        Assertions.assertNotNull(none.getCreatedAt());
    }
    
    @Test
    public void testRegisteredSchemaConstructor() {
        String schemaName = "test-schema";
        SchemaDataFormat dataFormat = SchemaDataFormat.JSON;
        String schemaVersionId = "schema-version-123";
        String definition = "{\"type\":\"object\",\"properties\":{}}";
        int versionNumber = 1;
        OffsetDateTime createdAt = OffsetDateTime.now();
        
        RegisteredSchema schema = new RegisteredSchema(
                schemaName,
                dataFormat,
                schemaVersionId,
                definition,
                versionNumber,
                createdAt
        );
        
        Assertions.assertEquals(schemaName, schema.getSchemaName());
        Assertions.assertEquals(dataFormat, schema.getDataFormat());
        Assertions.assertEquals(schemaVersionId, schema.getSchemaVersionId());
        Assertions.assertEquals(definition, schema.getDefinition());
        Assertions.assertEquals(versionNumber, schema.getVersionNumber());
        Assertions.assertEquals(createdAt, schema.getCreatedAt());
    }
    
    @Test
    public void testToSchemaInfo() {
        String schemaName = "test-schema";
        SchemaDataFormat dataFormat = SchemaDataFormat.JSON;
        
        RegisteredSchema schema = new RegisteredSchema(
                schemaName,
                dataFormat,
                "schema-version-123",
                "{\"type\":\"object\",\"properties\":{}}",
                1,
                OffsetDateTime.now()
        );
        
        SchemaInfo schemaInfo = schema.toSchemaInfo();
        
        Assertions.assertEquals(schemaName, schemaInfo.getSchemaName());
        Assertions.assertEquals(dataFormat, schemaInfo.getDataFormat());
    }
    
    @Test
    public void testEqualsAndHashCode() {
        OffsetDateTime now = OffsetDateTime.now();
        
        RegisteredSchema schema1 = new RegisteredSchema(
                "test-schema",
                SchemaDataFormat.JSON,
                "schema-version-123",
                "{\"type\":\"object\",\"properties\":{}}",
                1,
                now
        );
        
        RegisteredSchema schema2 = new RegisteredSchema(
                "test-schema",
                SchemaDataFormat.JSON,
                "schema-version-123",
                "{\"type\":\"object\",\"properties\":{}}",
                1,
                now
        );
        
        RegisteredSchema schema3 = new RegisteredSchema(
                "different-schema",
                SchemaDataFormat.PROTOBUF,
                "schema-version-456",
                "message Test {}",
                2,
                now
        );
        
        Assertions.assertEquals(schema1, schema2);
        Assertions.assertEquals(schema1.hashCode(), schema2.hashCode());
        
        Assertions.assertNotEquals(schema1, schema3);
        Assertions.assertNotEquals(schema1.hashCode(), schema3.hashCode());
    }
}