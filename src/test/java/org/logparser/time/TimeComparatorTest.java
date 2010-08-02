package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.logparser.LogEntry;

/**
 * Tests for {@link TimeComparator}.
 * 
 * @author jorge.decastro
 * 
 */
public class TimeComparatorTest {
	private static final String ORIGINAL_ENTRY_TEXT = "something.do 11ms";
	private static final String ACTION = "something.do";
	private static final String DURATION = "11";
	private TimeComparator<LogEntry> underTest;

	@Test(expected = NullPointerException.class)
	public void testNullTimeUnitArgument() {
		new TimeComparator<LogEntry>(0, null);
	}

	@Test
	public void testEntriesSeparatedInTimeNotWithinTimeIntervalGiven() {
		underTest = new TimeComparator<LogEntry>(3000, TimeUnit.MILLISECONDS);
		Date earlier = new Date();
		earlier.setTime(1280589260565L);
		Date later = new Date();
		later.setTime(1280589262565L); // 1280589260565 + 2000 = 2000ms later
		LogEntry entry1 = new LogEntry(ORIGINAL_ENTRY_TEXT, earlier, ACTION, DURATION);
		LogEntry entry2 = new LogEntry(ORIGINAL_ENTRY_TEXT, later, ACTION, DURATION);
		boolean isApart = underTest.isIntervalApart(entry1, entry2);
		assertThat(isApart, is(false));
	}

	@Test
	public void testEntriesSeparatedInTimeWithinTimeIntervalGiven() {
		underTest = new TimeComparator<LogEntry>(3000, TimeUnit.MILLISECONDS);
		Date earlier = new Date();
		earlier.setTime(1280589260565L);
		Date later = new Date();
		later.setTime(1280589264565L); // 1280589260565 + 4000 = 4000ms later
		LogEntry entry1 = new LogEntry(ORIGINAL_ENTRY_TEXT, earlier, ACTION, DURATION);
		LogEntry entry2 = new LogEntry(ORIGINAL_ENTRY_TEXT, later, ACTION, DURATION);
		boolean isApart = underTest.isIntervalApart(entry1, entry2);
		assertThat(isApart, is(true));
	}
}