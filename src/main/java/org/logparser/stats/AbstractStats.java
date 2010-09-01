package org.logparser.stats;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Skeletal implementation and common functionality for statistic summaries.
 * 
 * @author jorge.decastro
 * 
 */
public abstract class AbstractStats<E> implements Serializable {
	private static final long serialVersionUID = -5699879056725405682L;

	public void addAll(final List<E> logEntries) {
		Preconditions.checkNotNull(logEntries);
		for (E entry : logEntries) {
			add(entry);
		}
	}

	public abstract void add(final E entry);
}
