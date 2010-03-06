package org.logparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Represents a log file snapshot, containing filtered log {@code E}ntries .
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
public class LogSnapshot<E> {
	private final List<E> filteredEntries;
	private final int totalEntries;

	public LogSnapshot(final List<E> filteredEntries, final int totalEntries) {
		Preconditions.checkNotNull(filteredEntries);
		this.filteredEntries = new ArrayList<E>(filteredEntries);
		this.totalEntries = totalEntries;
	}

	public List<E> getFilteredEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	public int getTotalEntries() {
		return totalEntries;
	}
}
