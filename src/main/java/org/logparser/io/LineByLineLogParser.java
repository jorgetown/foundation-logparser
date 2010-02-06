package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.Immutable;

import org.logparser.filter.IMessageFilter;

/**
 * Implementation of {@link ILogParser} that processes a log file one line at a
 * time. It is expected to have slightly worse performance than
 * {@link InMemoryLogParser} but with better memory utilization.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
@Immutable
public class LineByLineLogParser<E> extends AbstractLogParser<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private final List<E> filteredEntries;
	private final AtomicInteger count;

	public LineByLineLogParser(final IMessageFilter<E>... messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public LineByLineLogParser(final List<IMessageFilter<E>> messageFilters) {
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.filteredEntries = new ArrayList<E>();
		this.count = new AtomicInteger(0);
	}

	public List<E> parse(final String filePathAndName) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePathAndName));
			String str;
			E entry;
			while ((str = in.readLine()) != null) {
				count.incrementAndGet();
				entry = applyFilters(str, messageFilters);
				if (entry != null) {
					filteredEntries.add(entry);
				}
			}
			in.close();
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("Failed to parse %s", filePathAndName), e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format("Failed to properly close %s", filePathAndName), ioe);
			}
		}
		return Collections.unmodifiableList(filteredEntries);
	}

	@Override
	public List<E> getParsedEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	@Override
	public int getTotalEntries() {
		return count.get();
	}

	@Override
	public E getEarliestEntry() {
		if (!filteredEntries.isEmpty()) {
			return filteredEntries.get(0);
		}
		return null;
	}

	@Override
	public E getLatestEntry() {
		if (!filteredEntries.isEmpty()) {
			return filteredEntries.get(filteredEntries.size() - 1);
		}
		return null;
	}

	public void dispose() {
		this.filteredEntries.clear();
	}
}
