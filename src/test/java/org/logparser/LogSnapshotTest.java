package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link LogSnapshot}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class LogSnapshotTest {
	private LogSnapshot<TestMessage> underTest;
	@Mock
	private Map<String, Integer> mockSummary;
	@Mock
	private Map<Integer, Integer> mockTimeBreakdown;
	

	@Test(expected = NullPointerException.class)
	public void testNullFilteredEntriesArgument() {
		new LogSnapshot<TestMessage>(null, 0, mockSummary, mockTimeBreakdown);
	}

	@Test
	public void testNotNullFilteredEntriesArguments() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0, mockSummary, mockTimeBreakdown);
		assertThat(underTest, is(notNullValue()));
	}

	@Test
	public void testTotalEntriesArgument() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 1000, mockSummary, mockTimeBreakdown);
		assertThat(underTest.getTotalEntries(), is(equalTo(1000)));
	}

	@Test
	public void testEmptyFilteredEntries() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0, mockSummary, mockTimeBreakdown);
		assertThat(underTest.getFilteredEntries().size(), is(equalTo(0)));
	}

	@Test
	public void testNotEmptyFilteredEntries() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		TestMessage tm = new TestMessage(1000);
		filteredEntries.add(tm);
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0, mockSummary, mockTimeBreakdown);
		assertThat(underTest.getFilteredEntries().size(), is(equalTo(1)));
		assertThat(underTest.getFilteredEntries(), hasItem(tm));
	}
	
	@Test
	public void testJsonString() throws JsonGenerationException, JsonMappingException, IOException {
		TestMessage tm = new TestMessage("/test.do", 12345);
		List<TestMessage> filtered = Arrays.asList(tm);
		Map<String, Integer> summary = new TreeMap<String, Integer>();
		Map<Integer, Integer> timeBreakdown = new TreeMap<Integer, Integer>();
		summary.put("/test.do", 123);
		summary.put("/metadata.do", 12);
		timeBreakdown.put(17, 30);
		underTest = new LogSnapshot<TestMessage>(filtered, 30, summary, timeBreakdown);
		String expected = "{\"totalEntries\":30,\"summary\":{\"/metadata.do\":12,\"/test.do\":123},\"timeBreakdown\":{\"17\":30},\"filteredEntries\":[{\"timestamp\":12345,\"action\":\"/test.do\",\"duration\":12345.0}]}";
		assertThat(underTest.toJsonString(), is(equalTo(expected)));
	}
}
