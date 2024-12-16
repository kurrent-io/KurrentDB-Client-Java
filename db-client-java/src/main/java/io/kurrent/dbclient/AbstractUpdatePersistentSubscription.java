package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.persistentsubscriptions.PersistentSubscriptionsGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

abstract class AbstractUpdatePersistentSubscription {
    private final GrpcClient client;
    private final String group;
    private final PersistentSubscriptionSettings settings;
    private final OptionsBase<?> options;
    private static final Logger logger = LoggerFactory.getLogger(AbstractUpdatePersistentSubscription.class);

    public AbstractUpdatePersistentSubscription(GrpcClient client, String group,
                                                PersistentSubscriptionSettings settings, OptionsBase<?> options) {
        this.client = client;
        this.group = group;
        this.settings = settings;
        this.options = options;
    }

    protected Persistent.UpdateReq.Settings.Builder createSettings() {
        return Persistent.UpdateReq.Settings.newBuilder();
    }

    protected abstract Persistent.UpdateReq.Options.Builder createOptions();

    @SuppressWarnings("unchecked")
    public CompletableFuture execute() {
        return this.client.runWithArgs(args -> {
            CompletableFuture result = new CompletableFuture();
            PersistentSubscriptionsGrpc.PersistentSubscriptionsStub client =
                    GrpcUtils.configureStub(PersistentSubscriptionsGrpc.newStub(args.getChannel()), this.client.getSettings(), this.options);
            Persistent.UpdateReq.Settings.Builder settingsBuilder = createSettings();

            settingsBuilder
                    .setResolveLinks(settings.shouldResolveLinkTos())
                    .setReadBatchSize((int)settings.getReadBatchSize())
                    .setMinCheckpointCount((int)settings.getCheckpointLowerBound())
                    .setMaxCheckpointCount((int)settings.getCheckpointUpperBound())
                    .setMessageTimeoutMs((int)settings.getMessageTimeoutMs())
                    .setMaxSubscriberCount((int)settings.getMaxSubscriberCount())
                    .setMaxRetryCount((int)settings.getMaxRetryCount())
                    .setLiveBufferSize((int)settings.getLiveBufferSize())
                    .setHistoryBufferSize((int)settings.getHistoryBufferSize())
                    .setExtraStatistics(settings.isExtraStatistics())
                    .setCheckpointAfterMs((int)settings.getCheckpointAfterInMs());

            if (settings.getNamedConsumerStrategy().isDispatchToSingle()) {
                settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.DispatchToSingle);
            } else if (settings.getNamedConsumerStrategy().isRoundRobin()) {
                settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.RoundRobin);
            } else if (settings.getNamedConsumerStrategy().isPinned()) {
                settingsBuilder.setNamedConsumerStrategy(Persistent.UpdateReq.ConsumerStrategy.Pinned);
            } else {
                logger.error(String.format("Unsupported named consumer strategy: '%s'", settings.getNamedConsumerStrategy().toString()));
                throw new UnsupportedFeatureException();
            }

            Persistent.UpdateReq req = Persistent.UpdateReq.newBuilder()
                    .setOptions(createOptions()
                            .setSettings(settingsBuilder)
                            .setGroupName(group))
                    .build();

            if (req.getOptions().hasAll() && !args.supportFeature(FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL)) {
                result.completeExceptionally(new UnsupportedFeatureException());
            } else {
                client.update(req, GrpcUtils.convertSingleResponse(result));
            }

            return result;
        });
    }
}
