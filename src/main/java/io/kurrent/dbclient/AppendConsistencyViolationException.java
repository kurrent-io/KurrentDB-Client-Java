package io.kurrent.dbclient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when one or more consistency checks fail during an AppendRecords operation.
 * The entire transaction is aborted and no records are written.
 */
public class AppendConsistencyViolationException extends RuntimeException {
    private final List<ConsistencyViolation> violations;

    /**
     * Creates a new AppendConsistencyViolationException.
     *
     * @param violations the consistency violations that caused the transaction to be aborted.
     */
    public AppendConsistencyViolationException(List<ConsistencyViolation> violations) {
        super(formatMessage(violations));
        this.violations = violations;
    }

    /**
     * Returns the consistency violations that caused the transaction to be aborted.
     */
    public List<ConsistencyViolation> getViolations() {
        return violations;
    }

    private static String formatMessage(List<ConsistencyViolation> violations) {
        String details = violations.stream()
                .map(v -> String.format("[Check %d: Stream '%s' expected state %s, actual state %s]",
                        v.getCheckIndex(), v.getStream(), v.getExpectedState(), v.getActualState()))
                .collect(Collectors.joining(", "));
        return "Append failed due to consistency violation(s): " + details;
    }
}
