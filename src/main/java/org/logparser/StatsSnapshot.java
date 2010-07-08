package org.logparser;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
 
/**
 * Provides a summary and descriptive statistics for a collection of
 * {@link ITimestampedEntry}s from a log.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@JsonPropertyOrder({ "mean", "deviation", "maxima", "minima", "entries" })
public class StatsSnapshot<E extends ITimestampedEntry> implements IStatsView<E> {
	private static String NEWLINE = System.getProperty("line.separator");
	private final List<E> entries;
	private final ObjectMapper jsonMapper;
	private E maxima;
	private E minima;
	private double mean;
	private double std;
	private DescriptiveStats ds = new DescriptiveStats();
 
	public StatsSnapshot() {
		entries = new ArrayList<E>();
		jsonMapper = new ObjectMapper();
	}
 
	public void add(final E newEntry) {
		entries.add(newEntry);
		if (maxima == null || (newEntry.getDuration() > maxima.getDuration())) {
			maxima = newEntry;
		}
		if (minima == null || (newEntry.getDuration() < minima.getDuration())) {
			minima = newEntry;
		}
		ds.calculate(Double.valueOf(newEntry.getDuration()), ds.getMean(), ds.getVariance(), ds.getObservations());
		this.mean = ds.getMean();
		this.std = ds.getStd();
	}
 
	public List<E> getEntries() {
		return entries;
	}
 
	@JsonIgnore
	public E getEarliestEntry() {
		if (!entries.isEmpty()) {
			return entries.get(0);
		}
		return null;
	}
 
	@JsonIgnore
	public E getLatestEntry() {
		if (!entries.isEmpty()) {
			return entries.get(entries.size() - 1);
		}
		return null;
	}
 
	public E getMaxima() {
		return maxima;
	}
 
	public E getMinima() {
		return minima;
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
		return String.format("%s, \"%s\", \"%s\", %s, %s%s", entries.size(), maxima, minima, mean, std, NEWLINE);
	}
 
	@Override
	public String toString() {
		return String.format("\nMAX: %s\nMIN: %s\nMEAN: %s\nSTD: %s\nEARLIEST: %s\nLATEST: %s\n",
						maxima, minima, mean, std, getEarliestEntry(), getLatestEntry());
	}
	
	public String toJsonString() throws JsonGenerationException, JsonMappingException, IOException {
		return jsonMapper.writeValueAsString(this);
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
