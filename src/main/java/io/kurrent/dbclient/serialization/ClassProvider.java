package io.kurrent.dbclient.serialization;

import java.util.Optional;

public class ClassProvider {
    public static Optional<Class<?>> getClassByName(String fullName) {
        try {
            return Optional.of(Class.forName(fullName));
        } catch (ClassNotFoundException e) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader == null)
                return Optional.empty();
            
            try {
                return Optional.ofNullable(contextLoader.loadClass(fullName));
            } catch (ClassNotFoundException ignored) {
                return Optional.empty();
            }            
        }
    }
}
