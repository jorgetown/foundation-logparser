package org.logparser;

/**
 * Specifies the protocol required of generic log entries.
 * 
 * @author jorge.decastro
 * 
 */
public interface ITimestampedEntry {
	/**
	 * The timestamp of the log entry.
	 * 
	 * @return {@code long} representing the timestamp of the log entry.
	 */
	public long getTimestamp();

	/**
	 * The log action being measured.
	 * 
	 * @return {@link String} representing the action being timed.
	 */
	public String getAction();

	/**
	 * The time taken by the log action being measured.
	 * 
	 * @return {@code double} representing the duration of the action.
	 */
	public double getDuration();
}
