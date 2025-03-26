package io.kurrent.dbclient.misc;

import io.kurrent.dbclient.ServerVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerVersionTests {
    @Test
    public void testEquals() {
        ServerVersion v = new ServerVersion(22, 6, 123);

        Assertions.assertTrue(v.equals(22, 6, 123));

        Assertions.assertFalse(v.equals(21, 6, 123));
        Assertions.assertFalse(v.equals(22, 5, 123));
        Assertions.assertFalse(v.equals(22, 6, 124));
    }

    @Test
    public void testIsLessThan() {
        ServerVersion v = new ServerVersion(22, 6, 123);

        Assertions.assertFalse(v.isLessThan(22, 6, 123));

        Assertions.assertTrue(v.isLessThan(23, 6, 123));
        Assertions.assertTrue(v.isLessThan(22, 7, 123));
        Assertions.assertTrue(v.isLessThan(22, 6, 124));

        Assertions.assertFalse(v.isLessThan(21, 6, 123));
        Assertions.assertFalse(v.isLessThan(22, 5, 123));
        Assertions.assertFalse(v.isLessThan(22, 6, 122));

        Assertions.assertTrue(v.isLessThan(23, 5, 123));
    }

    @Test
    public void testIsGreaterThan() {
        ServerVersion v = new ServerVersion(22, 6, 123);

        Assertions.assertFalse(v.isGreaterThan(22, 6, 123));

        Assertions.assertTrue(v.isGreaterThan(21, 6, 123));
        Assertions.assertTrue(v.isGreaterThan(22, 5, 123));
        Assertions.assertTrue(v.isGreaterThan(22, 6, 122));

        Assertions.assertFalse(v.isGreaterThan(23, 6, 123));
        Assertions.assertFalse(v.isGreaterThan(22, 7, 123));
        Assertions.assertFalse(v.isGreaterThan(22, 6, 124));

        Assertions.assertTrue(v.isGreaterThan(21, 7, 123));
    }
}
