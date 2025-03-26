package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Subscription filter that targets $all stream.
 */
public class SubscriptionFilter {
    @NotNull
    private final EventFilter filter;

    private final int checkpointIntervalUnsigned;
    private final Checkpointer checkpointer;

    /**
     * Creates a new subscription filter builder.
     * @see SubscriptionFilterBuilder
     * @return a builder.
     */
    public static SubscriptionFilterBuilder newBuilder() {
        return new SubscriptionFilterBuilder();
    }

    SubscriptionFilter(@NotNull final EventFilter filter) {
        this.filter = filter;
        this.checkpointer = null;
        this.checkpointIntervalUnsigned = 1; // TODO(jen20): Review this default
    }

    SubscriptionFilter(@NotNull final EventFilter filter,
                              final int checkpointIntervalUnsigned,
                              @NotNull final Checkpointer checkpointer) {
        this.filter = filter;
        this.checkpointer = checkpointer;
        this.checkpointIntervalUnsigned = checkpointIntervalUnsigned;
    }

    Checkpointer getCheckpointer() {
        return checkpointer;
    }

    void addToWireStreamsReadReq(StreamsOuterClass.ReadReq.Options.Builder builder) {
        builder.setFilter(new StreamsReadReqWireBuilder(builder).build());
    }

    void addToWirePersistentCreateReq(Persistent.CreateReq.AllOptions.Builder builder) {
        builder.setFilter(new PersistentReadReqWireBuilder(builder).build());
    }

    private class PersistentReadReqWireBuilder
            extends FilterWireBase<Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder> {
        Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder expression = null;
        Persistent.CreateReq.AllOptions.FilterOptions.Builder filter = null;
        Persistent.CreateReq.AllOptions.Builder allOptionsBuilder = null;

        PersistentReadReqWireBuilder(Persistent.CreateReq.AllOptions.Builder builder) {
            expression = Persistent.CreateReq.AllOptions.FilterOptions.Expression.newBuilder();
            filter = Persistent.CreateReq.AllOptions.FilterOptions.newBuilder();
            allOptionsBuilder = builder;
        }

        public Persistent.CreateReq.AllOptions.FilterOptions.Builder build() {
            internalBuild();
            return filter;
        }

        Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder newExprBuilder() {
            return Persistent.CreateReq.AllOptions.FilterOptions.Expression.newBuilder();
        }

        Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder setRegEx(String regEx) {
            expression.setRegex(regEx);
            return expression;
        }

        void addPrefix(Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder builder, String prefix) {
            builder.setRegex(prefix);
        }

        void setNoFilter() {
            allOptionsBuilder.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        void setStreamIdentifier(Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder expression) {
            filter.setStreamIdentifier(expression);
        }

        void setEventType(Persistent.CreateReq.AllOptions.FilterOptions.Expression.Builder expression) {
            filter.setEventType(expression);
        }

        void setMaxSearchWindow(Integer count) {
            filter.setMax(count);
        }

        void setSearchWindowCount() {
            filter.setCount(Shared.Empty.getDefaultInstance());
        }
    }

    private class StreamsReadReqWireBuilder
            extends FilterWireBase<StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder> {
        StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder expression = null;
        StreamsOuterClass.ReadReq.Options.FilterOptions.Builder filter = null;
        StreamsOuterClass.ReadReq.Options.Builder allOptionsBuilder = null;

        StreamsReadReqWireBuilder(StreamsOuterClass.ReadReq.Options.Builder builder) {
            expression = StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.newBuilder();
            filter = StreamsOuterClass.ReadReq.Options.FilterOptions.newBuilder();
            allOptionsBuilder = builder;
        }

        public StreamsOuterClass.ReadReq.Options.FilterOptions.Builder build() {
            internalBuild();
            filter.setCheckpointIntervalMultiplier(checkpointIntervalUnsigned);
            return filter;
        }

        StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder newExprBuilder() {
            return StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.newBuilder();
        }

        StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder setRegEx(String regEx) {
            expression.setRegex(regEx);
            return expression;
        }

        void addPrefix(StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder builder, String prefix) {
            builder.setRegex(prefix);
        }

        void setNoFilter() {
            allOptionsBuilder.setNoFilter(Shared.Empty.getDefaultInstance());
        }

        void setStreamIdentifier(StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder expression) {
            filter.setStreamIdentifier(expression);
        }

        void setEventType(StreamsOuterClass.ReadReq.Options.FilterOptions.Expression.Builder expression) {
            filter.setEventType(expression);
        }

        void setMaxSearchWindow(Integer count) {
            filter.setMax(count);
        }

        void setSearchWindowCount() {
            filter.setCount(Shared.Empty.getDefaultInstance());
        }
    }

    private abstract class FilterWireBase<TE> {
        void internalBuild(){
            RegularFilterExpression regex = filter.getRegularFilterExpression();
            PrefixFilterExpression[] prefixes = filter.getPrefixFilterExpressions();
            Optional<Integer> maxSearchWindow = filter.getMaxSearchWindow();

            if (regex != null && prefixes != null && prefixes.length != 0) {
                throw new IllegalArgumentException("Regex and Prefix expressions are mutually exclusive");
            }

            TE expression = null;

            if (regex != null) {
                expression = setRegEx(regex.toString());
            }

            if (prefixes != null && prefixes.length > 0) {
                TE tmp = newExprBuilder();

                Stream.of(prefixes)
                        .map(Object::toString)
                        .filter(Objects::nonNull)
                        .distinct()
                        .forEach((s) -> addPrefix(tmp, s));

                expression = tmp;
            }

            if (expression == null) {
                setNoFilter();
                return;
            }

            if (filter instanceof StreamFilter) {
                setStreamIdentifier(expression);
            }

            if (filter instanceof EventTypeFilter) {
                setEventType(expression);
            }

            if (maxSearchWindow != null && maxSearchWindow.isPresent()) {
                setMaxSearchWindow(maxSearchWindow.get());
            } else {
                setSearchWindowCount();
            }
        }

        abstract TE setRegEx(String regEx);
        abstract TE newExprBuilder();
        abstract void addPrefix(TE builder, String prefix);
        abstract void setNoFilter();
        abstract void setStreamIdentifier(TE expression);
        abstract void setEventType(TE expression);
        abstract void setMaxSearchWindow(Integer count);
        abstract void setSearchWindowCount();
    }
}

