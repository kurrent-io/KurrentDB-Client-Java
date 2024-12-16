package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;

class CreatePersistentSubscriptionToStream extends AbstractCreatePersistentSubscription<Long, PersistentSubscriptionToStreamSettings> {
    private final String stream;
    private final PersistentSubscriptionToStreamSettings settings;

    public CreatePersistentSubscriptionToStream(GrpcClient client, String stream, String group,
                                                CreatePersistentSubscriptionToStreamOptions options) {
        super(client, group, options.getSettings(), options);
        this.stream = stream;
        this.settings = options.getSettings();
    }

    @Override
    protected Persistent.CreateReq.Settings.Builder createSettings() {
        return Persistent.CreateReq.Settings.newBuilder();
    }

    @Override
    @SuppressWarnings("deprecation")
    // We have to support the setStreamIdentifier call while 20.10LTS is still supported.
    protected Persistent.CreateReq.Options.Builder createOptions() {
        Persistent.CreateReq.Options.Builder optionsBuilder = Persistent.CreateReq.Options.newBuilder();
        Shared.StreamIdentifier.Builder streamIdentifierBuilder = Shared.StreamIdentifier.newBuilder();
        Persistent.CreateReq.StreamOptions.Builder streamOptionsBuilder = Persistent.CreateReq.StreamOptions
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
