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
 * Implementation of {@link ILogFilter} that processes a log file one line at a
 * time. It is expected to have slightly worse performance than
 * {@link InMemoryLogFilter} but with better memory utilization.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of elements held by this {@link ILogFilter}.
 */
@Immutable
public class LineByLineLogFilter<E> extends AbstractLogFilter<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private final List<E> filteredEntries;

	public LineByLineLogFilter(final IMessageFilter<E>... messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public LineByLineLogFilter(final List<IMessageFilter<E>> messageFilters) {
		Preconditions.checkNotNull(messageFilters);
		for (IMessageFilter<E> filter : messageFilters) {
			Preconditions.checkNotNull(filter);
		}
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.filteredEntries = new ArrayList<E>();
	}

	public LogSnapshot<E> filter(final String filepath) {
		Preconditions.checkNotNull(filepath);
		BufferedReader in = null;
		int count = 0;
		try {
			in = new BufferedReader(new FileReader(filepath));
			String str;
			E entry;
			while ((str = in.readLine()) != null) {
				count++;
				entry = applyFilters(str, messageFilters);
				if (entry != null) {
					filteredEntries.add(entry);
				}
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
		return new LogSnapshot<E>(filteredEntries, count);
	}

	public void cleanup() {
		this.filteredEntries.clear();
	}
}
