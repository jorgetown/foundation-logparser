package org.logparser;

import java.util.Map;

/**
 * Protocol for {@link IMessageFilter} implementations that provide a summary of
 * the log.
 * 
 * @author jorge.decastro
 * 
 */
public interface ILogSummaryFilter {
	public Map<String, Integer> getSummary();

	public Map<String, Integer> getTimeBreakdown();
}