package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.config.FilterParams;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Unit tests for the {@link LogEntryFilter}.
 * 
 * @author jorge.decastro
 */
public class LogEntryFilterTest {
	private static final String SAMPLE_LOG_MESSAGE = "[15/Dec/2009:00:00:15 +0000] GET /path/something.html?event=execute&eventId=37087422 HTTP/1.1 200 14 300";
	private static final String TIMESTAMP_PATTERN = "\\[((.*?))\\]";
	private static final String TIMESTAMP_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final String ACTION_PATTERN = "(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))";
	private static final String DURATION_PATTERN = "HTTP.*\\s((\\d)(.*))$";
	private static final String FILTER_PATTERN = ".*.html";
	private ITimeInterval timeInterval = new InfiniteTimeInterval();
	private ITimeInterval dateInterval = new InfiniteTimeInterval();

	private LogEntryFilter underTest;
	private FilterParams filterParams;

	@Before
	public void setUp() {
		filterParams = new FilterParams(
				SAMPLE_LOG_MESSAGE, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN,
				FILTER_PATTERN, 
				timeInterval,
				dateInterval);

		underTest = new LogEntryFilter(filterParams);
	}

	@After
	public void tearDown() {
		timeInterval = null;
		filterParams = null;
		underTest = null;
	}

	@Test
	public void testActionPatternAgainstThatOfGivenParams() {
		assertThat(underTest.getActionPattern(), is(equalTo(filterParams.getActionPattern())));
	}

	@Test
	public void testDurationPatternAgainstThatOfGivenParams() {
		assertThat(underTest.getDurationPattern(), is(equalTo(filterParams.getDurationPattern())));
	}

	@Test
	public void testFilterPatternGiveAgainstThatOfGivenParams() {
		assertThat(underTest.getFilterPattern(), is(equalTo(filterParams.getFilterPattern())));
	}

	@Test
	public void testTimestampPatternAgainstThatOfGivenParams() {
		assertThat(underTest.getTimestampPattern(), is(equalTo(filterParams.getTimestampPattern())));
	}

	@Test(expected = NullPointerException.class)
	public void testFilterFailsCreationIfNullConfigArgument() {
		underTest = new LogEntryFilter(null);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testFilterIsCreatedIfNotNullConfigArgument() {
		assertThat(underTest, is(notNullValue()));
	}

	@Test
	public void testMalformedDurationPatternReturnsNullEntry() {
		filterParams = new FilterParams(
				SAMPLE_LOG_MESSAGE, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				"^NOTPRESENT",
				FILTER_PATTERN, 
				timeInterval,
				dateInterval);

		underTest = new LogEntryFilter(filterParams);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedActionPatternReturnsNullEntry() {
		filterParams = new FilterParams(
				SAMPLE_LOG_MESSAGE, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				"^NO_THERE", 
				DURATION_PATTERN, 
				FILTER_PATTERN,
				timeInterval,
				dateInterval);

		underTest = new LogEntryFilter(filterParams);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMalformedTimestampFormatReturnsNullEntry() {
		filterParams = new FilterParams(
				SAMPLE_LOG_MESSAGE, 
				TIMESTAMP_PATTERN,
				"MMM/yyyy:HH:mm", 
				ACTION_PATTERN, 
				DURATION_PATTERN,
				FILTER_PATTERN, 
				timeInterval,
				dateInterval);

		underTest = new LogEntryFilter(filterParams);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedTimestampPatternReturnsNullEntry() {
		filterParams = new FilterParams(
				SAMPLE_LOG_MESSAGE, 
				"^NOTIMESTAMP",
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN,
				FILTER_PATTERN, 
				timeInterval,
				dateInterval);

		underTest = new LogEntryFilter(filterParams);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);

		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testParsableTimestampPatternReturnsEntry() {
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getTimestamp(), is(equalTo(1260835215000L)));
	}

	@Test
	public void testParsableActionPatternReturnsEntry() {
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getAction(), is(equalTo("/path/something.html")));
	}

	@Test
	public void testParsableDurationPatternReturnsEntry() {
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
		assertThat(EXPECTED_TIMESTAMP, is(equalTo(underTest.getDateFormatter().format(entry.getTimestamp()))));
		assertThat(EXPECTED_ACTION, is(equalTo(entry.getAction())));
		assertThat(EXPECTED_DURATION, is(equalTo(entry.getDuration())));
	}
}