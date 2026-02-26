package io.kurrent.dbclient;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("io.kurrent.dbclient.misc")
@SelectClasses(SubscriptionStreamConsumerTests.class)
public class MiscTests {}
