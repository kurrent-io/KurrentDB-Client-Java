package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

class UpdatePersistentSubscriptionToStream extends AbstractUpdatePersistentSubscription {
    private final PersistentSubscriptionToStreamSettings settings;
    private final String stream;

    public UpdatePersistentSubscriptionToStream(GrpcClient connection, String stream, String group,
                                                UpdatePersistentSubscriptionToStreamOptions options) {
        super(connection, group, options.getSettings(), options);

        this.stream = stream;
        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.UpdateReq.Settings.Builder createSettings() {
        return Persistent.UpdateReq.Settings.newBuilder();
    }

    @Override
    @SuppressWarnings("deprecation")
    // As long as the 20.10LTS, we have to use that setStreamIdentifier deprecated call.
    protected Persistent.UpdateReq.Options.Builder createOptions() {
        Persistent.UpdateReq.Options.Builder optionsBuilder = Persistent.UpdateReq.Options.newBuilder();
        Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();
        Persistent.UpdateReq.StreamOptions.Builder streamOptionsBuilder = Persistent.UpdateReq.StreamOptions
                .newBuilder();

        StreamPosition<Long> position = settings.getStartFrom();
        if (position instanceof StreamPosition.Start) {
            streamOptionsBuilder.setStart(Shared.Empty.newBuilder());
        } else if (position instanceof StreamPosition.End) {
            streamOptionsBuilder.setEnd(Shared.Empty.newBuilder());
        } else {
            streamOptionsBuilder.setRevision(position.getPositionOrThrow());
        }

        streamIdentifierBuilder.setStreamName(ByteString.copyFromUtf8(stream));
        streamOptionsBuilder.setStreamIdentifier(streamIdentifierBuilder);
        optionsBuilder.setStream(streamOptionsBuilder);
        optionsBuilder.setStreamIdentifier(streamIdentifierBuilder);

        return optionsBuilder;
    }
}
