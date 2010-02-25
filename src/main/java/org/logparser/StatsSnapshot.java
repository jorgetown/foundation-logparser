package org.logparser;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a summary and descriptive statistics for a collection of
 * {@link IStatsCapable} entries from a log.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
public class StatsSnapshot<E extends IStatsCapable> implements IStatsView<E> {
	private static String NEWLINE = System.getProperty("line.separator");
	private final List<E> entries;
	private E max;
	private E min;
	private double mean;
	private double std;
	private DescriptiveStats ds = new DescriptiveStats();

	public StatsSnapshot() {
		entries = new ArrayList<E>();
	}

	public void add(final E newEntry) {
		entries.add(newEntry);
		if (max == null || (newEntry.getElapsedTime() > max.getElapsedTime())) {
			max = newEntry;
		}
		if (min == null || (newEntry.getElapsedTime() < min.getElapsedTime())) {
			min = newEntry;
		}
		ds.calculate(Double.valueOf(newEntry.getElapsedTime()), ds.getMean(), ds.getVariance(), ds.getObservations());
		this.mean = ds.getMean();
		this.std = ds.getStd();
	}

	public List<E> getEntries() {
		return entries;
	}

	public E getEarliestEntry() {
		if (!entries.isEmpty()) {
			return entries.get(0);
		}
		return null;
	}

	public E getLatestEntry() {
		if (!entries.isEmpty()) {
			return entries.get(entries.size() - 1);
		}
		return null;
	}

	public E getMax() {
		return max;
	}

	public E getMin() {
		return min;
	}

	public double getDeviation() {
		return std;
	}

	public double getMean() {
		return mean;
	}

	public String toCsvString() {
		// TODO CSV header?
		// TODO loop through list of entries too?
		// TODO require toCsvString() interface for log message implementations?
		return String.format("\"%s\", \"%s\", \"%s\", \"%s\"%s", max, min, mean, std, NEWLINE);
	}

	@Override
	public String toString() {
		return String.format("\nMAX: %s\nMIN: %s\nMEAN: %s\nSTD: %s\nEARLIEST: %s\nLATEST: %s\n",
						max, min, mean, std, getEarliestEntry(), getLatestEntry());
	}

	// TODO refactor; move out & inject here
	public static class DescriptiveStats implements Serializable {
		private static final long serialVersionUID = 2268207471613686207L;
		private static final String PRECISION_PATTERN = "##.##";
		private int observations; // observations counter
		private double mean; // arithmetic mean
		private double variance; // partial variance calculation
		private final DecimalFormat decimalFormatter;

		public DescriptiveStats() {
			observations = 0;
			mean = 0D;
			variance = 0D;
			decimalFormatter = new DecimalFormat(PRECISION_PATTERN);
		}

		public void calculate(final double datapoint, final double oldMean, final double partialVar, final int obs) {
			observations = obs + 1;
			double delta = datapoint - oldMean;
			mean = oldMean + (delta / observations);
			variance = partialVar + (delta * (datapoint - mean));
		}

		public Double getMean() {
			return Double.valueOf(decimalFormatter.format(mean));
		}

		public int getObservations() {
			return observations;
		}

		public Double getStd() {
			return Double.valueOf(decimalFormatter.format(Math.sqrt(variance)));
		}

		public Double getVariance() {
			int obs = observations > 1 ? observations - 1 : 1;
			return Double.valueOf(decimalFormatter.format(variance / obs));
		}
	}
}
