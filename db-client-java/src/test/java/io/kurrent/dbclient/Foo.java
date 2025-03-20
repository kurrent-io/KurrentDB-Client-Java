package io.kurrent.dbclient;

import java.util.Objects;

public class Foo {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Foo foo1 = (Foo) o;
        return foo == foo1.foo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(foo);
    }

    private boolean foo;

    public boolean isFoo() {
        return foo;
    }

    public void setFoo(boolean foo) {
        this.foo = foo;
    }
}

