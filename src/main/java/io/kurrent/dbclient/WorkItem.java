package io.kurrent.dbclient;

interface WorkItem {
    void accept(WorkItemArgs args, Exception error);
}