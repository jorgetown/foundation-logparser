package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.logparser.Constants.DEFAULT_OUTPUT_DIR;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.config.Config;
import org.logparser.config.FilterProvider;
import org.logparser.config.LogFilesProvider;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.io.LogFiles;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Integration tests for {@link LogEntryFilter}s.
 * 
 * @author jorge.decastro
 * 
 */
public class LogEntryFilterIntegrationTest {
	private static final String SAMPLE_ENTRY = "10.117.101.80 - - [15/Dec/2009:00:00:15 +0000] \"GET /example/action/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14";
	private static final String TIMESTAMP_PATTERN = "\\[((.*?))\\]";
	private static final String TIMESTAMP_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final String ACTION_PATTERN = "(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))";
	private static final String DURATION_PATTERN = "(\\d+)$";
	private static final String FILTER_PATTERN = ".*save.do$";
	private Config config; // TODO remove config dependency
	private FilterProvider filterProvider;
	private LogEntryFilter underTest;

	@Before
	public void setup() {
		ITimeInterval timeInterval = new InfiniteTimeInterval();
		ITimeInterval dateInterval = new InfiniteTimeInterval();
		filterProvider = new FilterProvider(
				SAMPLE_ENTRY,
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT,
				ACTION_PATTERN,
				DURATION_PATTERN,
				FILTER_PATTERN,
				timeInterval,
				dateInterval);

		config = new Config();
		config.setFriendlyName("Example Log Integration Test");
		LogFilesProvider logFilesProvider = new LogFilesProvider(
				"EXAMPLE_log_(.+)-15.log",
				new String[] { DEFAULT_OUTPUT_DIR },
				DEFAULT_OUTPUT_DIR,
				null);
		config.setLogFilesProvider(logFilesProvider);
		config.setFilterProvider(filterProvider);

		underTest = filterProvider.build();
	}

	@After
	public void teardown() {
		config = null;
		filterProvider = null;
		underTest = null;
	}

	@Test
	public void testLogFilterSettingsAgainstGivenParams() {
		assertThat(underTest.getTimestampPattern().pattern(), is(equalTo(TIMESTAMP_PATTERN)));
		assertThat(underTest.getTimestampFormat(), is(equalTo(TIMESTAMP_FORMAT)));
		assertThat(underTest.getActionPattern().pattern(), is(equalTo(ACTION_PATTERN)));
		assertThat(underTest.getDurationPattern().pattern(), is(equalTo(DURATION_PATTERN)));
		assertThat(underTest.getFilterPattern().pattern(), is(equalTo(FILTER_PATTERN)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLogFilterParsesAndFiltersLogEntries() {
		LogFiles logfiles = config.getLogFilesProvider().build();
		File[] files = logfiles.list();

		LogSnapshot<LogEntry> logSnapshot = new LogSnapshot<LogEntry>(config);

		LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(underTest);
		lineByLineParser.attach(logSnapshot);

		String filepath;
		for (File f : files) {
			filepath = f.getAbsolutePath();
			lineByLineParser.filter(filepath);
		}
		System.out.println("\n" + logSnapshot.toString());

		assertThat(lineByLineParser.size(), is(10822));
		assertThat(logSnapshot.getFilteredEntries().size(), is(167));
	}
}
