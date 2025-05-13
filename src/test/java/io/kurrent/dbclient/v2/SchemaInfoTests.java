package io.kurrent.dbclient.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchemaInfoTests {

    @Test
    public void testNoneSchemaInfo() {
        SchemaInfo none = SchemaInfo.NONE;
        
        Assertions.assertEquals("", none.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, none.getDataFormat());
        Assertions.assertEquals("application/octet-stream", none.getContentType());
        Assertions.assertTrue(none.isSchemaNameMissing());
    }
    
    @Test
    public void testSchemaInfoConstructor() {
        SchemaInfo jsonSchema = new SchemaInfo("test-schema", SchemaDataFormat.JSON);
        
        Assertions.assertEquals("test-schema", jsonSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.JSON, jsonSchema.getDataFormat());
        Assertions.assertEquals("application/json", jsonSchema.getContentType());
        Assertions.assertFalse(jsonSchema.isSchemaNameMissing());
        
        SchemaInfo protobufSchema = new SchemaInfo("proto-schema", SchemaDataFormat.PROTOBUF);
        
        Assertions.assertEquals("proto-schema", protobufSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.PROTOBUF, protobufSchema.getDataFormat());
        Assertions.assertEquals("application/vnd.google.protobuf", protobufSchema.getContentType());
        Assertions.assertFalse(protobufSchema.isSchemaNameMissing());
        
        SchemaInfo avroSchema = new SchemaInfo("avro-schema", SchemaDataFormat.AVRO);
        
        Assertions.assertEquals("avro-schema", avroSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.AVRO, avroSchema.getDataFormat());
        Assertions.assertEquals("application/vnd.apache.avro+json", avroSchema.getContentType());
        Assertions.assertFalse(avroSchema.isSchemaNameMissing());
        
        SchemaInfo bytesSchema = new SchemaInfo("bytes-schema", SchemaDataFormat.BYTES);
        
        Assertions.assertEquals("bytes-schema", bytesSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.BYTES, bytesSchema.getDataFormat());
        Assertions.assertEquals("application/octet-stream", bytesSchema.getContentType());
        Assertions.assertFalse(bytesSchema.isSchemaNameMissing());
    }
    
    @Test
    public void testSchemaNameMissing() {
        SchemaInfo emptyName = new SchemaInfo("", SchemaDataFormat.JSON);
        Assertions.assertTrue(emptyName.isSchemaNameMissing());
        
        SchemaInfo whitespace = new SchemaInfo("   ", SchemaDataFormat.JSON);
        Assertions.assertTrue(whitespace.isSchemaNameMissing());
        
        SchemaInfo nullName = new SchemaInfo(null, SchemaDataFormat.JSON);
        Assertions.assertTrue(nullName.isSchemaNameMissing());
        
        SchemaInfo validName = new SchemaInfo("valid-name", SchemaDataFormat.JSON);
        Assertions.assertFalse(validName.isSchemaNameMissing());
    }
    
    @Test
    public void testInjectIntoMetadata() {
        SchemaInfo schema = new SchemaInfo("test-schema", SchemaDataFormat.JSON);
        Metadata metadata = new Metadata();
        
        schema.injectIntoMetadata(metadata);
        
        Assertions.assertEquals(2, metadata.size());
        Assertions.assertEquals("test-schema", metadata.get("schema-name", String.class));
        Assertions.assertEquals("json", metadata.get("schema-data-format", String.class));
    }
    
    @Test
    public void testInjectSchemaNameIntoMetadata() {
        SchemaInfo schema = new SchemaInfo("test-schema", SchemaDataFormat.JSON);
        Metadata metadata = new Metadata();
        
        schema.injectSchemaNameIntoMetadata(metadata);
        
        Assertions.assertEquals(1, metadata.size());
        Assertions.assertEquals("test-schema", metadata.get("schema-name", String.class));
        Assertions.assertNull(metadata.get("schema-data-format", String.class));
    }
    
    @Test
    public void testFromMetadata() {
        Metadata metadata = new Metadata();
        metadata.set("schema-name", "test-schema");
        metadata.set("schema-data-format", "json");
        
        SchemaInfo schema = SchemaInfo.fromMetadata(metadata);
        
        Assertions.assertEquals("test-schema", schema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.JSON, schema.getDataFormat());
        Assertions.assertEquals("application/json", schema.getContentType());
    }
    
    @Test
    public void testFromMetadataWithMissingValues() {
        Metadata emptyMetadata = new Metadata();
        SchemaInfo schema = SchemaInfo.fromMetadata(emptyMetadata);
        
        Assertions.assertEquals("", schema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, schema.getDataFormat());
        
        Metadata nameOnlyMetadata = new Metadata();
        nameOnlyMetadata.set("schema-name", "test-schema");
        
        SchemaInfo nameOnlySchema = SchemaInfo.fromMetadata(nameOnlyMetadata);
        
        Assertions.assertEquals("test-schema", nameOnlySchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, nameOnlySchema.getDataFormat());
        
        Metadata formatOnlyMetadata = new Metadata();
        formatOnlyMetadata.set("schema-data-format", "protobuf");
        
        SchemaInfo formatOnlySchema = SchemaInfo.fromMetadata(formatOnlyMetadata);
        
        Assertions.assertEquals("", formatOnlySchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.PROTOBUF, formatOnlySchema.getDataFormat());
    }
    
    @Test
    public void testFromContentType() {
        SchemaInfo jsonSchema = SchemaInfo.fromContentType("test-schema", "application/json");
        
        Assertions.assertEquals("test-schema", jsonSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.JSON, jsonSchema.getDataFormat());
        
        SchemaInfo protobufSchema = SchemaInfo.fromContentType("proto-schema", "application/vnd.google.protobuf");
        
        Assertions.assertEquals("proto-schema", protobufSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.PROTOBUF, protobufSchema.getDataFormat());
        
        SchemaInfo bytesSchema = SchemaInfo.fromContentType("bytes-schema", "application/octet-stream");
        
        Assertions.assertEquals("bytes-schema", bytesSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.BYTES, bytesSchema.getDataFormat());
        
        SchemaInfo unknownSchema = SchemaInfo.fromContentType("unknown-schema", "unknown/content-type");
        
        Assertions.assertEquals("unknown-schema", unknownSchema.getSchemaName());
        Assertions.assertEquals(SchemaDataFormat.UNSPECIFIED, unknownSchema.getDataFormat());
    }
    
    @Test
    public void testFromContentTypeWithInvalidArguments() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaInfo.fromContentType(null, "application/json");
        });
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaInfo.fromContentType("", "application/json");
        });
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaInfo.fromContentType("test-schema", null);
        });
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SchemaInfo.fromContentType("test-schema", "");
        });
    }
}