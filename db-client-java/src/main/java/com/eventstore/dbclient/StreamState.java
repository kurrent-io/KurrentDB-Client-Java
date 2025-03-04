package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.shared.Shared;
import com.eventstore.dbclient.proto.streams.StreamsOuterClass;

import java.util.Objects;

/**
 * Constants used for expected revision control.
 * <p>
 *
 * The EventStoreDB server will assure idempotency for all requests using any value in <i>ExpectedRevision</i> except
 * <i>ANY</i>. When using <i>ANY</i>, the EventStoreDB server will do its best to assure idempotency but will not
 * guarantee it. Any other <i>ExpectedRevision</i> instances are meant for optimistic concurrency checks.
 * </p>
 */
public abstract class StreamState {
    /**
     * This writes should not conflict with anything and should always succeed.
     */
    public static StreamState any() {
        return new AnyStreamState();
    }

    /**
     * The stream being written to should not yet exist. If it does exist, treats that as a concurrency problem.
     */
    public static StreamState noStream() {
        return new NoStreamState();
    }

    /**
     * The stream should exist. If it or a metadata stream does not exist, treats that as a concurrency problem.
     */
    public static StreamState streamExists() {
        return new StreamExistsState();
    }

    /**
     * States that the last event written to the stream should have an event revision matching your expected value.
     */
    public static StreamState streamRevision(long revision) {
        return new StreamRevisionStreamState(revision);
    }

    public static StreamState fromRawLong(long revision) {
        if (revision == -1)
            return StreamState.noStream();
        if (revision == -2)
            return StreamState.any();
        if (revision == -4)
            return StreamState.streamExists();

        if (revision < 0)
            throw new RuntimeException(String.format("Invalid stream revision long representation '%s'", revision));

        return StreamState.streamRevision(revision);
    }

    StreamState() {}

    abstract StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options);
    abstract StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options);
    abstract StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options);

    public long toRawLong() {
        if (this instanceof NoStreamState)
            return -1;

        if (this instanceof AnyStreamState)
            return -2;

        if (this instanceof StreamExistsState)
            return -4;

        StreamRevisionStreamState revision = (StreamRevisionStreamState) this;

        return revision.version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    public static class NoStreamState extends StreamState {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setNoStream(Shared.Empty.getDefaultInstance());
        }

        @Override
        public String toString() {
            return "NoStreamState";
        }
    }

    public static class AnyStreamState extends StreamState {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setAny(Shared.Empty.getDefaultInstance());
        }

        @Override
        public String toString() {
            return "AnyStreamState";
        }
    }

    public static class StreamExistsState extends StreamState {
        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setStreamExists(Shared.Empty.getDefaultInstance());
        }

        @Override
        public String toString() {
            return "StreamsExistsState";
        }
    }

    public static class StreamRevisionStreamState extends StreamState {
        final long version;

        StreamRevisionStreamState(long version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StreamRevisionStreamState that = (StreamRevisionStreamState) o;
            return version == that.version;
        }

        @Override
        public int hashCode() {
            return Objects.hash(version);
        }

        @Override
        public StreamsOuterClass.AppendReq.Options.Builder applyOnWire(StreamsOuterClass.AppendReq.Options.Builder options) {
            return options.setRevision(version);
        }

        @Override
        public StreamsOuterClass.DeleteReq.Options.Builder applyOnWire(StreamsOuterClass.DeleteReq.Options.Builder options) {
            return options.setRevision(version);
        }

        @Override
        public StreamsOuterClass.TombstoneReq.Options.Builder applyOnWire(StreamsOuterClass.TombstoneReq.Options.Builder options) {
            return options.setRevision(version);
        }

        @Override
        public String toString() {
            return Long.toString(this.version);
        }
    }
}
