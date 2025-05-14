package io.kurrent.dbclient.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a collection of successful appends to streams.
 */
public class AppendStreamSuccesses extends ArrayList<AppendStreamSuccess> {
    
    /**
     * Initializes a new instance of the AppendStreamSuccesses class.
     */
    public AppendStreamSuccesses() {
        super();
    }
    
    /**
     * Initializes a new instance of the AppendStreamSuccesses class with the specified collection of AppendStreamSuccess objects.
     *
     * @param input The collection of AppendStreamSuccess objects.
     */
    public AppendStreamSuccesses(Collection<AppendStreamSuccess> input) {
        super(input);
    }
}