package org.logparser.example;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.logparser.ILogEntryFilter;
import org.logparser.LogEntry;
import org.logparser.LogEntryFilter;
import org.logparser.LogSnapshot;
import org.logparser.config.ChartParams;
import org.logparser.config.Config;
import org.logparser.config.Config.SamplerConfig;
import org.logparser.config.StatsParams;
import org.logparser.io.ChartView;
import org.logparser.io.CommandLineArguments;
import org.logparser.io.CsvView;
import org.logparser.io.GoogleChartView;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.io.LogFiles;
import org.logparser.sampling.SamplingByFrequency;
import org.logparser.sampling.SamplingByTime;
import org.logparser.stats.DayStats;
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
	private static final Logger LOGGER = Logger.getLogger(CommandLineApplicationRunner.class.getName());

	public static void main(String[] args) {
		CommandLineArguments cla = new CommandLineArguments();
		JCommander jc = new JCommander(cla, args);
		if (cla.help) {
			jc.usage();
			return;
		}

		Config config = getConfig(cla);

		if (config != null) {
			LogFiles logfiles = config.getLogFiles();
			File[] files = logfiles.list();

			LogEntryFilter filter = new LogEntryFilter(config.getFilterParams());
			// for large log files sampling is preferred/required
			ILogEntryFilter<LogEntry> sampler = getSamplerIfAvailable(config, filter);

			LogSnapshot<LogEntry> logSnapshot = new LogSnapshot<LogEntry>(config);

			LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(sampler != null ? sampler : filter);
			lineByLineParser.attach(logSnapshot);

			StatsParams statsParams = config.getStatsParams();
			DayStats<LogEntry> dayStats = null;
			WeekDayStats<LogEntry> weekStats = null;
			if (statsParams != null) {
				dayStats = new DayStats<LogEntry>();
				weekStats = new WeekDayStats<LogEntry>();
				lineByLineParser.attach(dayStats);
				lineByLineParser.attach(weekStats);
			}

			DecimalFormat df = new DecimalFormat("#.##");

			String filepath;
			String path = null;
			String filename = null;
			int totalEntries = 0;
			int filteredEntries = 0;
			int previousFiltered = 0;

			for (File f : files) {
				filepath = f.getAbsolutePath();
				filename = f.getName();
				path = f.getParent();

				long start = System.nanoTime();
				lineByLineParser.filter(filepath);
				long end = (System.nanoTime() - start) / 1000000;
				totalEntries = lineByLineParser.size();
				filteredEntries = logSnapshot.getFilteredEntries().size() - previousFiltered;
				LOGGER.info(String.format("\n%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s\n", filename, end, df.format(totalEntries / (double) end), totalEntries, filteredEntries));
				previousFiltered = filteredEntries;
			}

			LOGGER.info(LINE_SEPARATOR + logSnapshot.toString() + LINE_SEPARATOR);

			if (StringUtils.isNotBlank(path) && StringUtils.isNotBlank(filename)) {
				CsvView csvView = new CsvView();
				csvView.write(path, filename, logSnapshot, dayStats, weekStats);
				if (config.isFilteredEntriesStored()) {
					ChartView<LogEntry> chartView = new ChartView<LogEntry>(logSnapshot);
					chartView.write(path, filename);
				}
			}

			if (dayStats != null) {
				LOGGER.info(LINE_SEPARATOR + dayStats.toString() + LINE_SEPARATOR);
				LOGGER.info(LINE_SEPARATOR + weekStats.toString() + LINE_SEPARATOR);
				Map<String, TimeStats<LogEntry>> filtered = new HashMap<String, TimeStats<LogEntry>>();
				if (statsParams != null) {
					Predicate<PredicateArguments> predicate = statsParams.getPredicate();
					if (predicate != null) {
						LOGGER.info(String.format("Filtering by %s %s %s", statsParams.getPredicateValue(), statsParams.getPredicateType().toString(), LINE_SEPARATOR));
						filtered = dayStats.filter(predicate);
						LOGGER.info(dayStats.toString(filtered));
					}
				}
				ChartParams chartParams = config.getChartParams();
				if (chartParams != null) {
					GoogleChartView gcv = new GoogleChartView(config.getChartParams());
					Map<String, String> urls = gcv.createChartUrls(dayStats, filtered, dayStats.formatToShortDate);
					gcv.write(urls, "png", "daily_");
					urls = gcv.createChartUrls(weekStats, weekStats.formatToDayOfWeek);
					gcv.write(urls, "png", "weekly_");
					urls = gcv.createChartUrl("aggregate", weekStats.getAggregatedStats(), weekStats.formatToDayOfWeek);
					gcv.write(urls, "png", "weekly_");
				}
			}
		}
	}

	// TODO this should be responsibility of 'sampler params': return given filter decorated with a sampler if one is given, or return given filter
	private static ILogEntryFilter<LogEntry> getSamplerIfAvailable(final Config config, final LogEntryFilter filter) {
		ILogEntryFilter<LogEntry> sampler = null;
		if (config.getSampler() != null) {
			SamplerConfig samplerConfig = config.getSampler();
			switch (samplerConfig.sampleBy) {
			case TIME:
				sampler = new SamplingByTime<LogEntry>(filter, samplerConfig.value);
				break;
			case FREQUENCY:
				sampler = new SamplingByFrequency<LogEntry>(filter, samplerConfig.value);
				break;
			default:
				sampler = null;
			}
		}
		return sampler;
	}

	private static Config getConfig(final CommandLineArguments cla) {
		ObjectMapper mapper = new ObjectMapper();
		Config config = null;
		try {
			Map<String, Config> configs = mapper.readValue(new File(cla.configFile), new TypeReference<Map<String, Config>>() {});
			config = configs.get(cla.logName);

			config.validate();
			LOGGER.info(String.format("Loaded '%s' configuration", config.getFriendlyName()));
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
