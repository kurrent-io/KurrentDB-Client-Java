package io.kurrent.dbclient.misc;

import io.kurrent.dbclient.Acl;
import io.kurrent.dbclient.Acls;
import io.kurrent.dbclient.StreamMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class OfflineMetadataTests {
    @Test
    public void testSerializationIsoMorphism() throws Throwable {
        StreamMetadata expected = new StreamMetadata();
        HashMap<String, Object> custom = new HashMap<>();

        custom.put("foo", "bar");

        expected.setMaxAge(2L);
        expected.setCacheControl(15L);
        expected.setTruncateBefore(1L);
        expected.setMaxCount(12L);

        Acl acl = Acls.newStreamAcl()
                .addReadRoles("admin")
                .addWriteRoles("admin")
                .addDeleteRoles("admin")
                .addMetaReadRoles("admin")
                .addMetaWriteRoles("admin");

        expected.setAcl(acl);
        expected.setCustomProperties(custom);

        ObjectMapper mapper = new ObjectMapper();
        StreamMetadata actual = mapper.readValue(mapper.writeValueAsString(expected), StreamMetadata.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testNullAcl() throws Throwable {
        StreamMetadata expected = new StreamMetadata();

        expected.setMaxAge(2L);
        expected.setCacheControl(15L);
        expected.setTruncateBefore(1L);
        expected.setMaxCount(12L);

        ObjectMapper mapper = new ObjectMapper();
        StreamMetadata actual = mapper.readValue(mapper.writeValueAsString(expected), StreamMetadata.class);

        Assertions.assertEquals(expected, actual);
    }
}
