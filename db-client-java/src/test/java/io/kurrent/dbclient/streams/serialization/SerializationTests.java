package io.kurrent.dbclient.streams.serialization;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public interface SerializationTests extends ConnectionAware {
    @Test
    default void testPlainJavaObjectsAreSerializedAndDeserializedUsingAutoSerialization() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();

        // Given
        final String streamName = generateName();
        final Object[] expected = generateMessages(2).toArray();
        
        // When
        AppendToStreamOptions appendOptions = AppendToStreamOptions.get()
                .expectedRevision(ExpectedRevision.noStream());

        WriteResult appendResult = client.appendToStream(streamName, appendOptions, expected)
                .get();

        Assertions.assertEquals(ExpectedRevision.expectedRevision(1), appendResult.getNextExpectedRevision());
        
        // Ensure appended event is readable
        ReadResult result = client.readStream(streamName, ReadStreamOptions.get())
                .get();

        Assertions.assertEquals(2, result.getEvents().size());
    }

    static List<UserRegistered> generateMessages(int count){
        return IntStream.range(0, count)
                .mapToObj(x -> 
                        new UserRegistered(
                        UUID.randomUUID(),
                        new Address(UUID.randomUUID().toString(), UUID.randomUUID().hashCode())
                        )
                )
                .collect(Collectors.toList());
    }
    
    class Address{
        String street;
        int number;

        public Address(String street, int number) {
            this.street = street;
            this.number = number;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    class UserRegistered{
        UUID userId;
        Address address;

        public UserRegistered(UUID userId, Address address) {
            this.userId = userId;
            this.address = address;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }
}
