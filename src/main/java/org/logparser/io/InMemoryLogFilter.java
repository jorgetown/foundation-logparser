package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.jcip.annotations.Immutable;

import org.logparser.AbstractLogFilter;
import org.logparser.FilterConfig;
import org.logparser.ILogFilter;
import org.logparser.IMessageFilter;
import org.logparser.ITimestampedEntry;
import org.logparser.LogSnapshot;

import com.google.common.base.Preconditions;

/**
 * Implementation of {@link ILogFilter} that processes a log file's entries in
 * memory. It is, thus, prone to {@link OutOfMemoryError}s when dealing with
 * large files.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held by this {@link ILogFilter}
 *            implementation.
 */
@Immutable
public class InMemoryLogFilter<E extends ITimestampedEntry> extends AbstractLogFilter<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private final List<E> filteredEntries;
	private final List<String> readEntries;
	private final SortedMap<String, Integer> summary;
	private final SortedMap<String, Integer> timeBreakdown;
	private final int groupBy;
	private final Calendar calendar;

	public InMemoryLogFilter(final FilterConfig filterConfig, final IMessageFilter<E>... messageFilter) {
		this(filterConfig, Arrays.asList(messageFilter));
	}

	public InMemoryLogFilter(final FilterConfig filterConfig, final List<IMessageFilter<E>> messageFilters) {
		Preconditions.checkNotNull(messageFilters);
		for (IMessageFilter<E> filter : messageFilters) {
			Preconditions.checkNotNull(filter);
		}
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.filteredEntries = new ArrayList<E>();
		this.readEntries = new ArrayList<String>();
		this.summary = new TreeMap<String, Integer>();
		this.timeBreakdown = new TreeMap<String, Integer>();
		this.groupBy = filterConfig.groupByToCalendar();
		this.calendar = Calendar.getInstance();
	}

	public LogSnapshot<E> filter(final String filepath) {
		Preconditions.checkNotNull(filepath);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filepath));
			String str;
			while ((str = in.readLine()) != null) {
				readEntries.add(str);
			}
			in.close();
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Failed to read file %s", filepath), e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format("Failed to properly close file %s", filepath), ioe);
			}
		}
		E filteredEntry;
		for (String entryString : readEntries) {
			filteredEntry = applyFilters(entryString, messageFilters);
			if (filteredEntry != null) {
				filteredEntries.add(filteredEntry);
			}
		}
		return new LogSnapshot<E>(filteredEntries, readEntries.size(), summary, timeBreakdown);
	}

	protected void updateLogSummary(final E entry) {
		String key = entry.getAction();
		if (summary.containsKey(key)) {
			Integer value = summary.get(key);
			value++;
			summary.put(key, value);
		} else {
			summary.put(key, 1);
		}
	}

	protected void updateLogTimeBreakdown(final E entry) {
		calendar.setTimeInMillis(entry.getTimestamp());
		String key = "" + calendar.get(groupBy);
		if (timeBreakdown.containsKey(key)) {
			int value = timeBreakdown.get(key);
			value++;
			timeBreakdown.put(key, value);
		} else {
			timeBreakdown.put(key, 1);
		}
	}

	public void cleanup() {
		this.filteredEntries.clear();
		this.readEntries.clear();
		this.summary.clear();
		this.timeBreakdown.clear();
	}
}
