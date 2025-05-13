package io.kurrent.dbclient.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.kurrent.dbclient.SubscriptionFilter;

public class ConsumeFilterTests {

    @Test
    public void testNoneConsumeFilter() {
        ConsumeFilter none = ConsumeFilter.NONE;
        
        Assertions.assertEquals(ConsumeFilterScope.UNSPECIFIED, none.getScope());
        Assertions.assertEquals(ConsumeFilterType.UNSPECIFIED, none.getType());
        Assertions.assertEquals("", none.getExpression());
        Assertions.assertTrue(none.isEmptyFilter());
        Assertions.assertFalse(none.isLiteralFilter());
        Assertions.assertFalse(none.isRegexFilter());
        Assertions.assertFalse(none.isStreamFilter());
        Assertions.assertFalse(none.isRecordFilter());
        Assertions.assertFalse(none.isStreamNameFilter());
    }
    
    @Test
    public void testFromStream() {
        String streamName = "test-stream";
        ConsumeFilter filter = ConsumeFilter.fromStream(streamName);
        
        Assertions.assertEquals(ConsumeFilterScope.STREAM, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.LITERAL, filter.getType());
        Assertions.assertEquals(streamName, filter.getExpression());
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertTrue(filter.isLiteralFilter());
        Assertions.assertFalse(filter.isRegexFilter());
        Assertions.assertTrue(filter.isStreamFilter());
        Assertions.assertFalse(filter.isRecordFilter());
        Assertions.assertTrue(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch(streamName));
        Assertions.assertFalse(filter.isMatch("other-stream"));
    }
    
    @Test
    public void testFromStreamValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromStream(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromStream(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromStream(" "));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromStream("~stream"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromStream("a"));
    }
    
    @Test
    public void testFromPrefixes() {
        ConsumeFilter filter = ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, "test-", "other-");
        
