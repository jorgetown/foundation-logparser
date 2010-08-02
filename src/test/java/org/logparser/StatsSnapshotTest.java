package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link StatsSnapshot}.
 * 
 * @author jorge.decastro
 * 
 */
public class StatsSnapshotTest {
	private static final String EARLIEST_ENTRY = "10.118.101.132 - - [31/Jul/2010:16:14:20 +0000] \"POST /earliest.entry HTTP/1.1\" 200 1779 2000";
	private static final String LATEST_ENTRY = "10.118.101.132 - - [15/Jul/2010:16:14:30 +0000] \"POST /latest.entry HTTP/1.1\" 200 1779 1000";
	private static final String MAXIMA_ENTRY = "10.118.101.132 - - [31/Jul/2010:16:14:20 +0000] \"POST /maxima.entry HTTP/1.1\" 200 1779 2000";
	private static final String MINIMA_ENTRY = "10.118.101.132 - - [31/Jul/2010:16:14:30 +0000] \"POST /minima.entry HTTP/1.1\" 200 1779 1000";
	private LogEntry maxima;
	private LogEntry minima;
	private LogEntry earliest;
	private LogEntry latest;
	private int groubBy;
	private Calendar calendar;
	private TimeZone timeZone;

	private StatsSnapshot<LogEntry> underTest;

	@Before
	public void setUp() {
		timeZone = TimeZone.getTimeZone("GMT+0000");
		calendar = Calendar.getInstance(timeZone);
		calendar.setTimeInMillis(1280589260565L);
		Date earliestTime = calendar.getTime();
		calendar.setTimeInMillis(1280589270565L); // 10secs later
		Date latestTime = calendar.getTime();
		maxima = new LogEntry(MAXIMA_ENTRY, earliestTime, "/maxima.entry", "2000");
		minima = new LogEntry(MINIMA_ENTRY, latestTime, "/minima.entry", "1000");
		earliest = new LogEntry(EARLIEST_ENTRY, earliestTime, "/earliest.entry", "2000");
		latest = new LogEntry(LATEST_ENTRY, latestTime, "/latest.entry", "1000");
		groubBy = Calendar.MINUTE;
		underTest = new StatsSnapshot<LogEntry>(groubBy);
	}

	@After
	public void tearDown() {
		earliest = null;
		latest = null;
		maxima = null;
		minima = null;
		timeZone = null;
		calendar = null;
		underTest = null;
	}

	@Test
	public void testJsonStringGeneratedIsAsExpected() throws JsonGenerationException, JsonMappingException, IOException {
		underTest = new StatsSnapshot<LogEntry>();
		String expected = "{\"mean\":0.0,\"deviation\":0.0,\"maxima\":null,\"minima\":null,\"timeBreakdown\":{},\"entries\":[]}";
		assertThat(underTest.toJsonString(), is(equalTo(expected)));
	}

