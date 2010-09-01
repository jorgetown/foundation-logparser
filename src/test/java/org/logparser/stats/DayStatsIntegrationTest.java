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
import org.logparser.Config;
import org.logparser.Config.GroupBy;
import org.logparser.LogEntry;
import org.logparser.LogEntryFilter;
import org.logparser.LogSnapshot;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.io.LogFiles;

/**
 * Integration tests for {@link DayStats}.
 * 
 * @author jorge.decastro
 * 
 */
public class DayStatsIntegrationTest {
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
	public void testDayStatsSummaryStatisticsAreCalculatedCorrectly() {
		LogFiles logfiles = config.getLogFiles();
		File[] files = logfiles.list();

		@SuppressWarnings("unchecked")
		LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(config, underTest);
		String filepath;
		LogSnapshot<LogEntry> logSnapshot = null;
		for (File f : files) {
			filepath = f.getAbsolutePath();
			logSnapshot = lineByLineParser.filter(filepath);
		}
		Map<String, TimeStats<LogEntry>> dayStats = logSnapshot.getDayStats().getDayStats();

		assertThat(dayStats.keySet(), hasItem("/save.do"));

		TimeStats<LogEntry> timeStats = dayStats.get("/save.do");

		assertThat(timeStats, is(notNullValue()));
		assertThat(timeStats.getTimeStats().keySet(), hasItem(15));

		StatisticalSummary stats = timeStats.getSummaryStatistics(15);

		assertThat(stats.getN(), is(167L));
		assertThat(stats.getMean(), is(71.55089820359282D));
		assertThat(stats.getStandardDeviation(), is(440.11781909270985D));
		assertThat(stats.getMax(), is(5421D));
		assertThat(stats.getMin(), is(11D));
	}
}
