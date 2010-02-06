package org.logparser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.logparser.filter.IMessageFilter;
import org.logparser.filter.MessageFilter;
import org.logparser.filter.SamplingByTime;
import org.logparser.io.BackgroundLogParser;
import org.logparser.io.ChartWriter;
import org.logparser.io.InMemoryLogParser;
import org.logparser.io.LineByLineLogParser;
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
		final String file = analyzeArguments.getFile();
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
		BackgroundLogParser<Message> blp = new BackgroundLogParser<Message>(filters);
		long start = System.nanoTime();
		// API allows looking for the same type of messages across many files
		blp.parse(pathfile);
		long end = (System.nanoTime() - start) / 1000000;
		System.out.println(String.format("BackgroundLogParser - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s",
								end, blp.getTotalEntries() / end, blp.getTotalEntries(), blp.getParsedEntries().size()));
		blp.dispose();
		blp = null;
		
		InMemoryLogParser<Message> imlp = new InMemoryLogParser<Message>(filters);
		start = System.nanoTime();
		imlp.parse(pathfile);
		end = (System.nanoTime() - start) / 1000000;
		System.out.println(String.format("InMemoryLogParser - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s",
								end, imlp.getTotalEntries() / end, imlp.getTotalEntries(), imlp.getParsedEntries().size()));
		imlp.dispose();
		imlp = null;

		LineByLineLogParser<Message> rlp = new LineByLineLogParser<Message>(filters);
		start = System.nanoTime();
		rlp.parse(pathfile);
		end = (System.nanoTime() - start) / 1000000;
		System.out.println(String.format("LineByLineLogParser - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s",
								end, rlp.getTotalEntries() / end, rlp.getTotalEntries(), rlp.getParsedEntries().size()));
		// rlp.dispose();
		// rlp = null;
		
		// inject the parser onto the 'organiser'
		LogOrganiser<Message> logOrganiser = new LogOrganiser<Message>(rlp, MessageStatsView.class);

		// pass the class field used to group by
		Map<String, IStatsView<Message>> organisedEntries = logOrganiser.groupBy("url");

		for (Entry<String, IStatsView<Message>> entry : organisedEntries.entrySet()) {
			System.out.println(String.format("key=%s, stats=%s", entry.getKey(), entry.getValue()));
		}

		ChartWriter chartWriter = new ChartWriter(rlp, organisedEntries);
		chartWriter.write(path, file);
	}
}