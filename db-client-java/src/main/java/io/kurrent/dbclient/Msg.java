package io.kurrent.dbclient;

interface Msg {
    void accept(ConnectionService handler);
}
