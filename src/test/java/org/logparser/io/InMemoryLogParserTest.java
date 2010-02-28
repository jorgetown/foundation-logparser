package org.logparser.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.IMessageFilter;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link InMemoryLogParser}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class InMemoryLogParserTest {

	@Mock
	private IMessageFilter<TestMessage> filter;

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("unchecked")
	public void testNullFilter() {
		IMessageFilter<TestMessage> filter = null;
		new InMemoryLogParser<TestMessage>(filter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<IMessageFilter<TestMessage>> filters = null;
		new InMemoryLogParser<TestMessage>(filters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(null);
		new InMemoryLogParser<TestMessage>(filters);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNotNullFilter() {
		InMemoryLogParser<TestMessage> parser = new InMemoryLogParser<TestMessage>(filter);
		assertThat(parser.getTotalEntries(), is(equalTo(0)));
		assertThat(parser.getParsedEntries().size(), is(equalTo(0)));
		assertThat(parser.getEarliestEntry(), is(nullValue()));
		assertThat(parser.getLatestEntry(), is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOneFilteredEntry() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(filter);
		InMemoryLogParser<TestMessage> parser = new InMemoryLogParser<TestMessage>(filters);
		TestMessage tm = new TestMessage();
		List<String> entries = new ArrayList<String>();
		entries.add("An Entry");
		when(filter.parse(anyString())).thenReturn(tm);
		List<TestMessage> filtered = parser.parse(entries, filters);
		assertThat(entries.size(), is(equalTo(1)));
		assertThat(filtered.size(), is(equalTo(1)));
		assertThat(parser.getEarliestEntry(), is(equalTo(tm)));
		assertThat(parser.getLatestEntry(), is(equalTo(tm)));
		verify(filter, times(1)).parse(anyString());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testManyFilteredEntries() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(filter);
		InMemoryLogParser<TestMessage> parser = new InMemoryLogParser<TestMessage>(filters);
		TestMessage tm1 = new TestMessage();
		TestMessage tm2 = new TestMessage();
		List<String> entries = new ArrayList<String>();
		entries.add("Entry 1");
		entries.add("Entry 2");
		when(filter.parse("Entry 1")).thenReturn(tm1);
		when(filter.parse("Entry 2")).thenReturn(tm2);
		List<TestMessage> filtered = parser.parse(entries, filters);
		assertThat(entries.size(), is(equalTo(2)));
		assertThat(filtered.size(), is(equalTo(2)));
		assertThat(parser.getEarliestEntry(), is(equalTo(tm1)));
		assertThat(parser.getLatestEntry(), is(equalTo(tm2)));
		verify(filter, times(2)).parse(anyString());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFilteredAndUnfilteredEntriesMixed() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(filter);
		InMemoryLogParser<TestMessage> parser = new InMemoryLogParser<TestMessage>(filters);
		TestMessage tm1 = new TestMessage();
		List<String> entries = new ArrayList<String>();
		entries.add("Entry 1");
		entries.add("Entry 2");
		when(filter.parse("Entry 1")).thenReturn(null);
		when(filter.parse("Entry 2")).thenReturn(tm1);
		List<TestMessage> filtered = parser.parse(entries, filters);
		assertThat(entries.size(), is(equalTo(2)));
		assertThat(filtered.size(), is(equalTo(1)));
		assertThat(parser.getEarliestEntry(), is(equalTo(tm1)));
		assertThat(parser.getLatestEntry(), is(equalTo(tm1)));
		verify(filter, times(2)).parse(anyString());
	}

	private static class TestMessage {

	}
}
