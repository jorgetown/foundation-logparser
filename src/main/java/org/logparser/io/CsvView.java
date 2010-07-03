package org.logparser.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.logparser.IStatsView;
import org.logparser.LogSnapshot;
import org.logparser.Preconditions;

/**
 * Creates a CSV file from a collection of log data.
 * 
 * @author jorge.decastro
 * 
 */
public class CsvView<E> {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private final Map<String, IStatsView<E>> keyStats;
	private final LogSnapshot<E> logSnapshot;
	private final Map<String, Integer> logSummary;
	private final SortedMap<String, Integer> timeBreakdown;
 
	public CsvView(final LogSnapshot<E> logSnapshot, final Map<String, IStatsView<E>> keyStats) {
		this(logSnapshot, keyStats, new HashMap<String, Integer>(), new TreeMap<String, Integer>());
	}
	
	public CsvView(final LogSnapshot<E> logSnapshot, final Map<String, IStatsView<E>> keyStats, final Map<String, Integer> logSummary, final SortedMap<String, Integer> timeBreakdown) {
		Preconditions.checkNotNull(logSnapshot);
		Preconditions.checkNotNull(keyStats);
		this.logSnapshot = logSnapshot;
		this.keyStats = keyStats;
		this.logSummary = logSummary;
		this.timeBreakdown = timeBreakdown;
	}

	public void write(String path, String filename) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(filename);

		String filepath = String.format("%s%s%s.csv", path, FILE_SEPARATOR, filename);
		Set<Entry<String, IStatsView<E>>> statsEntries = keyStats.entrySet();

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filepath));
			out.write("Logfile, Controller, # Entries, # Filtered Entries, Max, Min, Mean, STD\n");
			for (Entry<String, IStatsView<E>> entries : statsEntries) {
				out.write(String.format("%s, %s, %s, %s", 
						filename,
						entries.getKey(), 
						logSnapshot.getTotalEntries(), 
						entries.getValue().toCsvString()));
			}

			DecimalFormat df = new DecimalFormat( "###.##%" );
			double percentOfFiltered = 0.0;
			double percentOfTotal = 0.0;
			int value = 0;
			
			if (!logSummary.isEmpty()) {
				out.write("\nFILTERED, TOTAL #, AS % OF FILTERED, AS % OF TOTAL\n");
				for (Entry<String, Integer> entries : logSummary.entrySet()) {
					value = entries.getValue() > 0 ? entries.getValue() : 0;
					percentOfFiltered = value > 0 ? value / (double)logSnapshot.getFilteredEntries().size() : 0D;
					percentOfTotal = value > 0 ? value / (double)logSnapshot.getTotalEntries() : 0D;
					out.write(String.format("%s, %s, %s, %s\n", entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
				}
			}
			
			percentOfFiltered = 0.0;
			percentOfTotal = 0.0;
			value = 0;
			if (!timeBreakdown.isEmpty()) {
				out.write("\nTIME, TOTAL #, AS % OF FILTERED, AS % OF TOTAL\n");
				for (Entry<String, Integer> entries : timeBreakdown.entrySet()) {
					value = entries.getValue() > 0 ? entries.getValue() : 0;
					percentOfFiltered = value > 0 ? value / (double) logSnapshot.getFilteredEntries().size() : 0D;
					percentOfTotal = value > 0 ? value / (double) logSnapshot.getTotalEntries() : 0D;
					out.write(String.format("%s, %s, %s, %s\n", entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
				}
			}

			out.close();
		} catch (IOException ioe) {
			throw new RuntimeException(String.format("Failed to write %s", filepath), ioe);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException ioe) {
				throw new RuntimeException(String.format("Failed to properly close %s", filepath), ioe);
			}
		}
	}
}