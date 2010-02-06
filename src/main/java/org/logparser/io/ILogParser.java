package org.logparser.io;

import java.util.List;

/**
 * Specifies the protocol required of log parser implementations.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
public interface ILogParser<E> {

	public List<E> parse(String filename);

	public List<E> getParsedEntries();

	public int getTotalEntries();

	public E getEarliestEntry();

	public E getLatestEntry();
}
