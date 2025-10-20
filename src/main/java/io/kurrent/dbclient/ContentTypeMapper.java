package io.kurrent.dbclient;

import io.kurrentdb.protocol.v2.streams.SchemaFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ContentTypeMapper {
    private static final Map<String, SchemaFormat> CONTENT_TYPE_MAP;

    static {
        Map<String, SchemaFormat> map = new HashMap<>();
        map.put("application/json", SchemaFormat.SCHEMA_FORMAT_JSON);
        map.put("application/octet-stream", SchemaFormat.SCHEMA_FORMAT_BYTES);
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public static SchemaFormat toSchemaDataFormat(String contentType) {
        return CONTENT_TYPE_MAP.getOrDefault(contentType, SchemaFormat.SCHEMA_FORMAT_UNSPECIFIED);
    }
}
