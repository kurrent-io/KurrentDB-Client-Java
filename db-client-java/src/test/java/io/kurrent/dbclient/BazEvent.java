package io.kurrent.dbclient;

import java.util.Objects;

public class BazEvent {
    private String name;
    private int age;

    public BazEvent() {}

    public BazEvent(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BazEvent bazEvent = (BazEvent) o;
        return age == bazEvent.age && Objects.equals(name, bazEvent.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