        Assertions.assertEquals(ConsumeFilterScope.RECORD, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.REGEX, filter.getType());
        Assertions.assertTrue(filter.getExpression().contains("test-"));
        Assertions.assertTrue(filter.getExpression().contains("other-"));
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertFalse(filter.isLiteralFilter());
        Assertions.assertTrue(filter.isRegexFilter());
        Assertions.assertFalse(filter.isStreamFilter());
        Assertions.assertTrue(filter.isRecordFilter());
        Assertions.assertFalse(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch("test-event"));
        Assertions.assertTrue(filter.isMatch("other-event"));
        Assertions.assertFalse(filter.isMatch("some-event"));
    }
    
    @Test
    public void testFromPrefixesValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, (String[])null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, " "));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, "test-", null));
    }
    
    @Test
    public void testFromPrefixesString() {
        ConsumeFilter filter = ConsumeFilter.fromPrefixes(ConsumeFilterScope.STREAM, "test-,other-");
        
        Assertions.assertEquals(ConsumeFilterScope.STREAM, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.REGEX, filter.getType());
        Assertions.assertTrue(filter.getExpression().contains("test-"));
        Assertions.assertTrue(filter.getExpression().contains("other-"));
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertFalse(filter.isLiteralFilter());
        Assertions.assertTrue(filter.isRegexFilter());
        Assertions.assertTrue(filter.isStreamFilter());
        Assertions.assertFalse(filter.isRecordFilter());
        Assertions.assertFalse(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch("test-stream"));
        Assertions.assertTrue(filter.isMatch("other-stream"));
        Assertions.assertFalse(filter.isMatch("some-stream"));
    }
    
    @Test
    public void testFromPrefixesStringValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, (String)null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromPrefixes(ConsumeFilterScope.RECORD, " "));
    }
    
    @Test
    public void testFromRegex() {
        ConsumeFilter filter = ConsumeFilter.fromRegex(ConsumeFilterScope.RECORD, "test-.*");
        
        Assertions.assertEquals(ConsumeFilterScope.RECORD, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.REGEX, filter.getType());
        Assertions.assertEquals("test-.*", filter.getExpression());
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertFalse(filter.isLiteralFilter());
        Assertions.assertTrue(filter.isRegexFilter());
        Assertions.assertFalse(filter.isStreamFilter());
        Assertions.assertTrue(filter.isRecordFilter());
        Assertions.assertFalse(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch("test-event"));
        Assertions.assertFalse(filter.isMatch("other-event"));
    }
    
    @Test
    public void testFromRegexWithTilde() {
        ConsumeFilter filter = ConsumeFilter.fromRegex(ConsumeFilterScope.STREAM, "~test-.*");
        
        Assertions.assertEquals(ConsumeFilterScope.STREAM, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.REGEX, filter.getType());
        Assertions.assertEquals("test-.*", filter.getExpression());
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertFalse(filter.isLiteralFilter());
        Assertions.assertTrue(filter.isRegexFilter());
        Assertions.assertTrue(filter.isStreamFilter());
        Assertions.assertFalse(filter.isRecordFilter());
        Assertions.assertFalse(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch("test-stream"));
        Assertions.assertFalse(filter.isMatch("other-stream"));
    }
    
    @Test
    public void testFromRegexValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.fromRegex(ConsumeFilterScope.RECORD, "["));
    }
    
    @Test
    public void testCreate() {
        ConsumeFilter filter = ConsumeFilter.create(ConsumeFilterScope.RECORD, "test-event");
        
        Assertions.assertEquals(ConsumeFilterScope.RECORD, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.LITERAL, filter.getType());
        Assertions.assertEquals("test-event", filter.getExpression());
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertTrue(filter.isLiteralFilter());
        Assertions.assertFalse(filter.isRegexFilter());
        Assertions.assertFalse(filter.isStreamFilter());
        Assertions.assertTrue(filter.isRecordFilter());
        Assertions.assertFalse(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch("test-event"));
        Assertions.assertFalse(filter.isMatch("other-event"));
    }
    
    @Test
    public void testCreateWithRegex() {
        ConsumeFilter filter = ConsumeFilter.create(ConsumeFilterScope.STREAM, "~test-.*");
        
        Assertions.assertEquals(ConsumeFilterScope.STREAM, filter.getScope());
        Assertions.assertEquals(ConsumeFilterType.REGEX, filter.getType());
        Assertions.assertEquals("test-.*", filter.getExpression());
        Assertions.assertFalse(filter.isEmptyFilter());
        Assertions.assertFalse(filter.isLiteralFilter());
        Assertions.assertTrue(filter.isRegexFilter());
        Assertions.assertTrue(filter.isStreamFilter());
        Assertions.assertFalse(filter.isRecordFilter());
        Assertions.assertFalse(filter.isStreamNameFilter());
        Assertions.assertTrue(filter.isMatch("test-stream"));
        Assertions.assertFalse(filter.isMatch("other-stream"));
    }
    
    @Test
    public void testCreateValidation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.create(ConsumeFilterScope.RECORD, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.create(ConsumeFilterScope.RECORD, ""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ConsumeFilter.create(ConsumeFilterScope.RECORD, " "));
    }
    
    @Test
    public void testToFilterOptions() {
        ConsumeFilter streamFilter = ConsumeFilter.fromStream("test-stream");
        SubscriptionFilter subscriptionFilter = streamFilter.toSubscriptionFilter(1000);
        
        Assertions.assertNotNull(subscriptionFilter);
        
        ConsumeFilter recordFilter = ConsumeFilter.create(ConsumeFilterScope.RECORD, "test-event");
        subscriptionFilter = recordFilter.toSubscriptionFilter(1000);
        
        Assertions.assertNotNull(subscriptionFilter);
        
        ConsumeFilter emptyFilter = ConsumeFilter.NONE;
        subscriptionFilter = emptyFilter.toSubscriptionFilter(1000);
        
        Assertions.assertNull(subscriptionFilter);
    }
    
    @Test
    public void testEqualsAndHashCode() {
        ConsumeFilter filter1 = ConsumeFilter.fromStream("test-stream");
        ConsumeFilter filter2 = ConsumeFilter.fromStream("test-stream");
        ConsumeFilter filter3 = ConsumeFilter.fromStream("other-stream");
        
        Assertions.assertEquals(filter1, filter2);
        Assertions.assertEquals(filter1.hashCode(), filter2.hashCode());
        
        Assertions.assertNotEquals(filter1, filter3);
        Assertions.assertNotEquals(filter1.hashCode(), filter3.hashCode());
    }
    
    @Test
    public void testToString() {
        ConsumeFilter filter = ConsumeFilter.fromStream("test-stream");
        
        Assertions.assertEquals("[STREAM|LITERAL] test-stream", filter.toString());
    }
}