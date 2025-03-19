package io.kurrent.dbclient.serialization;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

interface MessageTypeRegistry {
    void register(Map<Class<?>, String> messageTypeMap);
    
    void register(Class<?> messageType, String messageTypeName);

    Optional<String> getTypeName(Class<?> messageType);

    String getOrAddTypeName(Class<?> javaClass, Function<Class<?>, String> getTypeName);

    Optional<Class<?>> getJavaClass(String messageTypeName);

    Optional<Class<?>> getOrAddJavaClass(String messageTypeName, Function<String, Optional<Class<?>>> getJavaClass);
}

class MessageTypeRegistryImpl implements MessageTypeRegistry {
    private final ConcurrentHashMap<String, Class<?>> classMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, String> typeNameMap = new ConcurrentHashMap<>();


    @Override
    public void register(Map<Class<?>, String> messageTypeMap) {
        for (Map.Entry<Class<?>, String> entry : messageTypeMap.entrySet()) {
            register(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void register(Class<?> messageType, String messageTypeName) {
        classMap.put(messageTypeName, messageType);
        typeNameMap.put(messageType, messageTypeName);
    }

    @Override
    public Optional<String> getTypeName(Class<?> messageType) {
        return Optional.ofNullable(typeNameMap.getOrDefault(messageType, null));
    }

    @Override
    public String getOrAddTypeName(Class<?> javaClass, Function<Class<?>, String> getTypeName) {
        return typeNameMap.computeIfAbsent(
                javaClass,
                c -> getTypeName.apply(javaClass)
        );
    }

    @Override
    public Optional<Class<?>> getJavaClass(String messageTypeName) {
        return Optional.ofNullable(classMap.getOrDefault(messageTypeName, null));
    }

    @Override
    public Optional<Class<?>> getOrAddJavaClass(String messageTypeName, Function<String, Optional<Class<?>>> getJavaClass) {
        return Optional.ofNullable(classMap.computeIfAbsent(
                messageTypeName,
                c -> getJavaClass.apply(messageTypeName).orElse(null)
        ));
    }
}
