package org.logparser.example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.logparser.AnalyzeArguments;
import org.logparser.IMessageFilter;
import org.logparser.IStatsView;
import org.logparser.LogOrganiser;
import org.logparser.LogSnapshot;
import org.logparser.SamplingByTime;
import org.logparser.io.BackgroundLogFilter;
import org.logparser.io.CsvView;
import org.logparser.io.InMemoryLogFilter;
import org.logparser.io.LineByLineLogFilter;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;
import org.logparser.time.Instant;
import org.logparser.time.SimpleTimeInterval;

/**
 * Responsible for running the log parser via the command line.
 * 
 * <code>
 *  java -Xmx128m -jar log-parser-1.0.jar /access_log .*\\.do
 * </code>
 * 
 * 
 * @author jorge.decastro
 */
public class CommandLineApplicationRunner {
	/**
	 * Run with no args to see help information.
	 * 
	 * @param args Run with no args to see help information.
	 */
	public static void main(String[] args) {
		AnalyzeArguments analyzeArguments = new AnalyzeArguments(args);

		final String pathfile = analyzeArguments.getPathFile();
		final String path = analyzeArguments.getPath();
		final String filename = analyzeArguments.getFile();
		final Pattern pattern = analyzeArguments.getPattern();
		final Instant before = analyzeArguments.getBefore();
		final Instant after = analyzeArguments.getAfter();
		ITimeInterval timeInterval = new InfiniteTimeInterval();

		if (after != null && before != null) {
			timeInterval = new SimpleTimeInterval(after, before);
		}

		// filter message patterns we're interested in within the given time interval
		IMessageFilter<Message> entryFilter = new MessageFilter(timeInterval, pattern.pattern());

		IMessageFilter<Message> lockFilter = new MessageFilter(timeInterval, "lock.do");
		IMessageFilter<Message> statusCheckFilter = new MessageFilter(timeInterval, "statusCheck.do");
		IMessageFilter<Message> editFilter = new MessageFilter(timeInterval, "edit.do");
		IMessageFilter<Message> saveFilter = new MessageFilter(timeInterval, "save.do");

		@SuppressWarnings("unchecked")
		List<IMessageFilter<Message>> filters = Arrays.asList(lockFilter, statusCheckFilter, editFilter, saveFilter);

		// sample message patterns we're interested in within the given time interval
		// (useful if log files are huge)
		// samplers and filters are interchangeable
		IMessageFilter<Message> sampler = new SamplingByTime<Message>(entryFilter, 5000); // every 5secs

		// inject the filter onto a log parser
		BackgroundLogFilter<Message> blp = new BackgroundLogFilter<Message>(filters);
		long start = System.nanoTime();
		// API allows looking for the same type of messages across many files
		LogSnapshot<Message> ls = blp.filter(pathfile);
		long end = (System.nanoTime() - start) / 1000000;
		System.out.println(String.format("BackgroundLogFilter - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s",
								end, ls.getTotalEntries() / end, ls.getTotalEntries(), ls.getFilteredEntries().size()));
		blp.cleanup();
		blp = null;
		
		InMemoryLogFilter<Message> imlp = new InMemoryLogFilter<Message>(filters);
		start = System.nanoTime();
		ls = imlp.filter(pathfile);
		end = (System.nanoTime() - start) / 1000000;
		System.out.println(String.format("InMemoryLogFilter - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s",
								end, ls.getTotalEntries() / end, ls.getTotalEntries(), ls.getFilteredEntries().size()));
		imlp.cleanup();
		imlp = null;

		LineByLineLogFilter<Message> rlp = new LineByLineLogFilter<Message>(filters);
		start = System.nanoTime();
		ls = rlp.filter(pathfile);
		end = (System.nanoTime() - start) / 1000000;
		System.out.println(String.format("LineByLineLogFilter - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s",
								end, ls.getTotalEntries() / end, ls.getTotalEntries(), ls.getFilteredEntries().size()));
		// rlp.dispose();
		// rlp = null;
		
		// inject the parser onto the 'organiser'
		LogOrganiser<Message> logOrganiser = new LogOrganiser<Message>();

		// pass the class field used to group by
		Map<String, IStatsView<Message>> organisedEntries = logOrganiser.groupBy(ls, "url");

		for (Entry<String, IStatsView<Message>> entry : organisedEntries.entrySet()) {
			System.out.println(String.format("key=%s, stats=%s", entry.getKey(), entry.getValue()));
		}
		
		MessageChartView mcv = new MessageChartView(ls, organisedEntries);
		mcv.write(path, filename);
		
		CsvView<Message> csv = new CsvView<Message>(ls, organisedEntries);
		csv.write(path, filename);
	}
}