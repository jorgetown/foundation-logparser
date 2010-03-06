package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.logparser.AbstractLogFilter;
import org.logparser.ILogFilter;
import org.logparser.IMessageFilter;
import org.logparser.LogSnapshot;
import org.logparser.Preconditions;

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
public class InMemoryLogFilter<E> extends AbstractLogFilter<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private List<E> filteredEntries;
	private final List<String> readEntries;

	public InMemoryLogFilter(final IMessageFilter<E>... messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public InMemoryLogFilter(final List<IMessageFilter<E>> messageFilters) {
		Preconditions.checkNotNull(messageFilters);
		for (IMessageFilter<E> filter : messageFilters) {
			Preconditions.checkNotNull(filter);
		}
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.filteredEntries = new ArrayList<E>();
		this.readEntries = new ArrayList<String>();
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
		return new LogSnapshot<E>(filteredEntries, readEntries.size());
	}

	public void cleanup() {
		this.filteredEntries.clear();
		this.readEntries.clear();
	}
}
