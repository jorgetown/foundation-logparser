package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.config.Config;
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
	private LogEntry entryA;
	private LogEntry entryB;
	private Calendar calendar;
	private TimeZone timeZone;

	private LogSnapshot<LogEntry> underTest;
	@Mock
	Config mockConfig;

	@Before
	public void setUp() {
		timeZone = TimeZone.getTimeZone("GMT+0000");
		calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(1280589260565L);
		entryA = new LogEntry(calendar.getTimeInMillis(), "/action.a", 2073D);
		entryB = new LogEntry(calendar.getTimeInMillis(), "/action.b", 2073D);
		underTest = new LogSnapshot<LogEntry>(mockConfig);
	}

	@After
	public void tearDown() {
		entryA = null;
		entryB = null;
		timeZone = null;
		calendar = null;
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testNullConfigArgumentGiven() {
		new LogSnapshot<LogEntry>(null);
	}

	@Test
	public void testNotNullConfigArgumentGiven() {
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getFilteredEntries().size(), is(0));
		assertThat(underTest.getTotalEntries(), is(0));
		assertThat(underTest.getHourStats().getHourStats().size(), is(0));
	}

	@Test
	public void testFilteredEntriesWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getFilteredEntries().size(), is(0));
	}

	@Test
	public void testFilteredEntriesWithSingleConsumedEntry() {
		when(mockConfig.isFilteredEntriesStored()).thenReturn(true);
		
		underTest = new LogSnapshot<LogEntry>(mockConfig);
		underTest.consume(entryA);

		verify(mockConfig, atLeastOnce()).isFilteredEntriesStored();
		assertThat(underTest.getFilteredEntries().size(), is(1));
		assertThat(underTest.getFilteredEntries(), hasItem(entryA));
	}
	
	@Test
	public void testFilteredEntriesWithSingleConsumedEntryAndStoringDisabled() {
		when(mockConfig.isFilteredEntriesStored()).thenReturn(false);
		
		underTest = new LogSnapshot<LogEntry>(mockConfig);
		underTest.consume(entryA);

		verify(mockConfig, atLeastOnce()).isFilteredEntriesStored();
		assertThat(underTest.getFilteredEntries().size(), is(0));
	}

	@Test
	public void testFilteredEntriesWithMultipleConsumedEntries() {
		when(mockConfig.isFilteredEntriesStored()).thenReturn(true);
		
		underTest = new LogSnapshot<LogEntry>(mockConfig);
		underTest.consume(entryA);
		underTest.consume(entryB);

		verify(mockConfig, atLeastOnce()).isFilteredEntriesStored();
		assertThat(underTest.getFilteredEntries().size(), is(2));
		assertThat(underTest.getFilteredEntries(), hasItem(entryA));
		assertThat(underTest.getFilteredEntries(), hasItem(entryB));
	}

	@Test
	public void testTotalEntriesWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getTotalEntries(), is(1));
	}

	@Test
	public void testTotalEntriesWithSingleConsumedEntry() {
		underTest.consume(entryA);

		assertThat(underTest.getTotalEntries(), is(1));
	}

	@Test
	public void testTotalEntriesWithMultipleConsumedEntries() {
		underTest.consume(entryA);
		underTest.consume(entryB);

		assertThat(underTest.getTotalEntries(), is(2));
	}

	@Test
	public void testStatsWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getHourStats().getHourStats().size(), is(0));
	}

	@Test
	public void testStatsWithSingleConsumedEntry() {
		underTest.consume(entryA);

		assertThat(underTest.getHourStats().getHourStats().size(), is(1));
		assertThat(underTest.getHourStats().getHourStats().size(), is(1));
		assertThat(underTest.getHourStats().getHourStats().keySet(), hasItem("/action.a"));
	}

	@Test
	public void testStatsWithMultipleConsumedEntries() {
		underTest.consume(entryA);
		underTest.consume(entryB);

		assertThat(underTest.getHourStats().getHourStats().size(), is(2));
		assertThat(underTest.getHourStats().getHourStats().keySet(), hasItem("/action.a"));
		assertThat(underTest.getHourStats().getHourStats().keySet(), hasItem("/action.b"));
	}
}