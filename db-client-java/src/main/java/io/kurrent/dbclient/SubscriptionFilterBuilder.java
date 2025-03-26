package io.kurrent.dbclient;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility class for building a subscription filter.
 */
public class SubscriptionFilterBuilder {
    private int _checkpointIntervalUnsigned = 1;
    private Checkpointer _checkpointer = null;
    private FilterType _filterType = null;
    private Optional<Integer> _maxWindow;
    private RegularFilterExpression _regular = null;
    private ArrayList<PrefixFilterExpression> _prefixes = new ArrayList<>();

    private enum FilterType {
        STREAM,
        EVENT_TYPE;
    }

    SubscriptionFilterBuilder() {
    }

    /**
     * The maximum number of events that are filtered out before the page is returned.
     * Must be greater than 0, if supplied.
     */
    public SubscriptionFilterBuilder withMaxWindow(int maxWindow) {
        _maxWindow = Optional.of(maxWindow);
        return this;
    }

    /**
     * A regex to filter events based on their stream name.
     */
    public SubscriptionFilterBuilder withStreamNameRegularExpression(@NotNull String pattern) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.STREAM;
        _regular = new RegularFilterExpression(Pattern.compile(pattern));

        return this;
    }

    /**
     * A string prefix to filter events based on their stream name.
     */
    public SubscriptionFilterBuilder addStreamNamePrefix(@NotNull String prefix) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.STREAM;
        _prefixes.add(new PrefixFilterExpression(prefix));

        return this;
    }

    /**
     * A regex to filter events based on their type.
     */
    public SubscriptionFilterBuilder withEventTypeRegularExpression(@NotNull String pattern) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.EVENT_TYPE;
        _regular = new RegularFilterExpression(Pattern.compile(pattern));

        return this;
    }

    /**
     * A string prefix to filter events based on their type.
     */
    public SubscriptionFilterBuilder addEventTypePrefix(@NotNull String prefix) {
        if (_filterType != null) {
            throw new IllegalStateException(String.format("Filter type is already set to %s", _filterType.name()));
        }

        _filterType = FilterType.EVENT_TYPE;
        _prefixes.add(new PrefixFilterExpression(prefix));

        return this;
    }

    /**
     * Calls a callback everytime a checkpoint is reached.
     * @param checkpointer a callback.
     * @param intervalMultiplierUnsigned defines how often this callback is called.
     */
    public SubscriptionFilterBuilder withCheckpointer(@NotNull Checkpointer checkpointer, int intervalMultiplierUnsigned) {
        this._checkpointIntervalUnsigned = intervalMultiplierUnsigned;
        this._checkpointer = checkpointer;

        return this;
    }

    /**
     * Calls a callback everytime a checkpoint is reached.
     * @param checkpointer a callback.
     */
    public SubscriptionFilterBuilder withCheckpointer(@NotNull Checkpointer checkpointer) {
        return this.withCheckpointer(checkpointer, 1);
    }

    /**
     * Returns a configured subscription filter.
     * @return
     */
    public SubscriptionFilter build() {
        if (_filterType == null) {
            throw new IllegalStateException("No filter type is specified");
        }

        switch (this._filterType) {
            case STREAM:
                StreamFilter s;
                if (_regular != null) {
                    s = new StreamFilter(this._maxWindow, _regular);
                } else if (!_prefixes.isEmpty()) {
                    PrefixFilterExpression[] prefixes = new PrefixFilterExpression[_prefixes.size()];
                    s = new StreamFilter(this._maxWindow, _prefixes.toArray(prefixes));
                } else {
                    throw new IllegalStateException("Neither prefix or regular expression stream filter is configured");
                }

                return new SubscriptionFilter(s, this._checkpointIntervalUnsigned, this._checkpointer);

            case EVENT_TYPE:
                EventTypeFilter et;
                if (_regular != null) {
                    et = new EventTypeFilter(this._maxWindow, _regular);
                } else if (!_prefixes.isEmpty()) {
                    PrefixFilterExpression[] prefixes = new PrefixFilterExpression[_prefixes.size()];
                    et = new EventTypeFilter(this._maxWindow, _prefixes.toArray(prefixes));
                } else {
                    throw new IllegalStateException("Neither prefix or regular expression event type filter is configured");
                }

                return new SubscriptionFilter(et, this._checkpointIntervalUnsigned, this._checkpointer);

            default:
                throw new IllegalStateException(String.format("Unhandled filter type variant: %s", this._filterType.name()));
        }
    }
}
