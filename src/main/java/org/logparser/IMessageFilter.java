package org.logparser;

/**
 * Specifies the protocol required of log entry parsers.
 * 
 * Typically, an {@link IMessageFilter} implementation will parse a log message
 * and return a corresponding message E if successful.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entry.
 */
public interface IMessageFilter<E> {
	/**
	 * Parse the given {@code text} and return a populated message E.
	 * 
	 * @param text the {@code text} to parse.
	 * @return a parsed message of type E, or null if the filter is unable to parse
	 *         {@code text}.
	 */
	public E parse(String text);
}
