package com.eventstore.dbclient.misc;


import com.eventstore.dbclient.StreamState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpectedRevisionTests {

    @Test
    public void testExpectedRevisionEquality() {
        Assertions.assertEquals(StreamState.any(), new StreamState.AnyStreamState());
        Assertions.assertEquals(StreamState.noStream(), new StreamState.NoStreamState());
        Assertions.assertEquals(StreamState.streamExists(), new StreamState.StreamExistsState());
        Assertions.assertEquals(StreamState.streamRevision(1L), StreamState.streamRevision(1L));
    }

    @Test
    public void testExpectedRevisionNonEquality() {
        Assertions.assertNotEquals(StreamState.any(), StreamState.noStream());
        Assertions.assertNotEquals(StreamState.any(), StreamState.streamExists());
        Assertions.assertNotEquals(StreamState.any(), StreamState.streamRevision(0L));
        Assertions.assertNotEquals(StreamState.noStream(), StreamState.streamExists());
        Assertions.assertNotEquals(StreamState.noStream(), StreamState.streamRevision(0L));
        Assertions.assertNotEquals(StreamState.streamExists(), StreamState.streamRevision(0L));
    }

    @Test
    public void testExpectedRevisionHashCode() {
        Assertions.assertEquals(StreamState.any().hashCode(), new StreamState.AnyStreamState().hashCode());
        Assertions.assertEquals(StreamState.noStream().hashCode(), new StreamState.NoStreamState().hashCode());
        Assertions.assertEquals(StreamState.streamExists().hashCode(), new StreamState.StreamExistsState().hashCode());
        Assertions.assertEquals(StreamState.streamRevision(1L).hashCode(), StreamState.streamRevision(1L).hashCode());
    }

    @Test
    public void testHumanRepresentation() {
        Assertions.assertEquals("AnyStreamState", StreamState.any().toString());
        Assertions.assertEquals("StreamExistsState", StreamState.streamExists().toString());
        Assertions.assertEquals("NoStreamStream", StreamState.noStream().toString());
        Assertions.assertEquals("42", StreamState.streamRevision(42).toString());
    }

    @Test
    public void testRawLong() {
        Assertions.assertEquals(-2, StreamState.any().toRawLong());
        Assertions.assertEquals(-1, StreamState.noStream().toRawLong());
        Assertions.assertEquals(-4, StreamState.streamExists().toRawLong());
        Assertions.assertEquals(42, StreamState.streamRevision(42).toRawLong());
    }

    @Test
    public void testRawLongConversion() {
        Assertions.assertEquals(StreamState.fromRawLong(-2), StreamState.any());
        Assertions.assertEquals(StreamState.fromRawLong(-1), StreamState.noStream());
        Assertions.assertEquals(StreamState.fromRawLong(-4), StreamState.streamExists());
        Assertions.assertEquals(StreamState.fromRawLong(42), StreamState.streamRevision(42));
        Assertions.assertThrowsExactly(RuntimeException.class, () -> StreamState.fromRawLong(-5));
    }
}
