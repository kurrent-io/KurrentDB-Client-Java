package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.ConnectionAware;
import io.kurrent.dbclient.KurrentDBClient;
import io.kurrent.dbclient.ReadStreamOptions;
import io.grpc.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public interface InterceptorTests extends ConnectionAware {
    @Test
    default void testInterceptorIsCalled() {
        AtomicInteger atom = new AtomicInteger(0);
        KurrentDBClient client = getDatabase().connectWith(opts -> opts.addInterceptor(new MyInterceptor(atom)));

        try {
            client.readStream("foobar", ReadStreamOptions.get()).get();
        } catch (Exception e) {
            // We don't care.
        }

        Assertions.assertEquals(42, atom.get());
    }

    class MyInterceptor implements ClientInterceptor {
        final AtomicInteger atom;

        MyInterceptor(AtomicInteger atom) {
            this.atom = atom;
        }
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
            atom.set(42);
            return next.newCall(method, callOptions);
        }
    }
}
