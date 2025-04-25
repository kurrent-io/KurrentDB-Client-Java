package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public interface MetadataTests extends ConnectionAware {
    @Test
    default void testSetStreamMetadata() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();

        StreamMetadata metadata = new StreamMetadata();

        metadata.setMaxAge(2L);
        metadata.setCacheControl(15L);
        metadata.setTruncateBefore(1L);
        metadata.setMaxCount(12L);

        Acl acl = Acls.newStreamAcl()
                .addReadRoles("admin")
                .addWriteRoles("admin")
                .addDeleteRoles("admin")
                .addMetaReadRoles("admin")
                .addMetaWriteRoles("admin");

        metadata.setAcl(acl);

        byte[] payload = "data".getBytes();

        String streamName = generateName();

        client.appendToStream(streamName, EventDataBuilder.json("foo", payload).build()).get();
        client.setStreamMetadata(streamName, metadata).get();

        StreamMetadata got = client.getStreamMetadata(streamName).get();

        Assertions.assertEquals(metadata, got);
    }

    @Test
    default void testReadNoExistingMetadata() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        String streamName = generateName();
        client.appendToStream(streamName, EventDataBuilder.json("bar", "data".getBytes()).build()).get();

        StreamMetadata got = client.getStreamMetadata(streamName).get();

        Assertions.assertEquals(new StreamMetadata(), got);
    }

    @Test
    default void testReadMetadataAfterStreamDeletion() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        String streamName = generateName();
        client.appendToStream(streamName, EventDataBuilder.json("bar", "data".getBytes()).build()).get();

        client.deleteStream(streamName).get();
        client.getStreamMetadata(streamName).get();
    }
}