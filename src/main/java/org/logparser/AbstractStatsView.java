package org.logparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeletal implementation containing a summary and description of a collection
 * of log entries.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
public abstract class AbstractStatsView<E> implements IStatsView<E> {
	private static String NEWLINE = System.getProperty("line.separator");
	private final List<E> entries;
	protected E max;
	protected E min;
	protected double mean;
	protected double std;

	public AbstractStatsView() {
		this.entries = new ArrayList<E>();
	}

	public void add(final E newEntry) {
		entries.add(newEntry);
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
}
