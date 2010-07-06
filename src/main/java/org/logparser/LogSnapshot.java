package org.logparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import net.jcip.annotations.Immutable;

import com.google.common.base.Preconditions;

/**
 * Represents a log file snapshot, containing filtered log {@code E}ntries and log summaries.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
public class LogSnapshot<E> {
	private final List<E> filteredEntries;
	private final int totalEntries;
	private final SortedMap<String, Integer> summary;
	private final SortedMap<String, Integer> timeBreakdown;

	public LogSnapshot(final List<E> filteredEntries, final int totalEntries, final SortedMap<String, Integer> summary, final SortedMap<String, Integer> timeBreakdown) {
		Preconditions.checkNotNull(filteredEntries);
		Preconditions.checkNotNull(summary);
		Preconditions.checkNotNull(timeBreakdown);
		this.filteredEntries = new ArrayList<E>(filteredEntries);
		this.totalEntries = totalEntries;
		this.summary = summary;
		this.timeBreakdown = timeBreakdown;
	}

	public List<E> getFilteredEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	public SortedMap<String, Integer> getSummary() {
		return Collections.unmodifiableSortedMap(summary);
	}

	public SortedMap<String, Integer> getTimeBreakdown() {
		return Collections.unmodifiableSortedMap(timeBreakdown);
	}
}
