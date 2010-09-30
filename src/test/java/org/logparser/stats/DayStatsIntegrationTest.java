package org.logparser.stats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.logparser.Constants.DEFAULT_OUTPUT_DIR;

import java.io.File;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.LogEntry;
import org.logparser.LogEntryFilter;
import org.logparser.LogSnapshot;
import org.logparser.config.Config;
import org.logparser.config.FilterProvider;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.io.LogFiles;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Integration tests for {@link DayStats}.
 * 
 * @author jorge.decastro
 * 
 */
public class DayStatsIntegrationTest {
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
		LogFiles logfiles = new LogFiles("EXAMPLE_log_(.+)-15.log", new String[] { DEFAULT_OUTPUT_DIR });
		config.setLogFiles(logfiles);
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
	@SuppressWarnings("unchecked")
	public void testDayStatsSummaryStatisticsAreCalculatedCorrectly() {
		LogFiles logfiles = config.getLogFiles();
		File[] files = logfiles.list();

		LogSnapshot<LogEntry> logSnapshot = new LogSnapshot<LogEntry>(config);
		DayStats<LogEntry> dayStats = new DayStats<LogEntry>();

		LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(underTest);
		lineByLineParser.attach(logSnapshot);
		lineByLineParser.attach(dayStats);

		String filepath;
		for (File f : files) {
			filepath = f.getAbsolutePath();
			lineByLineParser.filter(filepath);
		}
		Map<String, TimeStats<LogEntry>> stats = dayStats.getDayStats();

		System.out.println("\n" + dayStats.toString());

		assertThat(stats.keySet(), hasItem("/save.do"));

		TimeStats<LogEntry> timeStats = stats.get("/save.do");

		assertThat(timeStats, is(notNullValue()));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(20081215));

		StatisticalSummary summaryStats = timeStats.getSummaryStatistics(20081215);

		assertThat(summaryStats.getN(), is(167L));
		assertThat(summaryStats.getMean(), is(71.55089820359282D));
		assertThat(summaryStats.getStandardDeviation(), is(440.11781909270985D));
		assertThat(summaryStats.getMax(), is(5421D));
		assertThat(summaryStats.getMin(), is(11D));
	}
}
