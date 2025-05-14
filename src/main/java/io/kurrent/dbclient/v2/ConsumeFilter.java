package io.kurrent.dbclient.v2;

import io.kurrent.dbclient.SubscriptionFilter;
import io.kurrent.dbclient.SubscriptionFilterBuilder;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a filter for consuming events.
 */
public class ConsumeFilter {
    /**
     * Represents an empty filter.
     */
    public static final ConsumeFilter NONE = new ConsumeFilter();

    private final ConsumeFilterScope scope;
    private final ConsumeFilterType type;
    private final String expression;
    private final Pattern regex;

    /**
     * Creates a new empty consume filter.
     */
    public ConsumeFilter() {
        this.scope = ConsumeFilterScope.UNSPECIFIED;
        this.type = ConsumeFilterType.UNSPECIFIED;
        this.expression = "";
        this.regex = Pattern.compile("");
    }

    /**
     * Creates a new consume filter with the specified parameters.
     *
     * @param scope The scope of the filter.
     * @param type The type of the filter.
     * @param expression The filter expression.
     * @param regex The compiled regex pattern.
     */
    private ConsumeFilter(ConsumeFilterScope scope, ConsumeFilterType type, String expression, Pattern regex) {
        this.scope = scope;
        this.type = type;
        this.expression = expression;
        this.regex = regex;
    }

    /**
     * Gets the scope of the filter.
     *
     * @return The filter scope.
     */
    public ConsumeFilterScope getScope() {
        return scope;
    }

    /**
     * Gets the type of the filter.
     *
     * @return The filter type.
     */
    public ConsumeFilterType getType() {
        return type;
    }

    /**
     * Gets the filter expression.
     *
     * @return The filter expression.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Checks if this is a literal filter.
     *
     * @return True if this is a literal filter, false otherwise.
     */
    public boolean isLiteralFilter() {
        return type == ConsumeFilterType.LITERAL;
    }

    /**
     * Checks if this is a regex filter.
     *
     * @return True if this is a regex filter, false otherwise.
     */
    public boolean isRegexFilter() {
        return type == ConsumeFilterType.REGEX;
    }

    /**
     * Checks if this is a stream filter.
     *
     * @return True if this is a stream filter, false otherwise.
     */
    public boolean isStreamFilter() {
        return scope == ConsumeFilterScope.STREAM;
    }

    /**
     * Checks if this is a record filter.
     *
     * @return True if this is a record filter, false otherwise.
     */
    public boolean isRecordFilter() {
        return scope == ConsumeFilterScope.RECORD;
    }

    /**
     * Checks if this is a stream name filter.
     *
     * @return True if this is a stream name filter, false otherwise.
     */
    public boolean isStreamNameFilter() {
        return type == ConsumeFilterType.LITERAL && scope == ConsumeFilterScope.STREAM;
    }

    /**
     * Checks if this is an empty filter.
     *
     * @return True if this is an empty filter, false otherwise.
     */
    public boolean isEmptyFilter() {
        return type == ConsumeFilterType.UNSPECIFIED && scope == ConsumeFilterScope.UNSPECIFIED;
    }

    /**
     * Checks if the input matches this filter.
     *
     * @param input The input to check.
     * @return True if the input matches this filter, false otherwise.
     */
    public boolean isMatch(CharSequence input) {
        return regex.matcher(input).matches();
    }

    /**
     * Creates a stream filter from a stream name.
     *
     * @param stream The stream name.
     * @return A new consume filter for the specified stream.
     * @throws IllegalArgumentException If the stream name is invalid.
     */
    public static ConsumeFilter fromStream(String stream) {
        if (stream == null || stream.trim().isEmpty())
            throw new IllegalArgumentException("Stream name cannot be null or whitespace.");

        if (stream.startsWith("~"))
            throw new IllegalArgumentException("Stream name cannot start with '~'.");

        if (stream.length() < 2)
            throw new IllegalArgumentException("Stream name must be at least 2 characters long.");

        return new ConsumeFilter(
            ConsumeFilterScope.STREAM,
            ConsumeFilterType.LITERAL,
            stream,
            Pattern.compile(Pattern.quote(stream))
        );
    }

