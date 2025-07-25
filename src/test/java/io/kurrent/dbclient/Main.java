package io.kurrent.dbclient;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.setProperty("org.slf4j.simpleLogger.log.io.kurrent.dbclient", "trace");
        System.setProperty("org.slf4j.simpleLogger.log.io.netty", "trace");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss.SSS");

        KurrentDBClientSettings settings = KurrentDBConnectionString.parseOrThrow("kurrentdb://localhost:2113?tls=false");
        KurrentDBClient client = KurrentDBClient.create(settings);

        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        Publisher<ReadMessage> publisher = client.readAllReactive(options);

        final CountDownLatch latch = new CountDownLatch(1);
        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent event = readMessage.getEvent().getOriginalEvent();

                if (!event.getEventType().startsWith("$")) {
                    System.out.println("Event: " + event.getEventType());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error type: " + throwable.getClass().getSimpleName());
                System.out.println("Error message: " + throwable.getMessage());
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        latch.await();
    }
}