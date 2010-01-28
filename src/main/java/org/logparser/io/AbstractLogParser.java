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
 * Skeletal implementation of {@link ILogParser} with typical parser
 * functionality.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
@Immutable
public class AbstractLogParser<E> implements ILogParser<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private final AtomicInteger count;
	private final List<E> filteredEntries;

	@SuppressWarnings("unchecked")
	public AbstractLogParser(final IMessageFilter<E> messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public AbstractLogParser(final List<IMessageFilter<E>> messageFilters) {
		this.messageFilters = messageFilters;
		this.count = new AtomicInteger();
		this.filteredEntries = new ArrayList<E>();
	}

	public List<E> parse(final String filePathAndName) {
		BufferedReader in = null;
		try {
			for (IMessageFilter<E> mf : messageFilters) {
				in = new BufferedReader(new FileReader(filePathAndName));
				String str;
				E entry = null;
				while ((str = in.readLine()) != null) {
					count.incrementAndGet();
					entry = mf.parse(str);
					if (entry != null) {
						filteredEntries.add(entry);
					}
				}
				in.close();
			}

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

	public List<E> getParsedEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	public int getTotalEntries() {
		return count.get();
	}

	public E getEarliestEntry() {
		if (!filteredEntries.isEmpty()) {
			return filteredEntries.get(0);
		}
		return null;
	}

	public E getLatestEntry() {
		if (!filteredEntries.isEmpty()) {
			return filteredEntries.get(filteredEntries.size() - 1);
		}
		return null;
	}
}
