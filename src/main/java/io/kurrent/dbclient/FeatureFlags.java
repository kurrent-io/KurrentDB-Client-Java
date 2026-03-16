package io.kurrent.dbclient;
class FeatureFlags {
    public final static int NOTHING = 0;
    public final static int BATCH_APPEND = 1;
    public final static int PERSISTENT_SUBSCRIPTION_LIST = 2;
    public final static int PERSISTENT_SUBSCRIPTION_REPLAY = 4;
    public final static int PERSISTENT_SUBSCRIPTION_RESTART_SUBSYSTEM = 8;
    public final static int PERSISTENT_SUBSCRIPTION_GET_INFO = 16;
    public final static int PERSISTENT_SUBSCRIPTION_TO_ALL = 32;
    public final static int MULTI_STREAM_APPEND = 64;
    public final static int APPEND_RECORDS = 128;
    public final static int PERSISTENT_SUBSCRIPTION_MANAGEMENT = PERSISTENT_SUBSCRIPTION_LIST | PERSISTENT_SUBSCRIPTION_REPLAY | PERSISTENT_SUBSCRIPTION_GET_INFO | PERSISTENT_SUBSCRIPTION_RESTART_SUBSYSTEM;
}
