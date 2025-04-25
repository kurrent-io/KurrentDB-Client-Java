package io.kurrent.dbclient;

import java.util.List;

class ListProjectionsResult {
    private final List<ProjectionDetails> projections;

    public ListProjectionsResult(List<ProjectionDetails> projections) {
        this.projections = projections;
    }

    public List<ProjectionDetails> getProjections() {
        return this.projections;
    }
}
