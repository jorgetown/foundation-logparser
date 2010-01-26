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
	 * Parse the given string text and return a populated message E.
	 * 
	 * @param text the string text to parse.
	 * @return a parsed message E, or NULL if the filter doesn't match.
	 */
	public E parse(String text);
}
