package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.logparser.Config;
import org.logparser.ILogEntryFilter;
import org.logparser.ILogFilter;
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
public class InMemoryLogFilter<E extends ITimestampedEntry> implements ILogFilter<E> {
	private final List<ILogEntryFilter<E>> logEntryFilters;
	private final List<String> readEntries;
	private final LogSnapshot<E> logSnapshot;

	public InMemoryLogFilter(final Config config, final ILogEntryFilter<E>... messageFilter) {
		this(config, Arrays.asList(messageFilter));
	}

	public InMemoryLogFilter(final Config config, final List<ILogEntryFilter<E>> messageFilters) {
		Preconditions.checkNotNull(messageFilters);
		for (ILogEntryFilter<E> filter : messageFilters) {
			Preconditions.checkNotNull(filter);
		}
		this.logEntryFilters = Collections.unmodifiableList(messageFilters);
		this.readEntries = new ArrayList<String>();
		this.logSnapshot = new LogSnapshot<E>(config);
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
			filteredEntry = applyFilters(entryString, logEntryFilters);
			logSnapshot.consume(filteredEntry);
		}
		return logSnapshot;
	}

	// TODO address this quadratic time complexity
	private E applyFilters(final String toParse, final List<ILogEntryFilter<E>> filters) {
		E entry = null;
		for (ILogEntryFilter<E> filter : filters) {
			entry = filter.parse(toParse);
			if (entry != null) {
				break;
			}
		}
		return entry;
	}

	public void cleanup() {
		this.readEntries.clear();
	}
}
