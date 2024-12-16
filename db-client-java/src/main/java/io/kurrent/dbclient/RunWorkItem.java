package io.kurrent.dbclient;

class RunWorkItem implements Msg {
    final String msgId;
    final WorkItem item;

    public RunWorkItem(String msgId, WorkItem item) {
        this.msgId = msgId;
        this.item = item;
    }

    public String getMsgId() {
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