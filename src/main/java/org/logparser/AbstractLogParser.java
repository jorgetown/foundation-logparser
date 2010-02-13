package org.logparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeletal implementation of {@link ILogParser} with basic parser infrastructure.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
public abstract class AbstractLogParser<E> implements ILogParser<E> {

	// TODO address this quadratic time complexity
	protected E applyFilters(final String toParse, final List<IMessageFilter<E>> filters) {
		E entry = null;
		for (IMessageFilter<E> filter : filters) {
			entry = filter.parse(toParse);
			if (entry != null) {
				break;
			}
		}
		return entry;
	}

	public List<E> getParsedEntries() {
		return new ArrayList<E>();
	}

	public int getTotalEntries() {
		return 0;
	}

	public E getEarliestEntry() {
		return null;
	}

	public E getLatestEntry() {
		return null;
	}
}
