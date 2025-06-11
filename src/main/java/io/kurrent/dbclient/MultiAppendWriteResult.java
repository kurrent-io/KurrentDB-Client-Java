package io.kurrent.dbclient;

import java.util.List;
import java.util.Optional;

public class MultiAppendWriteResult {
    private final List<AppendStreamSuccess> successes;
    private final List<AppendStreamFailure> failures;

    public MultiAppendWriteResult(List<AppendStreamSuccess> successes, List<AppendStreamFailure> failures) {
        this.successes = successes;
        this.failures = failures;
    }

    public Optional<List<AppendStreamSuccess>> getSuccesses() {
        return Optional.ofNullable(successes);
    }

    public Optional<List<AppendStreamFailure>> getFailures() {
        return Optional.ofNullable(failures);
    }
}
