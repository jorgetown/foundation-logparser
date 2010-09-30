package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.time.ITimeInterval;

/**
 * Unit tests for the {@link LogEntryFilter}.
 * 
 * @author jorge.decastro
 */
public class LogEntryFilterTest {
	private static final String SAMPLE_LOG_MESSAGE = "[15/Dec/2009:00:00:15 +0000] GET /path/something.html?event=execute&eventId=37087422 HTTP/1.1 200 14 300";
	private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[((.*?))\\]");
	private static final String TIMESTAMP_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final Pattern ACTION_PATTERN = Pattern.compile("(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))");
	private static final Pattern DURATION_PATTERN = Pattern.compile("HTTP.*\\s((\\d)(.*))$");
	private static final Pattern FILTER_PATTERN = Pattern.compile(".*.html");

	private LogEntryFilter underTest;

	@Before
	public void setUp() {
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, ACTION_PATTERN, DURATION_PATTERN).build();
	}

	@After
	public void tearDown() {
		underTest = null;
	}

	@Test
	public void testRequiredTimestampPattern() {
		assertThat(underTest.getTimestampPattern(), is(equalTo(TIMESTAMP_PATTERN)));
	}

	@Test
	public void testRequiredTimestampFormat() {
		assertThat(underTest.getTimestampFormat(), is(equalTo(TIMESTAMP_FORMAT)));
	}

	@Test
	public void testRequiredActionPattern() {
		assertThat(underTest.getActionPattern(), is(equalTo(ACTION_PATTERN)));
	}

	@Test
	public void testRequiredDurationPattern() {
		assertThat(underTest.getDurationPattern(), is(equalTo(DURATION_PATTERN)));
	}

	@Test
	public void testOptionalFilterPatternHasDefaultValue() {
		assertThat(underTest.getFilterPattern().pattern(), is(equalTo(LogEntryFilter.DEFAULT_FILTER_PATTERN)));
	}

	@Test
	public void testOverridingOptionalFilterPatternReturnsTheOverride() {
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, ACTION_PATTERN, DURATION_PATTERN).filterPattern(FILTER_PATTERN).build();
		assertThat(underTest.getFilterPattern(), is(notNullValue()));
		assertThat(underTest.getFilterPattern(), is(equalTo(FILTER_PATTERN)));
	}

	@Test
	public void testOptionalTimeIntervalHasDefaultValue() {
		assertThat(underTest.getTimeInterval(), is(notNullValue()));
		assertThat(underTest.getTimeInterval(), is(instanceOf(ITimeInterval.class)));
	}

	@Test
	public void testOptionalDateIntervalHasDefaultValue() {
		assertThat(underTest.getDateInterval(), is(notNullValue()));
		assertThat(underTest.getDateInterval(), is(instanceOf(ITimeInterval.class)));
	}

	@Test
	public void testOptionalSampleEntryHasDefaultValue() {
		assertThat(underTest.getSampleEntry(), is(notNullValue()));
		assertThat(underTest.getSampleEntry(), is(equalTo("")));
	}

	@Test
	public void testOverridingOptionalSampleEntryReturnsTheOverride() {
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, ACTION_PATTERN, DURATION_PATTERN).sampleEntry(SAMPLE_LOG_MESSAGE).build();
		assertThat(underTest.getSampleEntry(), is(notNullValue()));
		assertThat(underTest.getSampleEntry(), is(equalTo(SAMPLE_LOG_MESSAGE)));
	}

	@Test(expected = NullPointerException.class)
	public void testFilterFailsCreationIfRequiredTimestampPatternArgumentIsNull() {
		underTest = new LogEntryFilter.Builder(null, TIMESTAMP_FORMAT, ACTION_PATTERN, DURATION_PATTERN).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test(expected = NullPointerException.class)
	public void testFilterFailsCreationIfRequiredTimestampFormatArgumentIsNull() {
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, null, ACTION_PATTERN, DURATION_PATTERN).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test(expected = NullPointerException.class)
	public void testFilterFailsCreationIfRequiredActionPatternArgumentIsNull() {
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, null, DURATION_PATTERN).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test(expected = NullPointerException.class)
	public void testFilterFailsCreationIfRequiredDurationPatternArgumentIsNull() {
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, ACTION_PATTERN, null).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testFilterIsCreatedIfNotNullRequiredArgumentsAreGiven() {
		assertThat(underTest, is(notNullValue()));
	}

	@Test
	public void testMalformedDurationPatternReturnsNullLogEntry() {
		Pattern durationPattern = Pattern.compile("^NOTPRESENT");
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, ACTION_PATTERN, durationPattern).build();
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedActionPatternReturnsNullLogEntry() {
		Pattern actionPattern = Pattern.compile("^NOT_THERE");
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, TIMESTAMP_FORMAT, actionPattern, DURATION_PATTERN).build();
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMalformedTimestampFormatReturnsNullLogEntry() {
		String timestampFormat = "MMM/yyyy:HH:mm";
		underTest = new LogEntryFilter.Builder(TIMESTAMP_PATTERN, timestampFormat, ACTION_PATTERN, DURATION_PATTERN).build();
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedTimestampPatternReturnsNullEntry() {
		Pattern timestampPattern = Pattern.compile("^NOTIMESTAMP");
		underTest = new LogEntryFilter.Builder(timestampPattern, TIMESTAMP_FORMAT, ACTION_PATTERN, DURATION_PATTERN).build();
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testParsableTimestampPatternReturnsLogEntry() {
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getTimestamp(), is(equalTo(1260835215000L)));
	}

	@Test
	public void testParsableActionPatternReturnsLogEntry() {
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getAction(), is(equalTo("/path/something.html")));
	}

	@Test
	public void testParsableDurationPatternReturnsLogEntry() {
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getDuration(), is(equalTo(300D)));
	}

	@Test
	public void testLogEntryTokensAreIndividuallyParsable() {
		String EXPECTED_TIMESTAMP = "15/Dec/2009:00:00:15";
		String EXPECTED_ACTION = "/path/something.html";
		double EXPECTED_DURATION = 300D;

		String LOG_ENTRY = String.format("1.1.1.1 - - [%s] GET %s?event=execute&eventId=37087422 HTTP/1.1 200 14 %s", EXPECTED_TIMESTAMP, EXPECTED_ACTION, EXPECTED_DURATION);

		LogEntry entry = underTest.parse(LOG_ENTRY);

		assertThat(entry, is(notNullValue()));
		assertThat(entry.getTimestamp(), is(equalTo(underTest.getDateFromString.apply(EXPECTED_TIMESTAMP).getTime())));
		assertThat(entry.getAction(), is(equalTo(EXPECTED_ACTION)));
		assertThat(entry.getDuration(), is(equalTo(EXPECTED_DURATION)));
	}
}