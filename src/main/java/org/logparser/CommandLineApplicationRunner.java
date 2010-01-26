package org.logparser;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.logparser.filter.IMessageFilter;
import org.logparser.filter.MessageFilter;
import org.logparser.filter.MessageSamplingByFrequency;
import org.logparser.filter.MessageSamplingByTime;
import org.logparser.io.AbstractLogParser;
import org.logparser.io.ChartWriter;
import org.logparser.io.ILogParser;
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

		// sample message patterns we're interested in within the given time interval
		// useful if log files are huge
		IMessageFilter<Message> sampler = new MessageSamplingByTime(entryFilter, 5000); // every 5secs
		
		// inject the filter onto the log parser
		ILogParser<Message> logParser = new AbstractLogParser<Message>(entryFilter);
		logParser.parse(pathfile);
		
		System.out.println(String.format("Total: %s, Filtered: %s", logParser.getTotalEntries(), logParser.getParsedEntries().size()));
		
		// inject the sampler onto the log parser
		logParser = new AbstractLogParser<Message>(sampler);
		logParser.parse(pathfile);
		
		System.out.println(String.format("Total: %s, Sampled: %s", logParser.getTotalEntries(), logParser.getParsedEntries().size()));

		// API allows looking for the same type of messages across many files

		// inject the parser onto the 'organiser'
		LogOrganiser<Message> logOrganiser = new LogOrganiser<Message>(logParser, MessageStatsView.class);
		
		// pass the class field used to group by
		Map<String, IStatsView<Message>> organisedEntries = logOrganiser.groupBy("url");
		
		for (Entry<String, IStatsView<Message>> entry : organisedEntries.entrySet()) {
			System.out.println(String.format("key=%s, stats=%s", entry.getKey(), entry.getValue()));
		}

		ChartWriter chartWriter = new ChartWriter(logParser, organisedEntries);
		chartWriter.write(path, file);
	}
}