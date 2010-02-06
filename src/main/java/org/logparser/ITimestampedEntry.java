package org.logparser;

import java.util.Date;

import org.logparser.filter.SamplingByTime;

/**
 * Specifies the protocol for log entries that can make use of timestamped
 * services such as {@link SamplingByTime}.
 * 
 * @author jorge.decastro
 * 
 */
public interface ITimestampedEntry {
	public Date getDate();
}
