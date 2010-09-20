package org.logparser.stats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.LogEntry;

/**
 * Tests for {@link TimeStats}.
 * 
 * @author jorge.decastro
 * 
 */
public class TimeStatsTest {
	private LogEntry entryXAtTimeA;
	private LogEntry entryYAtTimeA;
	private LogEntry entryXAtTimeB;
	private LogEntry entryYAtTimeB;
	private TimeStats<LogEntry> underTest;

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
		entryYAtTimeB = new LogEntry(timeB.getTime(), "/entry.y", 4000D);
		underTest = new TimeStats<LogEntry>(Calendar.DAY_OF_MONTH);
	}

	@After
	public void tearDown() {
		entryXAtTimeA = null;
		entryYAtTimeA = null;
		entryXAtTimeB = null;
		entryYAtTimeB = null;
		underTest = null;
	}

	@Test
	public void testTimeCriteriaHasDefaultValue() {
		underTest = new TimeStats<LogEntry>();
		assertThat(underTest.getTimeCriteria(), is(equalTo(TimeStats.DEFAULT_TIME_CRITERIA)));
	}

	@Test
	public void testTimeCriteriaIsOverridable() {
		underTest = new TimeStats<LogEntry>(Calendar.HOUR_OF_DAY);
		assertThat(underTest.getTimeCriteria(), is(equalTo(Calendar.HOUR_OF_DAY)));
	}

	@Test(expected = NullPointerException.class)
	public void testAddNullLogEntryResultsInEmptyTimeStats() {
		underTest.add(null);
		assertThat(underTest.getTimeStats().isEmpty(), is(true));
	}

	@Test
	public void testAddNotNullLogEntryPopulatesTimeStats() {
		underTest.add(entryXAtTimeA);
		assertThat(underTest.getTimeStats().isEmpty(), is(false));
		assertThat(underTest.getTimeStats().size(), is(1));
	}

	@Test
	public void testExtractingNonExistingStatsReturnsNullStatisticalSummary() {
		StatisticalSummary summary = underTest.getSummaryStatistics(27);
		assertThat(summary, is(nullValue()));
	}

	@Test
	public void testStatisticalSummaryOfSingleAddedLogEntry() {
		underTest.add(entryXAtTimeA);
		assertThat(underTest.getTimeStats().size(), is(1));
		StatisticalSummary summary = underTest.getSummaryStatistics(20100727);
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getN(), is(1L));
		assertThat(summary.getMean(), is(1000D));
		assertThat(summary.getStandardDeviation(), is(0D));
		assertThat(summary.getMax(), is(1000D));
		assertThat(summary.getMin(), is(1000D));
	}

	@Test
	public void testStatisticalSummaryOfMultipleLogEntriesAtSameTime() {
		underTest.add(entryXAtTimeA);
		underTest.add(entryYAtTimeA);
		assertThat(underTest.getTimeStats().size(), is(1));
		StatisticalSummary summary = underTest.getSummaryStatistics(20100727);
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getN(), is(2L));
		assertThat(summary.getMean(), is(1500D));
		assertThat(summary.getStandardDeviation(), is(707.1067811865476D));
		assertThat(summary.getMax(), is(2000D));
		assertThat(summary.getMin(), is(1000D));
	}

	@Test
	public void testStatisticalSummaryOfMultipleLogEntriesAtDifferentTimes() {
		underTest.add(entryXAtTimeA);
		underTest.add(entryYAtTimeA);
		underTest.add(entryXAtTimeB);
		underTest.add(entryYAtTimeB);
		assertThat(underTest.getTimeStats().size(), is(2));
		StatisticalSummary summary = underTest.getSummaryStatistics(20100727);
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getN(), is(2L));
		assertThat(summary.getMean(), is(1500D));
		assertThat(summary.getStandardDeviation(), is(707.1067811865476D));
		assertThat(summary.getMax(), is(2000D));
		assertThat(summary.getMin(), is(1000D));
		summary = underTest.getSummaryStatistics(20100731);
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getN(), is(2L));
		assertThat(summary.getMean(), is(3500D));
		assertThat(summary.getStandardDeviation(), is(707.1067811865476D));
		assertThat(summary.getMax(), is(4000D));
		assertThat(summary.getMin(), is(3000D));
	}
}