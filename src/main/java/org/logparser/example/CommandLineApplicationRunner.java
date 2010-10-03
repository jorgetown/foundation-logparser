package org.logparser.example;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
 * 		java -Xms256m -Xmx256m -jar target/log-parser-1.7.X-jar-with-dependencies.jar -configfile config.json -logname example
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
			FilterProvider filterProvider = config.getFilterProvider();
			filterProvider.applyCommandLineOverrides(cla);
			LogEntryFilter filter = filterProvider.build();
			
			// for large log files sampling is preferred/required
			ILogEntryFilter<LogEntry> sampler = config.getSamplerProvider() != null ? config.getSamplerProvider().build(filter) : filter;

			// sampler returns filter if unable to decorate
			LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(sampler);

			// stuff below is a big mess...
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

			DecimalFormat df = new DecimalFormat("#.#");

			String filepath;
			String filename = null;
			int totalEntries = 0;
			int filteredEntries = 0;
			int previousFiltered = 0;

			LogFilesProvider logFilesProvider = config.getLogFilesProvider();
			logFilesProvider.applyCommandLineOverrides(cla);
			LogFiles logfiles = logFilesProvider.build();

			File[] files = logfiles.list();
			for (File f : files) {
				filepath = f.getAbsolutePath();
				filename = f.getName();

				long start = System.nanoTime();
				lineByLineParser.filter(filepath);
				long end = (System.nanoTime() - start) / 1000000;
				totalEntries = lineByLineParser.size();
				filteredEntries = logSnapshot.getFilteredEntries().size() - previousFiltered;
				System.out.println(String.format("\n%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s\n",
						filename,
						end,
						df.format(totalEntries / (double) end),
						totalEntries,
						filteredEntries));
				previousFiltered = filteredEntries;
			}

			System.out.println(LINE_SEPARATOR + logSnapshot.toString() + LINE_SEPARATOR);

			String outputDir = logfiles.getOutputDir();

			if (statsProvider != null) {
				System.out.println(LINE_SEPARATOR + dayStats.toString() + LINE_SEPARATOR);
				System.out.println(LINE_SEPARATOR + weekStats.toString() + LINE_SEPARATOR);
				System.out.println(LINE_SEPARATOR + hourStats.toString() + LINE_SEPARATOR);
				System.out.println(LINE_SEPARATOR + minuteStats.toString() + LINE_SEPARATOR);
				Map<String, TimeStats<LogEntry>> filtered = new HashMap<String, TimeStats<LogEntry>>();
				Predicate<PredicateArguments> predicate = statsProvider.getPredicate();
				if (predicate != null) {
					System.out.println(String.format("Filtering by %s %s %s",
							statsProvider.getPredicateValue(),
							statsProvider.getPredicateType().toString(),
							LINE_SEPARATOR));
					filtered = dayStats.filter(predicate);
					System.out.println(dayStats.toString(filtered));
				}
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

			if (StringUtils.isNotBlank(filename)) {
				CsvView csvView = new CsvView();
				if (statsProvider != null) {
					csvView.write(outputDir, filename, logSnapshot, dayStats, weekStats, hourStats, minuteStats);
				} else {
					csvView.write(outputDir, filename, logSnapshot);
				}

				ChartView<LogEntry> chartView = new ChartView<LogEntry>(logSnapshot);
				chartView.write(outputDir, filename);
			}
		}
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
		return config;
	}
}
