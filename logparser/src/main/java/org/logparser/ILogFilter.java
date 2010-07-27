package org.logparser;

/**
 * Specifies the protocol required of log filter implementations.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
public interface ILogFilter<E extends ITimestampedEntry> {

	public LogSnapshot<E> filter(String filepath);
}
