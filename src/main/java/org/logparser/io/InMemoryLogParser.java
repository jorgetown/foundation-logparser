package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.logparser.filter.IMessageFilter;

/**
 * Implementation of {@link ILogParser} that stores and processes a log file's
 * entries in memory. It is, thus, prone to {@link OutOfMemoryError}s when
 * dealing with large files.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
@Immutable
public class InMemoryLogParser<E> extends AbstractLogParser<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private final List<E> filteredEntries;
	private final List<String> readEntries;

	public InMemoryLogParser(final IMessageFilter<E>... messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public InMemoryLogParser(final List<IMessageFilter<E>> messageFilters) {
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.filteredEntries = new ArrayList<E>();
		this.readEntries = new ArrayList<String>();
	}

	public List<E> parse(final String filePathAndName) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePathAndName));
			String str;
			while ((str = in.readLine()) != null) {
				readEntries.add(str);
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
		
		parse(readEntries, messageFilters, filteredEntries);
		return Collections.unmodifiableList(filteredEntries);
	}

	public void parse(final List<String> originalEntries, final List<IMessageFilter<E>> filters, final List<E> filtered) {
		E message;
		for (String entry : originalEntries) {
			message = applyFilters(entry, filters);
			if (message != null) {
				filtered.add(message);
			}
		}
	}

	public List<String> getReadEntries() {
		return Collections.unmodifiableList(readEntries);
	}

	@Override
	public List<E> getParsedEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	@Override
	public int getTotalEntries() {
		return readEntries.size();
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
		this.readEntries.clear();
	}
}
