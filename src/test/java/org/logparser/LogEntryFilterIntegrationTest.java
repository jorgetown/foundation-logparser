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
import org.logparser.config.FilterParams;
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
	private Config config;
	private FilterParams filterParams;
	private LogEntryFilter underTest;

	@Before
	public void setup() {
		String sampleEntry = "10.117.101.80 - - [15/Dec/2009:00:00:15 +0000] \"GET /example/action/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14";
		String timestampPattern = "\\[((.*?))\\]";
		String timestampFormat = "dd/MMM/yyyy:HH:mm:ss";
		String actionPattern = "(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))";
		String durationPattern = "(\\d+)$";
		String filterPattern = ".*save.do$";
		ITimeInterval timeInterval = new InfiniteTimeInterval();
		ITimeInterval dateInterval = new InfiniteTimeInterval();
		filterParams = new FilterParams(
				sampleEntry, 
				timestampPattern,
				timestampFormat, 
				actionPattern, 
				durationPattern, 
				filterPattern,
				timeInterval,
				dateInterval);

		config = new Config();
		config.setFriendlyName("Example Log Integration Test");
		LogFiles logfiles = new LogFiles("EXAMPLE_log_(.+)-15.log", new String[] { DEFAULT_OUTPUT_DIR });
		config.setLogFiles(logfiles);
		config.setFilterParams(filterParams);

		underTest = new LogEntryFilter(filterParams);
	}

	@After
	public void teardown() {
		config = null;
		filterParams = null;
		underTest = null;
	}

	@Test
	public void testLogFilterSettingsAgainstGivenParams() {
		assertThat(underTest.getActionPattern(), is(equalTo(filterParams.getActionPattern())));
		assertThat(underTest.getDurationPattern(), is(equalTo(filterParams.getDurationPattern())));
		assertThat(underTest.getFilterPattern(), is(equalTo(filterParams.getFilterPattern())));
		assertThat(underTest.getTimestampPattern(), is(equalTo(filterParams.getTimestampPattern())));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLogFilterParsesAndFiltersLogEntries() {
		LogFiles logfiles = config.getLogFiles();
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
