package org.logparser.stats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.LogEntry;

/**
 * Tests for {@link DayStats}.
 * 
 * @author jorge.decastro
 * 
 */
public class DayStatsTest {
	private LogEntry entryXAtTimeA;
	private LogEntry entryYAtTimeA;
	private LogEntry entryXAtTimeB;
	private DayStats<LogEntry> underTest;

	@Before
	public void setUp() {
		TimeZone timeZone = TimeZone.getTimeZone("GMT+0000");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(1280189260565L); // Tue Jul 27 01:07:40 BST 2010
		Date timeA = calendar.getTime();
		calendar.setTimeInMillis(1280589270565L); // Sat Jul 31 16:14:20 BST 2010
		Date timeB = calendar.getTime();
		entryXAtTimeA = new LogEntry(timeA.getTime(), "/entry.x", 1000D);
		entryYAtTimeA = new LogEntry(timeA.getTime(), "/entry.y", 2000D);
		entryXAtTimeB = new LogEntry(timeB.getTime(), "/entry.x", 3000D);
		underTest = new DayStats<LogEntry>();
	}

	@After
	public void tearDown() {
		entryXAtTimeA = null;
		entryYAtTimeA = null;
		entryXAtTimeB = null;
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testAddNullLogEntryResultsInEmptyDayStats() {
		underTest.add(null);
		assertThat(underTest.getDayStats().isEmpty(), is(true));
	}

	@Test
	public void testAddNotNullLogEntryPopulatesDayStats() {
		underTest.add(entryXAtTimeA);
		assertThat(underTest.getDayStats().isEmpty(), is(false));
	}

	@Test
	public void testTimeStatsOfSingleAddedLogEntry() {
		underTest.add(entryXAtTimeA);
		assertThat(underTest.getDayStats().size(), is(1));
		assertThat(underTest.getDayStats().keySet(), hasItem(entryXAtTimeA.getAction()));
		TimeStats<LogEntry> timeStats = underTest.getTimeStats(entryXAtTimeA.getAction());
		assertThat(timeStats.getTimeStats().size(), is(1));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(27));
	}

	@Test
	public void testTimeStatsOfMultipleLogEntriesWithSameKey() {
		underTest.add(entryXAtTimeA);
		underTest.add(entryXAtTimeB);
		assertThat(underTest.getDayStats().size(), is(1));
		assertThat(underTest.getDayStats().keySet(), hasItem(entryXAtTimeA.getAction()));
		TimeStats<LogEntry> timeStats = underTest.getTimeStats(entryXAtTimeA.getAction());
		assertThat(timeStats.getTimeStats().size(), is(2));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(27));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(31));
	}

	@Test
	public void testTimeStatsOfMultipleLogEntriesWithDifferentKeys() {
		underTest.add(entryXAtTimeA);
		underTest.add(entryYAtTimeA);
		assertThat(underTest.getDayStats().size(), is(2));
		assertThat(underTest.getDayStats().keySet(), hasItem(entryXAtTimeA.getAction()));
		assertThat(underTest.getDayStats().keySet(), hasItem(entryYAtTimeA.getAction()));
		TimeStats<LogEntry> timeStats = underTest.getTimeStats(entryXAtTimeA.getAction());
		assertThat(timeStats.getTimeStats().size(), is(1));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(27));
		timeStats = underTest.getTimeStats(entryYAtTimeA.getAction());
		assertThat(timeStats.getTimeStats().size(), is(1));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(27));
	}
}
