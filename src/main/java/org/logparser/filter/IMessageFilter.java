package org.logparser.filter;

/**
 * Specifies the protocol for parsers of log messages.
 * 
 * Typically, an {@link IMessageFilter} implementation will parse a log message
 * and return a corresponding message E if successful.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the parameterized parsed message type.
 */
public interface IMessageFilter<E> {
	/**
	 * Parse the given {@code text} and return a populated message E.
	 * 
	 * @param text the {@code text} to parse.
	 * @return a parsed message E, or null if the filter is unable to parse {@code text}.
	 */
	public E parse(String text);
}