    /**
     * Creates a filter from prefixes.
     *
     * @param scope The scope of the filter.
     * @param prefixes The prefixes to filter by.
     * @return A new consume filter for the specified prefixes.
     * @throws IllegalArgumentException If the prefixes are invalid.
     */
    public static ConsumeFilter fromPrefixes(ConsumeFilterScope scope, String... prefixes) {
        if (prefixes == null || prefixes.length == 0)
            throw new IllegalArgumentException("Prefixes cannot be empty.");

        StringBuilder patternBuilder = new StringBuilder("^(");
        boolean first = true;

        for (String prefix : prefixes) {
            if (prefix == null || prefix.trim().isEmpty())
                throw new IllegalArgumentException("Prefix cannot be empty.");

            if (!first)
                patternBuilder.append("|");

            patternBuilder.append(Pattern.quote(prefix));
            first = false;
        }
        patternBuilder.append(").*");

        String pattern = patternBuilder.toString();
        return new ConsumeFilter(
            scope,
            ConsumeFilterType.REGEX,
            pattern,
            Pattern.compile(pattern)
        );
    }

    /**
     * Creates a filter from a comma-separated list of prefixes.
     *
     * @param scope The scope of the filter.
     * @param expression The comma-separated list of prefixes.
     * @return A new consume filter for the specified prefixes.
     * @throws IllegalArgumentException If the expression is invalid.
     */
    public static ConsumeFilter fromPrefixes(ConsumeFilterScope scope, String expression) {
        if (expression == null || expression.trim().isEmpty())
            throw new IllegalArgumentException("Prefix expression cannot be empty.");

        return fromPrefixes(scope, expression.split(","));
    }

    /**
     * Creates a filter from a regex pattern.
     *
     * @param scope The scope of the filter.
     * @param pattern The regex pattern.
     * @return A new consume filter for the specified regex pattern.
     * @throws IllegalArgumentException If the pattern is invalid.
     */
    public static ConsumeFilter fromRegex(ConsumeFilterScope scope, String pattern) {
        String expression = pattern.startsWith("~") ? pattern.substring(1) : pattern;

        try {
            return new ConsumeFilter(
                scope,
                ConsumeFilterType.REGEX,
                expression,
                Pattern.compile(expression)
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid regex pattern: " + pattern, ex);
        }
    }

    /**
     * Creates a filter from an expression.
     *
     * @param scope The scope of the filter.
     * @param expression The filter expression.
     * @return A new consume filter for the specified expression.
     * @throws IllegalArgumentException If the expression is invalid.
     */
    public static ConsumeFilter create(ConsumeFilterScope scope, String expression) {
        if (expression == null || expression.trim().isEmpty())
            throw new IllegalArgumentException("Expression cannot be null or empty.");

        if (expression.startsWith("~")) {
            return fromRegex(scope, expression);
        } else {
            return new ConsumeFilter(
                scope,
                ConsumeFilterType.LITERAL,
                expression,
                Pattern.compile(Pattern.quote(expression))
            );
        }
    }

    /**
     * Converts a ConsumeFilter to a SubscriptionFilter.
     *
     * @param checkpointInterval The checkpoint interval to use.
     * @return The converted subscription filter, or null if the filter is empty.
     * @throws IllegalArgumentException If the filter is invalid.
     */
    public SubscriptionFilter toSubscriptionFilter(int checkpointInterval) {
        if (isEmptyFilter())
            return null;

        SubscriptionFilterBuilder builder = SubscriptionFilter.newBuilder();

        // Set the checkpoint interval
        builder.withMaxWindow(checkpointInterval);

        // Configure the filter based on its scope and type
        if (isStreamFilter()) {
            if (isRegexFilter()) {
                builder.withStreamNameRegularExpression(getExpression());
            } else if (isLiteralFilter()) {
                builder.addStreamNamePrefix(getExpression());
            }
        } else if (isRecordFilter()) {
            if (isRegexFilter()) {
                builder.withEventTypeRegularExpression(getExpression());
            } else if (isLiteralFilter()) {
                builder.addEventTypePrefix(getExpression());
            }
        } else {
            throw new IllegalArgumentException("Invalid consume filter.");
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return "[" + scope + "|" + type + "] " + expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumeFilter that = (ConsumeFilter) o;
        return scope == that.scope &&
               type == that.type &&
               Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, type, expression);
    }
}