	@Test
	public void testStatsAreInitializedWithDefaultValues() {
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getEntries().size(), is(0));
		assertThat(underTest.getEarliestEntry(), is(nullValue()));
		assertThat(underTest.getLatestEntry(), is(nullValue()));
		assertThat(underTest.getMaxima(), is(nullValue()));
		assertThat(underTest.getMinima(), is(nullValue()));
		assertThat(underTest.getTimeBreakdown().size(), is(0));
		assertThat(underTest.getMean(), is(0.0));
		assertThat(underTest.getDeviation(), is(0.0));
	}

	@Test(expected = NullPointerException.class)
	public void testMeanOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getMean(), is(0D));
	}

	@Test
	public void testMeanOfSingleConsumedEntry() {
		underTest.add(earliest);

		assertThat(underTest.getMean(), is(2000D));
	}

	@Test
	public void testMeanOfMultipleConsumedEntries() {
		underTest.add(earliest);
		underTest.add(latest);

		assertThat(underTest.getMean(), is(1500D));
	}

	@Test(expected = NullPointerException.class)
	public void testStandardDeviationOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getDeviation(), is(0D));
	}

	@Test
	public void testStandartDeviationOfSingleConsumedEntry() {
		underTest.add(earliest);

		assertThat(underTest.getDeviation(), is(0D));
	}

	@Test
	public void testStandardDeviationOfMultipleConsumedEntries() {
		underTest.add(earliest);
		underTest.add(latest);

		assertThat(underTest.getDeviation(), is(707.11D));
	}

	@Test(expected = NullPointerException.class)
	public void testEntriesWithNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getEntries().size(), is(0));
	}

	@Test
	public void testEntriesWithSingleConsumedEntry() {
		underTest.add(earliest);

		assertThat(underTest.getEntries().size(), is(1));
		assertThat(underTest.getEntries(), hasItem(earliest));
	}

	@Test
	public void testEntriesWithMultipleConsumedEntries() {
		underTest.add(earliest);
		underTest.add(latest);

		assertThat(underTest.getEntries().size(), is(2));
		assertThat(underTest.getEntries(), hasItem(earliest));
		assertThat(underTest.getEntries(), hasItem(latest));
	}

	@Test(expected = NullPointerException.class)
	public void testMaximaOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getMaxima(), is(nullValue()));
	}

	@Test
	public void testMaximaOfSingleConsumedEntry() {
		underTest.add(maxima);

		assertThat(underTest.getMaxima(), is(notNullValue()));
		assertThat(underTest.getMaxima(), is(maxima));
	}

	@Test
	public void testMaximaOfMultipleConsumedEntries() {
		underTest.add(maxima);
		underTest.add(minima);

		assertThat(underTest.getMaxima(), is(notNullValue()));
		assertThat(underTest.getMaxima(), is(maxima));
	}

	@Test(expected = NullPointerException.class)
	public void testMinimaOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getMinima(), is(nullValue()));
	}

	@Test
	public void testMinimaOfSingleConsumedEntry() {
		underTest.add(minima);

		assertThat(underTest.getMinima(), is(notNullValue()));
		assertThat(underTest.getMinima(), is(minima));
	}

	@Test
	public void testMinimaOfMultipleConsumedEntries() {
		underTest.add(maxima);
		underTest.add(minima);

		assertThat(underTest.getMinima(), is(notNullValue()));
		assertThat(underTest.getMinima(), is(minima));
	}

	@Test(expected = NullPointerException.class)
	public void testEarliestOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getEarliestEntry(), is(nullValue()));
	}

	@Test
	public void testEarliestOfSingleConsumedEntry() {
		underTest.add(earliest);

		assertThat(underTest.getEarliestEntry(), is(notNullValue()));
		assertThat(underTest.getEarliestEntry(), is(earliest));
	}

	@Test
	public void testEarliestOfMultipleConsumedEntries() {
		underTest.add(earliest);
		underTest.add(latest);

		assertThat(underTest.getEarliestEntry(), is(notNullValue()));
		assertThat(underTest.getEarliestEntry(), is(earliest));
	}

	@Test(expected = NullPointerException.class)
	public void testLatestOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getLatestEntry(), is(nullValue()));
	}

	@Test
	public void testLatestOfSingleConsumedEntry() {
		underTest.add(latest);

		assertThat(underTest.getLatestEntry(), is(notNullValue()));
		assertThat(underTest.getLatestEntry(), is(latest));
	}

	@Test
	public void testLatestOfMultipleConsumedEntries() {
		underTest.add(earliest);
		underTest.add(latest);

		assertThat(underTest.getLatestEntry(), is(notNullValue()));
		assertThat(underTest.getLatestEntry(), is(latest));
	}

	@Test(expected = NullPointerException.class)
	public void testTimeBreakdownOfNullConsumedEntry() {
		underTest.add(null);

		assertThat(underTest.getTimeBreakdown().size(), is(0));
	}

	@Test
	public void testTimeBreakdownOfSingleConsumedEntry() {
		underTest.add(earliest);

		assertThat(underTest.getTimeBreakdown().size(), is(1));
		assertThat(underTest.getTimeBreakdown().keySet(), hasItem(14));
	}

	@Test
	public void testTimeBreakdownWithMultipleConsumedEntries() {
		underTest.add(earliest);
		underTest.add(latest);

		assertThat(underTest.getTimeBreakdown().size(), is(1));
		assertThat(underTest.getTimeBreakdown().keySet(), hasItem(14));
		assertThat(underTest.getTimeBreakdown().values(), hasItem(2));
	}
}