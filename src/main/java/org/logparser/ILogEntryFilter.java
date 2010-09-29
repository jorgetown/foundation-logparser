package org.logparser;

/**
 * Specifies the protocol required of log entry filters.
 * 
 * Typically, {@link ILogEntryFilter} implementations parse log entry strings
 * and return corresponding log entry objects.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entry.
 */
public interface ILogEntryFilter<E> {
	/**
	 * Parse the given {@code text} and return a corresponding log entry object.
	 * 
	 * @param text the {@code text} to parse.
	 * @return a parsed log entry object, or null if the filter is unable to parse {@code text}.
	 */
	public E parse(String text);
}
