package org.logparser.stats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.LogEntry;

/**
 * Tests for {@link HourStats}.
 * 
 * @author jorge.decastro
 * 
 */
public class HourStatsTest {
	private LogEntry entryXAtTimeA;
	private LogEntry entryYAtTimeA;
	private LogEntry entryXAtTimeB;
	private HourStats<LogEntry> underTest;

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
		underTest = new HourStats<LogEntry>();
	}

	@After
	public void tearDown() {
		entryXAtTimeA = null;
		entryYAtTimeA = null;
		entryXAtTimeB = null;
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testAddNullLogEntryResultsInEmptyHourStats() {
		underTest.add(null);
		assertThat(underTest.getHourStats().isEmpty(), is(true));
	}

	@Test
	public void testAddNotNullLogEntryPopulatesHourStats() {
		underTest.add(entryXAtTimeA);
		assertThat(underTest.getHourStats().isEmpty(), is(false));
	}

	@Test
	public void testTimeStatsOfSingleAddedLogEntry() {
		underTest.add(entryXAtTimeA);
		assertThat(underTest.getHourStats().size(), is(1));
		assertThat(underTest.getHourStats().keySet(), hasItem(entryXAtTimeA.getAction()));
		Map<Integer, TimeStats<LogEntry>> timeStats = underTest.getTimeStats(entryXAtTimeA.getAction());
		assertThat(timeStats.size(), is(1));
		assertThat(timeStats.keySet(), hasItem(27));
	}

	@Test
	public void testTimeStatsOfMultipleLogEntriesWithSameKey() {
		underTest.add(entryXAtTimeA);
		underTest.add(entryXAtTimeB);
		assertThat(underTest.getHourStats().size(), is(1));
		assertThat(underTest.getHourStats().keySet(), hasItem(entryXAtTimeA.getAction()));
		Map<Integer, TimeStats<LogEntry>> timeStats = underTest.getTimeStats(entryXAtTimeA.getAction());
		assertThat(timeStats.size(), is(2));
		assertThat(timeStats.keySet(), hasItem(27));
		assertThat(timeStats.keySet(), hasItem(31));
	}

	@Test
	public void testTimeStatsOfMultipleLogEntriesWithDifferentKeys() {
		underTest.add(entryXAtTimeA);
		underTest.add(entryYAtTimeA);
		assertThat(underTest.getHourStats().size(), is(2));
		assertThat(underTest.getHourStats().keySet(), hasItem(entryXAtTimeA.getAction()));
		assertThat(underTest.getHourStats().keySet(), hasItem(entryYAtTimeA.getAction()));
		Map<Integer, TimeStats<LogEntry>> timeStats = underTest.getTimeStats(entryXAtTimeA.getAction());
		assertThat(timeStats.size(), is(1));
		assertThat(timeStats.keySet(), hasItem(27));
		timeStats = underTest.getTimeStats(entryYAtTimeA.getAction());
		assertThat(timeStats.size(), is(1));
		assertThat(timeStats.keySet(), hasItem(27));
	}
}
