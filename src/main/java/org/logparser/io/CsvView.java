package org.logparser.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.logparser.IStatsView;
import org.logparser.LogSnapshot;

import com.google.common.base.Preconditions;

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

	public CsvView(final LogSnapshot<E> logSnapshot, final Map<String, IStatsView<E>> keyStats) {
		Preconditions.checkNotNull(logSnapshot);
		Preconditions.checkNotNull(keyStats);
		this.logSnapshot = logSnapshot;
		this.keyStats = keyStats;
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
			int totalEntries = logSnapshot.getTotalEntries();
			int filteredEntries = logSnapshot.getFilteredEntries().size();
			for (Entry<String, IStatsView<E>> entries : statsEntries) {
				out.write(String.format("%s, %s, %s, %s", filename, entries.getKey(), totalEntries, entries.getValue().toCsvString()));
				out.write("\n, , TIME, # TOTAL, AS % OF FILTERED, AS % OF TOTAL\n");
				writeSummary(entries.getValue().getTimeBreakdown(), filteredEntries, totalEntries, out);
			}
			if (!logSnapshot.getSummary().isEmpty()) {
				out.write("\nFILTERED, TOTAL #, AS % OF FILTERED, AS % OF TOTAL\n");
				writeSummary(logSnapshot.getSummary(), filteredEntries, totalEntries, out);
			}

			if (!logSnapshot.getTimeBreakdown().isEmpty()) {
				out.write("\nTIME, TOTAL #, AS % OF FILTERED, AS % OF TOTAL\n");
				writeSummary(logSnapshot.getTimeBreakdown(), filteredEntries, totalEntries, out);
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

	private static <K> void writeSummary(final Map<K, Integer> summary, final int filteredEntries, final int totalEntries, Writer out) throws IOException {
		int value = 0;
		double percentOfFiltered = 0.0;
		double percentOfTotal = 0.0;
		DecimalFormat df = new DecimalFormat("####.##%");
		for (Entry<K, Integer> entries : summary.entrySet()) {
			value = entries.getValue() > 0 ? entries.getValue() : 0;
			percentOfFiltered = value > 0 ? value / (double) filteredEntries : 0D;
			percentOfTotal = value > 0 ? value / (double) totalEntries : 0D;
			out.write(String.format(", , %s, %s, %s, %s\n", entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
		}
	}
}
