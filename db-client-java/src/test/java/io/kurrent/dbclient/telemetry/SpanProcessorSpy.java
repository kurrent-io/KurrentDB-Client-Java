package io.kurrent.dbclient.telemetry;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class SpanProcessorSpy implements SpanProcessor {
    private final List<Consumer<ReadableSpan>> spanEndedHooks;

    public SpanProcessorSpy(List<Consumer<ReadableSpan>> spanEndedHooks) {
        this.spanEndedHooks = spanEndedHooks;
    }

    @Override
    public void onStart(@NotNull Context context, @NotNull ReadWriteSpan readWriteSpan) {
        // Do nothing.
    }

    @Override
    public boolean isStartRequired() {
        return false;
    }

    @Override
    public void onEnd(@NotNull ReadableSpan readableSpan) {
        spanEndedHooks.forEach(hook -> hook.accept(readableSpan));
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
