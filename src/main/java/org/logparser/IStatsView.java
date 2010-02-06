package org.logparser;

import java.util.List;

/**
 * Specifies the protocol used to summarize and describe a collection of log
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

	// TODO simply return the value? multiple entries might share same max value
	public E getMax();

	// TODO simply return the value? multiple entries might share same min value
	public E getMin();

	public double getMean();

	public double getDeviation();

	public String toCsvString();
}
