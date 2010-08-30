package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.After;
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
	private Config config;

	@Before
	public void setUp() {
		config = new Config();
		config.setFriendlyName("Test Log");
		config.setSampleEntry(SAMPLE_LOG_MESSAGE);
		config.setTimestampPattern("\\[((.*?))\\]");
		config.setTimestampFormat("dd/MMM/yyyy:HH:mm:ss");
		config.setActionPattern("\\[.*?\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))");
		config.setDurationPattern("HTTP.*\\s((\\d)(.*))$");
		config.setFilterPattern(".*.html");
		underTest = new LogEntryFilter(config);
	}

	@After
	public void tearDown() {
		config = null;
		underTest = null;
	}

	@Test
	public void testFilterActionPatternGivenConfigActionPattern() {
		assertThat(underTest.getActionPattern().pattern(), is(equalTo(config.getActionPattern())));
	}

	@Test
	public void testFilterDurationPatternGivenConfigDurationPattern() {
		assertThat(underTest.getDurationPattern().pattern(), is(equalTo(config.getDurationPattern())));
	}

	@Test
	public void testFilterPatternGivenConfigFilterPattern() {
		assertThat(underTest.getFilterPattern().pattern(), is(equalTo(config.getFilterPattern())));
	}

	@Test
	public void testFilterTimestampPatternGivenConfigTimestampPattern() {
		assertThat(underTest.getTimestampPattern().pattern(), is(equalTo(config.getTimestampPattern())));
	}

	@Test
	public void testFilterConfigGivenConfigInstance() {
		assertThat(underTest.getConfig(), is(sameInstance(config)));
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

	@Test(expected = IllegalArgumentException.class)
	public void testMalformedTimestampFormatReturnsNullEntry() {
		config.setTimestampFormat("MMM/yyyy:HH:mm");
		underTest = new LogEntryFilter(config);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedTimestampPatternReturnsNullEntry() {
		config.setTimestampPattern("^NOTIMESTAMP");
		underTest = new LogEntryFilter(config);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedActionPatternReturnsNullEntry() {
		config.setActionPattern("^NOTHERE");
		underTest = new LogEntryFilter(config);
		LogEntry entry = underTest.parse(SAMPLE_LOG_MESSAGE);
		assertThat(entry, is(nullValue()));
	}

	@Test
	public void testMalformedDurationPatternReturnsNullEntry() {
		config.setDurationPattern("^NOTPRESENT");
		underTest = new LogEntryFilter(config);
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