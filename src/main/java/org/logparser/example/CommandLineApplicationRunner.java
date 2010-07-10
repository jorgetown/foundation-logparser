package org.logparser.example;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.logparser.AnalyzeArguments;
import org.logparser.FilterConfig;
import org.logparser.IMessageFilter;
import org.logparser.IStatsView;
import org.logparser.LogEntry;
import org.logparser.LogEntryFilter;
import org.logparser.LogOrganiser;
import org.logparser.LogSnapshot;
import org.logparser.SamplingByFrequency;
import org.logparser.SamplingByTime;
import org.logparser.FilterConfig.Sampler;
import org.logparser.io.ChartView;
import org.logparser.io.CsvView;
import org.logparser.io.LineByLineLogFilter;

/**
 * Responsible for running the log parser via the command line.
 * 
 * <code>
 *  java -Xmx128m -jar log-parser-1.0.jar config.json
 * </code>
 * 
 * 24hrs worth of log file can take ~5mins to process.
 * 
 * @author jorge.decastro
 */
public class CommandLineApplicationRunner {
	/**
	 * Run with no args to see help information.
	 * 
	 * @param args
	 *            Run with no args to see help information.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		AnalyzeArguments aa = new AnalyzeArguments(args);

		FilterConfig filterConfig = aa.getFilterConfig();
		if (filterConfig != null) {
			File[] files = filterConfig.listLogFiles();

			LogEntryFilter filter = new LogEntryFilter(filterConfig);
			// for large log files sampling is preferred/required
			IMessageFilter<LogEntry> sampler = null;
			if (filterConfig.getSampler() != null) {
				Sampler samplerConfig = filterConfig.getSampler();
				switch (samplerConfig.sampleBy) {
				case TIME:
					sampler = new SamplingByTime<LogEntry>(filter, (Long) samplerConfig.getValue());
					break;
				case FREQUENCY:
					sampler = new SamplingByFrequency<LogEntry>(filter, (Integer) samplerConfig.getValue());
					break;
				default:
					sampler = null;
				}
			}

			LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(filterConfig, sampler != null ? sampler : filter);
			LogOrganiser<LogEntry> logOrganiser;
			Map<String, IStatsView<LogEntry>> organisedEntries;
			ChartView<LogEntry> chartView;
			CsvView<LogEntry> csvView;
			String filepath;
			String path;
			String filename;
			for (File f : files) {
				filepath = f.getAbsolutePath();
				filename = f.getName();
				path = f.getParent();

				long start = System.nanoTime();
				LogSnapshot<LogEntry> logSnapshot = lineByLineParser.filter(filepath);
				long end = (System.nanoTime() - start) / 1000000;
				DecimalFormat df = new DecimalFormat("####.##");
				int totalEntries = logSnapshot.getTotalEntries();
				int filteredEntries = logSnapshot.getFilteredEntries().size();
				System.out.println(String.format("\n%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s\n", filename, end, df.format(totalEntries / (double) end), totalEntries, filteredEntries));
				// inject the parser onto the 'organiser'
				logOrganiser = new LogOrganiser<LogEntry>();
				// pass the class field used to group by
				organisedEntries = logOrganiser.organize(logSnapshot);
				chartView = new ChartView(logSnapshot);
				chartView.write(path, filename);
				csvView = new CsvView<LogEntry>(logSnapshot, organisedEntries);
				csvView.write(path, filename);
				System.out.println("URL,\t# Count,\t% of Filtered,\t% of Total");
				printConsoleSummary(logSnapshot.getSummary(), filteredEntries, totalEntries);
				System.out.println("\n" + filterConfig.getGroupBy() + ",\t# Count,\t% of Filtered,\t% of Total\n");
				printConsoleSummary(logSnapshot.getTimeBreakdown(), filteredEntries, totalEntries);
				lineByLineParser.cleanup();
			}
		}
	}

	private static <K> void printConsoleSummary(final Map<K, Integer> summary, final int filteredEntries, final int totalEntries) {
		int value = 0;
		double percentOfFiltered = 0.0;
		double percentOfTotal = 0.0;
		DecimalFormat df = new DecimalFormat("####.##%");
		for (Entry<K, Integer> entries : summary.entrySet()) {
			value = entries.getValue() > 0 ? entries.getValue() : 0;
			percentOfFiltered = value > 0 ? value / (double) filteredEntries : 0D;
			percentOfTotal = value > 0 ? value / (double) totalEntries : 0D;
			System.out.println(String.format("%s,\t %s,\t %s,\t %s", entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
		}
	}
}
