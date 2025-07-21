package io.kurrent.dbclient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ContentTypeMapper {
    private static final Map<String, String> CONTENT_TYPE_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("application/json", "Json");
        map.put("application/octet-stream", "Binary");
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public static String toSchemaDataFormat(String contentType) {
        return CONTENT_TYPE_MAP.getOrDefault(contentType, contentType);
    }
}
