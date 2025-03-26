package io.kurrent.dbclient.misc;

import io.kurrent.dbclient.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class PositionTests {
    static final String largeUnsignedGreater = "9223372036854776000";
    static final String largeUnsignedLess = "9223372036854775000";

    @Test
    public void testConstructionWithCommitLessThanPrepareLong() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Position(1000, 12345);
        });
    }

    @Test
    public void testConstructionWithCommitLessThanPrepareString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Position("12345", "1000");
        });
    }

    @Test
    public void testConstructionWithCommitLessThanPrepareLargeUnsigned() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Position(largeUnsignedGreater, largeUnsignedLess);
        });
    }

    @Test
    public void testPositionEquality() {
        assertEquals(new Position(12345, 10000),
                new Position(12345, 10000));

        assertEquals(new Position(largeUnsignedLess, largeUnsignedGreater),
                new Position(largeUnsignedLess, largeUnsignedGreater));
    }

    @Test
    public void testPositionComparable() {
        Position position1 = new Position(largeUnsignedLess, largeUnsignedGreater);
        Position position2 = new Position(largeUnsignedLess, largeUnsignedGreater);
        assertEquals(0, position1.compareTo(position2));

        Position position3 = new Position(12345, 10000);
        Position position4 = new Position(12345, 11000);
        assertTrue(position3.compareTo(position4) < 0);

        // Whilst both of these should not exist in the same database, the actual
        // equality rules should still hold.
        Position position5 = new Position(12345, 10000);
        Position position6 = new Position(20000, 10000);
        assertTrue(position5.compareTo(position6) < 0);
    }

    @Test
    public void testPositionString() {
        assertEquals("12345/10000", new Position(12345, 10000).toString());
    }
}
