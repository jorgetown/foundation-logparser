package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.logparser.Constants.DEFAULT_DECIMAL_FORMAT;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link LogSnapshot}.
 * 
 * @author jorge.decastro
 * 
 */
public class LogSnapshotTest {
	private LogEntry entryA;
	private LogEntry entryB;
	private Calendar calendar;
	private TimeZone timeZone;
	private DecimalFormat decimalFormat;

	private LogSnapshot<LogEntry> underTest;

	@Before
	public void setUp() {
		timeZone = TimeZone.getTimeZone("GMT+0000");
		calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(1280589260565L);
		entryA = new LogEntry(calendar.getTimeInMillis(), "/action.a", 2073D);
		entryB = new LogEntry(calendar.getTimeInMillis(), "/action.b", 2073D);
		decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT);
		underTest = new LogSnapshot<LogEntry>();
	}

	@After
	public void tearDown() {
		entryA = null;
		entryB = null;
		timeZone = null;
		calendar = null;
		decimalFormat = null;
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testLogSnapshotNullDecimalFormatArgument() {
		underTest = new LogSnapshot<LogEntry>(true, null);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testLogSnapshotDefaultValues() {
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getFilteredEntries().size(), is(0));
		assertThat(underTest.getSize(), is(0));
		assertThat(underTest.getSummary().size(), is(0));
		assertThat(underTest.isStoreFilteredEntries(), is(true));
		assertThat(underTest.getDecimalFormat(), is(notNullValue()));
	}

	@Test
	public void testLogSnapshotAfterNullConsumedEntry() {
		underTest.consume(null);

		assertThat(underTest.getFilteredEntries().size(), is(0));
		assertThat(underTest.getSummary().size(), is(0));
		assertThat(underTest.getSize(), is(0));
	}

	@Test
	public void testLogSnapshotAfterSingleConsumedEntry() {
		boolean storeFilteredEntries = true;
		underTest = new LogSnapshot<LogEntry>(storeFilteredEntries, decimalFormat);
		underTest.consume(entryA);

		assertThat(underTest.getFilteredEntries().size(), is(1));
		assertThat(underTest.getFilteredEntries(), hasItem(entryA));
		assertThat(underTest.getSummary().size(), is(1));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.a"));
		assertThat(underTest.getSize(), is(1));
	}

	@Test
	public void testLogSnapshotAfterSingleConsumedEntryAndStoringDisabled() {
		boolean storeFilteredEntries = false;
		underTest = new LogSnapshot<LogEntry>(storeFilteredEntries, decimalFormat);
		underTest.consume(entryA);

		assertThat(underTest.getFilteredEntries().size(), is(0));
		assertThat(underTest.getSummary().size(), is(1));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.a"));
		assertThat(underTest.getSize(), is(1));
	}

	@Test
	public void testLogSnapshotAfterMultipleConsumedEntries() {
		boolean storeFilteredEntries = true;
		underTest = new LogSnapshot<LogEntry>(storeFilteredEntries, decimalFormat);

		underTest.consume(entryA);
		underTest.consume(entryB);

		assertThat(underTest.getFilteredEntries().size(), is(2));
		assertThat(underTest.getFilteredEntries(), hasItem(entryA));
		assertThat(underTest.getFilteredEntries(), hasItem(entryB));
		assertThat(underTest.getSummary().size(), is(2));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.a"));
		assertThat(underTest.getSummary().keySet(), hasItem("/action.b"));
		assertThat(underTest.getSize(), is(2));
	}
}