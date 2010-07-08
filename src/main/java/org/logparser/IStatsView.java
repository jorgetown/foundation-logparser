package org.logparser;

import java.util.List;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * Specifies the protocol used to summarize and describe a collection of log entries.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@JsonPropertyOrder({ "mean", "deviation", "maxima", "minima", "entries" })
public interface IStatsView<E> {

	public void add(E newEntry);

	public List<E> getEntries();

	// TODO simply return the value? multiple entries might share same max value.
	public E getMaxima();

	// TODO simply return the value? multiple entries might share same min value.
	public E getMinima();

	public double getMean();

	public double getDeviation();

	public String toCsvString();
}
