package io.kurrent.dbclient.v2;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a collection of failed appends to streams.
 */
public class AppendStreamFailures extends ArrayList<AppendStreamFailure> {
    
    /**
     * Initializes a new instance of the AppendStreamFailures class.
     */
    public AppendStreamFailures() {
        super();
    }
    
    /**
     * Initializes a new instance of the AppendStreamFailures class with the specified collection of AppendStreamFailure objects.
     *
     * @param input The collection of AppendStreamFailure objects.
     */
    public AppendStreamFailures(Collection<AppendStreamFailure> input) {
        super(input);
    }
}