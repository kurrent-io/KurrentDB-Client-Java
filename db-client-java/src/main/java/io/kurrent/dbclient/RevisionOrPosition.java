package io.kurrent.dbclient;

import java.util.Optional;

/**
 * Holds a stream revision number or transaction log position.
 */
public class RevisionOrPosition {
    private Optional<Long> revision = Optional.empty();
    private Optional<Position> position = Optional.empty();

    RevisionOrPosition() {}

    /**
     * Returns a stream revision number.
     */
    public Optional<Long> getRevision() {
        return revision;
    }

    /**
     * Returns a transaction log position.
     */
    public Optional<Position> getPosition() {
        return position;
    }

    void setRevision(long revision) {
        this.revision = Optional.of(revision);
    }

    void setPosition(Position position) {
        this.position = Optional.of(position);
    }

    /**
     * Checks if the object holds a stream revision number.
     */
    public boolean isRevisionPresent() {
        return revision.isPresent();
    }

    /**
     * Checks if the object holds a transaction log position.
     */
    public boolean isPositionPresent(){
        return !isRevisionPresent();
    }
}
