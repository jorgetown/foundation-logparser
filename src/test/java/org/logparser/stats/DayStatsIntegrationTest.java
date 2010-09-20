package org.logparser.stats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
import org.logparser.config.FilterParams;
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
	private Config config;
	private LogEntryFilter underTest;
	private FilterParams filterParams;

	@Before
	public void setup() {
		String sampleEntry = "10.117.101.80 - - [15/Dec/2008:00:00:15 +0000] \"GET /example/action/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14";
		String timestampPattern = "\\[((.*?))\\]";
		String timestampFormat = "dd/MMM/yyyy:HH:mm:ss";
		String actionPattern = "(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))";
		String durationPattern = "(\\d+)$";
		String filterPattern = ".*save.do$";
		ITimeInterval timeInterval = new InfiniteTimeInterval();
		filterParams = new FilterParams(
				sampleEntry, 
				timestampPattern,
				timestampFormat, 
				actionPattern, 
				durationPattern, 
				filterPattern,
				timeInterval);
		
		config = new Config();
		config.setFriendlyName("Example Log Integration Test");
		LogFiles logfiles = new LogFiles("EXAMPLE_log_(.+)-15.log", new String[] { "." });
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
	@SuppressWarnings("unchecked")
	public void testDayStatsSummaryStatisticsAreCalculatedCorrectly() {
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
		Map<String, TimeStats<LogEntry>> dayStats = logSnapshot.getDayStats().getDayStats();

		assertThat(dayStats.keySet(), hasItem("/save.do"));

		TimeStats<LogEntry> timeStats = dayStats.get("/save.do");

		assertThat(timeStats, is(notNullValue()));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(20081215));

		StatisticalSummary stats = timeStats.getSummaryStatistics(20081215);

		assertThat(stats.getN(), is(167L));
		assertThat(stats.getMean(), is(71.55089820359282D));
		assertThat(stats.getStandardDeviation(), is(440.11781909270985D));
		assertThat(stats.getMax(), is(5421D));
		assertThat(stats.getMin(), is(11D));
	}
}
