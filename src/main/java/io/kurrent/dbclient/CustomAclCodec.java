package io.kurrent.dbclient;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CustomAclCodec {
    public static class ListSerializer extends JsonSerializer<List<String>> {
        @Override
        public void serialize(List<String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value.size() == 1) {
                gen.writeString(value.get(0));
            } else {
                gen.writeStartArray();

                for (String s: value) {
                    gen.writeString(s);
                }

                gen.writeEndArray();
            }
        }
    }

    public static class ListDeserializer extends JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.isExpectedStartArrayToken()) {
                List<String> list = new ArrayList<>();

                while (!p.nextToken().isStructEnd()) {
                    list.add(p.getValueAsString());
                }

                return list;
            }

            return Collections.singletonList(p.getValueAsString());
        }
    }

    public static class Serializer extends JsonSerializer<Acl> {
        @Override
        public void serialize(Acl value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value instanceof SystemStreamAcl) {
                gen.writeString(SystemStreamAcl.ACL_NAME);
                return;
            }

            if (value instanceof UserStreamAcl) {
                gen.writeString(UserStreamAcl.ACL_NAME);
                return;
            }

            gen.writePOJO(value);
        }
    }

    public static class Deserializer extends JsonDeserializer<Acl> {
        @Override
        public Acl deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String value = p.getText();

                if (value.equals(UserStreamAcl.ACL_NAME))
                    return UserStreamAcl.getInstance();

                if (value.equals(SystemStreamAcl.ACL_NAME)) {
                    return SystemStreamAcl.getInstance();
                }

                throw new IOException(String.format("Unknown ACL type '%s'", value));
            }

            return p.readValueAs(StreamAcl.class);
        }
    }
}
