package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.logparser.filter.IMessageFilter;

/**
 * Skeletal implementation of {@link ILogParser} with typical parser
 * functionality.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
public class AbstractLogParser<E> implements ILogParser<E> {
	private final IMessageFilter<E> messageFilter;
	private final AtomicInteger count;
	private final List<E> filteredEntries;

	public AbstractLogParser(IMessageFilter<E> messageFilter) {
		this.messageFilter = messageFilter;
		this.count = new AtomicInteger();
		this.filteredEntries = new ArrayList<E>();
	}

	public List<E> parse(final String filePathAndName) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePathAndName));
			String str;
			E entry = null;
			while ((str = in.readLine()) != null) {
				count.incrementAndGet();
				entry = messageFilter.parse(str);
				if (entry != null) {
					filteredEntries.add(entry);
				}
			}
			in.close();

		} catch (Exception e) {
			throw new IllegalArgumentException(String.format(
					"Failed to parse %s", filePathAndName), e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format(
						"Failed to properly close %s", filePathAndName), ioe);
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
}
