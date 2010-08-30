package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.Config.GroupBy;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.io.LogFiles;

/**
 * Integration test for {@link LogEntryFilter}s.
 * 
 * @author jorge.decastro
 * 
 */
public class LogEntryFilterIntegrationTest {
	private Config config;
	private LogEntryFilter underTest;

	@Before
	public void setup() {
		config = new Config();
		config.setFriendlyName("Example Log Integration Test");
		config.setSampleEntry("10.117.101.80 - - [15/Dec/2009:00:00:15 +0000] \"GET /example/action/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14");
		config.setActionPattern("(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))");
		config.setDurationPattern("(\\d+)$");
		config.setFilterPattern(".*save.do$");
		config.setTimestampFormat("dd/MMM/yyyy:HH:mm:ss");
		config.setTimestampPattern("\\[((.*?))\\]");
		config.setGroupBy(GroupBy.DAY_OF_MONTH);
		LogFiles logfiles = new LogFiles("EXAMPLE_log_(.+)-15.log", new String[] { "." });
		config.setLogFiles(logfiles);

		underTest = new LogEntryFilter(config);
	}

	@After
	public void teardown() {
		config = null;
		underTest = null;
	}

	@Test
	public void testFilterSettingsGivenConfigSettings() {
		assertThat(underTest.getActionPattern().pattern(), is(equalTo(config.getActionPattern())));
		assertThat(underTest.getDurationPattern().pattern(), is(equalTo(config.getDurationPattern())));
		assertThat(underTest.getFilterPattern().pattern(), is(equalTo(config.getFilterPattern())));
		assertThat(underTest.getTimestampPattern().pattern(), is(equalTo(config.getTimestampPattern())));
		assertThat(underTest.getConfig(), is(sameInstance(config)));
	}

	@Test
	public void testFilterParsesAndFiltersLogEntries() {
		LogFiles logfiles = config.getLogFiles();
		File[] files = logfiles.list();

		@SuppressWarnings("unchecked")
		LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(config, underTest);
		String filepath;
		int totalEntries = 0;
		int filteredEntries = 0;
		LogSnapshot<LogEntry> logSnapshot = null;
		for (File f : files) {
			filepath = f.getAbsolutePath();
			logSnapshot = lineByLineParser.filter(filepath);
			totalEntries = logSnapshot.getTotalEntries();
			filteredEntries = logSnapshot.getFilteredEntries().size();
		}
		System.out.println("\n" + logSnapshot.getDayStats().toString());

		assertThat(totalEntries, is(10822));
		assertThat(filteredEntries, is(167));
	}
}
