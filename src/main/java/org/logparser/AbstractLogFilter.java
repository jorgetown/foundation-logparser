package org.logparser;

import java.util.List;

/**
 * Skeletal implementation of {@link ILogFilter} with basic, common functionality.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
public abstract class AbstractLogFilter<E> implements ILogFilter<E> {

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

	public abstract LogSnapshot<E> filter(String filepath);
}
