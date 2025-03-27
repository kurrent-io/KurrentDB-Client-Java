package io.kurrent.dbclient;

import java.util.UUID;

class RunWorkItem implements Msg {
    final UUID msgId;
    final WorkItem item;

    public RunWorkItem(UUID msgId, WorkItem item) {
        this.msgId = msgId;
        this.item = item;
    }

    public UUID getMsgId() {
        return msgId;
    }

    public WorkItem getItem() {
        return item;
    }

    public void reportError(Exception e) {
        this.item.accept(null, e);
    }

    @Override
    public String toString() {
        return "RunWorkItem[" + msgId + "]";
    }

    @Override
    public void accept(ConnectionService connectionService) {
        connectionService.process(this);
    }
}