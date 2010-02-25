package org.logparser;

/**
 * Specifies the protocol required by {@link IStatsView} implementations to
 * create statistic snapshots.
 * 
 * @author jorge.decastro
 * 
 */
public interface IStatsCapable {
	public long getElapsedTime();
}
