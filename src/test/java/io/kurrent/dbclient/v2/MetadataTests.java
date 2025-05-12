package io.kurrent.dbclient.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MetadataTests {
    @Test
    public void testMetadataConstructors() {
        // Test default constructor
        Metadata metadata1 = new Metadata();
        Assertions.assertEquals(0, metadata1.size());

        // Test constructor with existing metadata
        metadata1.put("key1", "value1");
        Metadata metadata2 = new Metadata(metadata1);
        Assertions.assertEquals(1, metadata2.size());
        Assertions.assertEquals("value1", metadata2.get("key1"));

        // Test constructor with dictionary
        Map<String, Object> dictionary = new HashMap<>();
        dictionary.put("key2", "value2");
        Metadata metadata3 = new Metadata(dictionary);
        Assertions.assertEquals(1, metadata3.size());
        Assertions.assertEquals("value2", metadata3.get("key2"));
    }

    @Test
    public void testMetadataSetMethod() {
        Metadata metadata = new Metadata();
        
        // Test set method with chaining
        metadata.set("key1", "value1").set("key2", 123);
        
        Assertions.assertEquals(2, metadata.size());
        Assertions.assertEquals("value1", metadata.get("key1"));
        Assertions.assertEquals(123, metadata.get("key2"));
    }

    @Test
    public void testMetadataGetMethod() {
        Metadata metadata = new Metadata();
        metadata.put("stringKey", "stringValue");
        metadata.put("intKey", 123);
        metadata.put("boolKey", true);
        
        // Test get method with type parameter
        String stringValue = metadata.get("stringKey", String.class);
        Integer intValue = metadata.get("intKey", Integer.class);
        Boolean boolValue = metadata.get("boolKey", Boolean.class);
        
        Assertions.assertEquals("stringValue", stringValue);
        Assertions.assertEquals(123, intValue);
        Assertions.assertEquals(true, boolValue);
        
        // Test get method with a default value
        String nonExistentValue = metadata.get("nonExistentKey", "defaultValue", String.class);
        Assertions.assertEquals("defaultValue", nonExistentValue);
    }

    @Test
    public void testMetadataTryGetMethod() {
        Metadata metadata = new Metadata();
        metadata.put("stringKey", "stringValue");
        metadata.put("intKey", 123);
        metadata.put("boolKey", true);
        metadata.put("uuidKey", UUID.fromString("00000000-0000-0000-0000-000000000001"));
        metadata.put("instantKey", Instant.parse("2023-01-01T00:00:00Z"));
        
        // Test tryGet method with direct type match
        Optional<String> stringResult = metadata.tryGet("stringKey", String.class);
        Assertions.assertTrue(stringResult.isPresent());
        Assertions.assertEquals("stringValue", stringResult.get());
        
        // Test tryGet method with type conversion
        Optional<Integer> intResult = metadata.tryGet("intKey", Integer.class);
        Assertions.assertTrue(intResult.isPresent());
        Assertions.assertEquals(123, intResult.get());
        
        // Test tryGet method with string conversion
        metadata.put("stringIntKey", "456");
        Optional<Integer> stringIntResult = metadata.tryGet("stringIntKey", Integer.class);
        Assertions.assertTrue(stringIntResult.isPresent());
        Assertions.assertEquals(456, stringIntResult.get());
        
        // Test tryGet method with a non-existent key
        Optional<String> nonExistentResult = metadata.tryGet("nonExistentKey", String.class);
        Assertions.assertFalse(nonExistentResult.isPresent());

        // Test tryGet method with incompatible type
        Optional<UUID> incompatibleResult = metadata.tryGet("stringKey", UUID.class);
        Assertions.assertFalse(incompatibleResult.isPresent());
    }
}