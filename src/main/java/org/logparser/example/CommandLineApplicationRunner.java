package org.logparser.example;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.logparser.AnalyzeArguments;
import org.logparser.IStatsView;
import org.logparser.LogOrganiser;
import org.logparser.LogSnapshot;
import org.logparser.SamplingByFrequency;
import org.logparser.io.ChartView;
import org.logparser.io.CsvView;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;
import org.logparser.time.Instant;
import org.logparser.time.SimpleTimeInterval;

/**
 * Responsible for running the log parser via the command line.
 * 
 * <pre>
 *  java -Xmx128m -jar log-parser-XX.jar /logs/ EXAMPLE_log_(.*)-15.log .*.do
 * </pre>
 * 
 * @author jorge.decastro
 */
public class CommandLineApplicationRunner {
	/**
	 * Run with no args to see help information.
	 * 
	 * @param args Run with no args to see help information.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		AnalyzeArguments aa = new AnalyzeArguments(args);
		String[] dirs = aa.getPaths();
		File[] files = aa.getFiles();
		Instant after = aa.getAfter();
		Instant before = aa.getBefore();
		ITimeInterval timeInterval = new InfiniteTimeInterval();

		if (after != null && before != null) {
			timeInterval = new SimpleTimeInterval(after, before);
		}
		// filter all controllers
		MessageFilter filter = new MessageFilter(timeInterval, aa.getPattern().pattern());
		// for large log files sampling is required 
		SamplingByFrequency<Message> sampler = new SamplingByFrequency<Message>(filter, 50);
		LineByLineLogFilter<Message> rlp = new LineByLineLogFilter<Message>(filter);
		LogOrganiser<Message> logOrganiser;
		Map<String, IStatsView<Message>> organisedEntries;
		ChartView<Message> chartView;
		CsvView<Message> csvView;
		String filepath;
		String path;
		String filename;
		for (File f : files) {
			filepath = f.getAbsolutePath();
			filename = f.getName();
			path = f.getParent();

			long start = System.nanoTime();
			LogSnapshot<Message> ls = rlp.filter(filepath);
			long end = (System.nanoTime() - start) / 1000000;
			DecimalFormat df = new DecimalFormat( "###.#" );
			System.out.println(String.format("\n%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s\n",
									filename, end, df.format(ls.getTotalEntries() / (double)end), ls.getTotalEntries(), ls.getFilteredEntries().size()));
			// inject the parser onto the 'organiser'
			logOrganiser = new LogOrganiser<Message>();		 
			// pass the class field used to group by
			organisedEntries = logOrganiser.groupBy(ls);
			chartView = new ChartView(ls);
			chartView.write(path, filename);
			csvView = new CsvView<Message>(ls, organisedEntries, filter.getSummary());
			csvView.write(path, filename);
			df = new DecimalFormat( "###.##%" );
			double percentOfFiltered = 0.0;
			double percentOfTotal = 0.0;
			int value = 0;
			System.out.println("URL,\t# Count,\t% of Filtered,\t% of Total");
			for (Entry<String, Integer> entries : filter.getSummary().entrySet()) {
				value = entries.getValue() > 0 ? entries.getValue() : 0;
				percentOfFiltered = value > 0 ? value / (double)ls.getFilteredEntries().size() : 0D;
				percentOfTotal = value > 0 ? value / (double)ls.getTotalEntries() : 0D;
				System.out.println(String.format("%s, %s, %s, %s", entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
			}
			rlp.cleanup();
			filter.reset();
		}
		
	}
}