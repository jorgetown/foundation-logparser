package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.Config.GroupBy;
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
	private static final String SAMPLE_ENTRY_A = "10.118.101.132 - - [31/Jul/2010:16:14:20 +0000] \"POST /action.a HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_B = "10.118.101.132 - - [31/Jul/2010:16:14:20 +0000] \"POST /action.b HTTP/1.1\" 200 1779 2073";
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
		entryA = new LogEntry(SAMPLE_ENTRY_A, calendar.getTime(), "/action.a", "2073");
		entryB = new LogEntry(SAMPLE_ENTRY_B, calendar.getTime(), "/action.b", "2073");
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
		assertThat(underTest.getSummary().size(), is(0));
		assertThat(underTest.getTimeBreakdown().size(), is(0));
		assertThat(underTest.getStats().size(), is(0));
	}

	@Test
	public void testFilteredEntriesWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getFilteredEntries().size(), is(0));
	}

	@Test
	public void testFilteredEntriesWithSingleConsumedEntry() {
		underTest.consume(entryA);

		assertThat(underTest.getFilteredEntries().size(), is(1));
		assertThat(underTest.getFilteredEntries(), hasItem(entryA));
	}

	@Test
	public void testFilteredEntriesWithMultipleConsumedEntries() {
		underTest.consume(entryA);
		underTest.consume(entryB);

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
	public void testSummaryWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getSummary().size(), is(0));
	}

	@Test
	public void testSummaryWithSingleConsumedEntry() {
		underTest.consume(entryA);

		assertThat(underTest.getSummary().size(), is(1));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.a"));
	}

	@Test
	public void testSummaryWithMultipleConsumedEntries() {
		underTest.consume(entryA);
		underTest.consume(entryB);

		assertThat(underTest.getSummary().size(), is(2));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.a"));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.b"));
	}

	@Test
	public void testTimeBreakdownWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getTimeBreakdown().size(), is(0));
	}

	@Test
	public void testTimeBreakdownWithSingleConsumedEntry() {
		Config config = new Config();
		config.setGroupBy(GroupBy.MINUTE);

		underTest = new LogSnapshot<LogEntry>(config);
		underTest.consume(entryA);

		assertThat(underTest.getGroupBy(), is(Calendar.MINUTE));
		assertThat(underTest.getTimeBreakdown().size(), is(1));
		assertThat(underTest.getTimeBreakdown().keySet(), hasItem(14)); // minutes on timestamp
	}

	@Test
	public void testTimeBreakdownWithMultipleConsumedEntries() {
		Config config = new Config();
		config.setGroupBy(GroupBy.MINUTE);

		underTest = new LogSnapshot<LogEntry>(config);
		underTest.consume(entryA);
		underTest.consume(entryB);

		assertThat(underTest.getTimeBreakdown().size(), is(1));
		assertThat(underTest.getTimeBreakdown().keySet(), hasItem(14)); // minutes on timestamp
		assertThat(underTest.getTimeBreakdown().values(), hasItem(2));
	}

	@Test
	public void testStatsWithNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getStats().size(), is(0));
	}

	@Test
	public void testStatsWithSingleConsumedEntry() {
		Config config = new Config();
		config.setGroupBy(GroupBy.HOUR);

		underTest = new LogSnapshot<LogEntry>(config);
		underTest.consume(entryA);

		assertThat(underTest.getGroupBy(), is(Calendar.HOUR_OF_DAY));
		assertThat(underTest.getStats().size(), is(1));
	}

	@Test
	public void testStatsWithMultipleConsumedEntries() {
		Config config = new Config();
		config.setGroupBy(GroupBy.HOUR);

		underTest = new LogSnapshot<LogEntry>(config);
		underTest.consume(entryA);
		underTest.consume(entryB);

		assertThat(underTest.getStats().size(), is(2));
	}
}