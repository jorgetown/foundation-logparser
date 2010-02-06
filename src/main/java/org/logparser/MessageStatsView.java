package org.logparser;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * A view that provides descriptive statistics for a parsed collection of
 * log {@link Message}s.
 * 
 * @author jorge.decastro
 * 
 */
public class MessageStatsView extends AbstractStatsView<Message> {
	// TODO refactor to inject stats
	private DescriptiveStats ds = new DescriptiveStats();
	
	public MessageStatsView() {
		super();
	}

	@Override
	public void add(final Message newEntry) {
		super.add(newEntry);
		if (max == null
				|| Long.valueOf(newEntry.getMilliseconds()) > Long.valueOf(max.getMilliseconds())) {
			max = newEntry;
		}
		if (min == null
				|| Long.valueOf(newEntry.getMilliseconds()) < Long.valueOf(min.getMilliseconds())) {
			min = newEntry;
		}
		ds.calculate(Double.valueOf(newEntry.getMilliseconds()), ds.getMean(), ds.getVariance(), ds.getObservations());
		this.mean = ds.getMean();
		this.std = ds.getStd();
	}

	@Override
	public String toString() {
		return String.format("\nMAX: %s\nMIN: %s\nMEAN: %s\nSTD: %s\nEARLIEST: %s\nLATEST: %s\n",
						this.max, this.min, this.mean, this.std, this.getEarliestEntry(), this.getLatestEntry());
	}
	
	public static class DescriptiveStats implements Serializable {
		private static final long serialVersionUID = 2268207471613686207L;
		private static final String PRECISION_PATTERN = "##.##";
		private int observations; // observations counter
		private double mean; // arithmetic mean
		private double variance; // partial variance calculation
		private final DecimalFormat decimalFormatter;
		private static final String NEWLINE = System.getProperty("line.separator");

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
