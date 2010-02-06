package org.logparser;

import java.util.List;

/**
 * Specifies the protocol of a view containing descriptive statistics of log
 * entries.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
public interface IStatsView<E> {

	public void add(E newEntry);

	public List<E> getEntries();

	public E getEarliestEntry();

	public E getLatestEntry();

	public E getMax();

	public E getMin();

	public double getMean();

	public double getDeviation();

	public String toCsvString();
}
