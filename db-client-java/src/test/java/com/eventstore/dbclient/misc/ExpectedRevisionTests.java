package io.kurrent.dbclient.misc;


import io.kurrent.dbclient.ExpectedRevision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpectedRevisionTests {

    @Test
    public void testExpectedRevisionEquality() {
        Assertions.assertEquals(ExpectedRevision.any(), new ExpectedRevision.AnyExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.noStream(), new ExpectedRevision.NoStreamExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.streamExists(), new ExpectedRevision.StreamExistsExpectedRevision());
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1L), ExpectedRevision.expectedRevision(1L));
    }

    @Test
    public void testExpectedRevisionNonEquality() {
        Assertions.assertNotEquals(ExpectedRevision.any(), ExpectedRevision.noStream());
        Assertions.assertNotEquals(ExpectedRevision.any(), ExpectedRevision.streamExists());
        Assertions.assertNotEquals(ExpectedRevision.any(), ExpectedRevision.expectedRevision(0L));
        Assertions.assertNotEquals(ExpectedRevision.noStream(), ExpectedRevision.streamExists());
        Assertions.assertNotEquals(ExpectedRevision.noStream(), ExpectedRevision.expectedRevision(0L));
        Assertions.assertNotEquals(ExpectedRevision.streamExists(), ExpectedRevision.expectedRevision(0L));
    }

    @Test
    public void testExpectedRevisionHashCode() {
        Assertions.assertEquals(ExpectedRevision.any().hashCode(), new ExpectedRevision.AnyExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.noStream().hashCode(), new ExpectedRevision.NoStreamExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.streamExists().hashCode(), new ExpectedRevision.StreamExistsExpectedRevision().hashCode());
        Assertions.assertEquals(ExpectedRevision.expectedRevision(1L).hashCode(), ExpectedRevision.expectedRevision(1L).hashCode());
    }

    @Test
    public void testHumanRepresentation() {
        Assertions.assertEquals("ExpectedAny", ExpectedRevision.any().toString());
        Assertions.assertEquals("ExpectedStreamExists", ExpectedRevision.streamExists().toString());
        Assertions.assertEquals("ExpectedNoStream", ExpectedRevision.noStream().toString());
        Assertions.assertEquals("42", ExpectedRevision.expectedRevision(42).toString());
    }

    @Test
    public void testRawLong() {
        Assertions.assertEquals(-2, ExpectedRevision.any().toRawLong());
        Assertions.assertEquals(-1, ExpectedRevision.noStream().toRawLong());
        Assertions.assertEquals(-4, ExpectedRevision.streamExists().toRawLong());
        Assertions.assertEquals(42, ExpectedRevision.expectedRevision(42).toRawLong());
    }

    @Test
    public void testRawLongConversion() {
        Assertions.assertEquals(ExpectedRevision.fromRawLong(-2), ExpectedRevision.any());
        Assertions.assertEquals(ExpectedRevision.fromRawLong(-1), ExpectedRevision.noStream());
        Assertions.assertEquals(ExpectedRevision.fromRawLong(-4), ExpectedRevision.streamExists());
        Assertions.assertEquals(ExpectedRevision.fromRawLong(42), ExpectedRevision.expectedRevision(42));
        Assertions.assertThrowsExactly(RuntimeException.class, () -> ExpectedRevision.fromRawLong(-5));
    }
}
