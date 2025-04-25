package io.kurrent.dbclient;

import java.util.Optional;

interface EventFilter {
    PrefixFilterExpression[] getPrefixFilterExpressions();

    RegularFilterExpression getRegularFilterExpression();

    Optional<Integer> getMaxSearchWindow();
}
