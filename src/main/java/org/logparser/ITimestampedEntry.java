package org.logparser;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * Specifies the protocol required of generic log entries.
 * 
 * @author jorge.decastro
 * 
 */
@JsonPropertyOrder( { "timestamp", "action", "duration" })
public interface ITimestampedEntry {
	/**
	 * The timestamp of the log entry.
	 * 
	 * @return the timestamp as a {@code long} value
	 */
	public long getTimestamp();

	/**
	 * The log action being measured.
	 * 
	 * @return a {@link String} representing the action being timed.
	 */
	public String getAction();

	/**
	 * The time taken by the log action being measured.
	 * 
	 * @return a {@code double} representing the duration of the action.
	 */
	public double getDuration();
}
