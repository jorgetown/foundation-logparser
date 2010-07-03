package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link LogEntryFilter}.
 * 
 * @author jorge.decastro
 */
public class LogEntryFilterTest {
	private static final String SAMPLE_LOG_MESSAGE = "[15/Dec/2009:00:00:15 +0000] GET /path/something.html?event=execute&eventId=37087422 HTTP/1.1 200 14 300";
	private LogEntryFilter underTest;
	private FilterConfig filterConfig;

	@Before
	public void setUp() {
		filterConfig = new FilterConfig();
		filterConfig.setFriendlyName("Test Log");
		filterConfig.setSampleMessage(SAMPLE_LOG_MESSAGE);
		filterConfig.setTimestampPattern("\\[((.*?))\\]");
		filterConfig.setTimestampFormat("dd/MMM/yyyy:HH:mm:ss");
		filterConfig.setActionPattern("\\[.*?\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))");
		filterConfig.setDurationPattern("HTTP.*\\s((\\d)(.*))$");
	}

	@Test(expected = NullPointerException.class)
	public void testNullFilterConfigArgument() {
		underTest = new LogEntryFilter(null);
	}

	@Test
	public void testNotNullFilterConfigArgument() {
		underTest = new LogEntryFilter(filterConfig);
		assertThat(underTest, is(notNullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMalformedTimestampFormatReturnsNullEntry() {
		filterConfig.setTimestampFormat("MMM/yyyy:HH:mm");
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedTimestampPatternReturnsNullEntry() {
		filterConfig.setTimestampPattern("^NOTIMESTAMP");
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedActionPatternReturnsNullEntry() {
		filterConfig.setActionPattern("^NOTHERE");
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedDurationPatternReturnsNullEntry() {
		filterConfig.setDurationPattern("^NOTPRESENT");
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testParsableTimestampPatternReturnsEntry() {
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getTimestamp(), is(equalTo(1260835215000L)));
	}

	@Test
	public void testParsableActionPatternReturnsEntry() {
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getAction(), is(equalTo("/path/something.html")));
	}

	@Test
	public void testParsableDurationPatternReturnsEntry() {
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getDuration(), is(equalTo(300D)));
	}

	@Test
	public void testOriginalLogEntryIsKept() {
		underTest = new LogEntryFilter(filterConfig);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getMessage(), is(equalTo(SAMPLE_LOG_MESSAGE)));
	}
	
	@Test
	public void testParsableTokensInLogEntry() {
		underTest = new LogEntryFilter(filterConfig);
		String EXPECTED_TIMESTAMP = "15/Dec/2009:00:00:15";
		String EXPECTED_ACTION = "/path/something.html";
		double EXPECTED_DURATION = 300D;

		String LOG_ENTRY = String.format("1.1.1.1 - - [%s] GET %s?event=execute&eventId=37087422 HTTP/1.1 200 14 %s",
				EXPECTED_TIMESTAMP, EXPECTED_ACTION, EXPECTED_DURATION);

		LogEntry entry = underTest.parse(LOG_ENTRY);

		assertThat(entry, is(notNullValue()));
		assertThat(LOG_ENTRY, is(equalTo(entry.getMessage())));
		assertThat(EXPECTED_TIMESTAMP, is(equalTo(underTest.getDateFormatter().format(entry.getDate()))));
		assertThat(EXPECTED_ACTION, is(equalTo(entry.getAction())));
		assertThat(EXPECTED_DURATION, is(equalTo(entry.getDuration())));
	}
}