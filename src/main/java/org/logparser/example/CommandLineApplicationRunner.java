package org.logparser.example;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.logparser.ILogEntryFilter;
import org.logparser.LogEntry;
import org.logparser.LogEntryFilter;
import org.logparser.LogSnapshot;
import org.logparser.io.ChartView;
import org.logparser.io.CommandLineArguments;
import org.logparser.io.CsvView;
import org.logparser.io.GoogleChartView;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.io.LogFiles;
import org.logparser.provider.ChartParams;
import org.logparser.provider.Config;
import org.logparser.provider.FilterProvider;
import org.logparser.provider.LogFilesProvider;
import org.logparser.provider.StatsProvider;
import org.logparser.stats.AbstractStats;
import org.logparser.stats.DayStats;
import org.logparser.stats.HourStats;
import org.logparser.stats.MinuteStats;
import org.logparser.stats.PredicateArguments;
import org.logparser.stats.TimeStats;
import org.logparser.stats.WeekDayStats;

import com.beust.jcommander.JCommander;
import com.google.common.base.Predicate;

/**
 * Responsible for running the log parser via the command line.
 * 
 * Example usage:
 * 
 * Run the maven assembly plugin to create a bundle with all the dependencies:
 * 
 * <pre>
 * 		mvn clean package assembly:single
 * </pre>
 * 
 * Execute the generated jar (running the default 'example' below):
 * 
 * <pre>
 * 		java -Xms256m -Xmx256m -jar target/log-parser-1.9.X-jar-with-dependencies.jar -configfile config.json -logname example
 * </pre>
 * 
 * @author jorge.decastro
 */
@SuppressWarnings("unchecked")
public class CommandLineApplicationRunner {

	public static void main(String[] args) {
		CommandLineArguments cla = new CommandLineArguments();
		JCommander jc = new JCommander(cla, args);
		if (cla.help) {
			jc.usage();
			return;
		}

		Config config = getConfig(cla);

		if (config != null) {
			LogFilesProvider logFilesProvider = config.getLogFilesProvider();
			logFilesProvider.applyCommandLineOverrides(cla);
			LogFiles logfiles = logFilesProvider.build();

			File[] listOfLogFiles = logfiles.list();
			if (listOfLogFiles.length > 0) { // there's something to work with
				String outputDir = logfiles.getOutputDir();

				FilterProvider filterProvider = config.getFilterProvider();
				filterProvider.applyCommandLineOverrides(cla);
				LogEntryFilter filter = filterProvider.build();

				// for large log files sampling is preferred/required
				ILogEntryFilter<LogEntry> sampler = config.getSamplerProvider() != null ? config.getSamplerProvider().build(filter) : filter;

				// sampler returns filter if unable to decorate
				LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(sampler);
				LogSnapshot<LogEntry> logSnapshot = new LogSnapshot<LogEntry>();
				DayStats<LogEntry> dayStats = null;
				WeekDayStats<LogEntry> weekStats = null;
				HourStats<LogEntry> hourStats = null;
				MinuteStats<LogEntry> minuteStats = null;
				StatsProvider statsProvider = config.getStatsProvider();
				if (statsProvider != null) {
					logSnapshot = statsProvider.buildLogSnapshot();
					dayStats = statsProvider.buildDayStats();
					weekStats = statsProvider.buildWeekDayStats();
					hourStats = statsProvider.buildHourStats();
					minuteStats = statsProvider.buildMinuteStats();
					lineByLineParser.attach(dayStats);
					lineByLineParser.attach(weekStats);
					lineByLineParser.attach(hourStats);
					lineByLineParser.attach(minuteStats);
				}
				lineByLineParser.attach(logSnapshot);
				CsvView csvView = new CsvView(outputDir);
				// submit csv serializables in the order we want them presented
				csvView.submit(logSnapshot);
				csvView.submit(dayStats);
				csvView.submit(hourStats);
				csvView.submit(weekStats);
				csvView.submit(minuteStats);

				lineByLineParser.filter(listOfLogFiles);

				System.out.println(LINE_SEPARATOR + logSnapshot.toString());

				if (statsProvider != null) {
					printStats(dayStats, weekStats, hourStats, minuteStats);
					Map<String, TimeStats<LogEntry>> filtered = getAlerts(dayStats, statsProvider);
					writeCharts(config, dayStats, weekStats, outputDir, filtered);
				}

				csvView.write();
				ChartView<LogEntry> chartView = new ChartView<LogEntry>(logSnapshot);
				chartView.write(outputDir, "log-analysis");
			} else {
				System.out.println("No log files found!");
			}
		}

	}

	private static void writeCharts(final Config config,
			final DayStats<LogEntry> dayStats,
			final WeekDayStats<LogEntry> weekStats,
			final String outputDir,
			final Map<String, TimeStats<LogEntry>> filtered) {

		ChartParams chartParams = config.getChartParams();
		if (chartParams != null) {
			GoogleChartView gcv = new GoogleChartView(config.getChartParams(), outputDir);
			Map<String, String> urls = gcv.createChartUrls(dayStats, filtered, dayStats.formatToShortDate);
			gcv.write(urls, "png", "daily_");
			urls = gcv.createChartUrls(weekStats, weekStats.formatToDayOfWeek);
			gcv.write(urls, "png", "weekly_");
			urls = gcv.createChartUrl("aggregate", weekStats.getAggregatedStats(), weekStats.formatToDayOfWeek);
			gcv.write(urls, "png", "weekly_");
		}
	}

	private static void printStats(final AbstractStats<?>... stats) {
		for (AbstractStats<?> stat : stats) {
			System.out.println(stat.toString());
		}
	}

	private static Map<String, TimeStats<LogEntry>> getAlerts(DayStats<LogEntry> dayStats, StatsProvider statsProvider) {
		Map<String, TimeStats<LogEntry>> filtered = new HashMap<String, TimeStats<LogEntry>>();
		Predicate<PredicateArguments> predicate = statsProvider.getPredicate();
		if (predicate != null) {
			System.out.println(String.format("%sFiltering by %s %s ",
					LINE_SEPARATOR,
					statsProvider.getPredicateValue(),
					statsProvider.getPredicateType().toString()));
			filtered = dayStats.filter(predicate);
			System.out.println(dayStats.toString(filtered));
		}
		return filtered;
	}

	private static Config getConfig(final CommandLineArguments cla) {
		ObjectMapper mapper = new ObjectMapper();
		Config config = null;
		try {
			Map<String, Config> configs = mapper.readValue(new File(cla.configFile), new TypeReference<Map<String, Config>>() {
			});
			config = configs.get(cla.logName);

			System.out.println(String.format("Loaded '%s' configuration", config.getFriendlyName()));
			// TODO fix exception handling
		} catch (JsonParseException jpe) {
			jpe.printStackTrace();
		} catch (JsonMappingException jme) {
			jme.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		if (config == null) {
			System.out.println(String.format("Unable to find profile '%s' on JSON configuration file '%s'", cla.logName, cla.configFile));
		}
		return config;
	}
}
