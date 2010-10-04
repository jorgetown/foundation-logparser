package org.logparser.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

import org.apache.log4j.Logger;
import org.logparser.ILogEntryFilter;
import org.logparser.ILogFilter;
import org.logparser.IObserver;
import org.logparser.Observable;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * Implementation of {@link ILogFilter} that processes a log file one line at a
 * time, and publishes filtered entries as events to all {@link IObserver}s
 * attached.
 * 
 * It is expected to have slightly worse performance than an "in memory"
 * implementation but with better memory utilization.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of elements held by this {@link ILogFilter}.
 */
@Immutable
public final class LineByLineLogFilter<E> extends Observable<E> implements ILogFilter {
	private static final Logger LOGGER = Logger.getLogger(LineByLineLogFilter.class.getName());
	private final List<ILogEntryFilter<E>> logEntryFilters;
	private int size;

	public LineByLineLogFilter(final ILogEntryFilter<E>... messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public LineByLineLogFilter(final List<ILogEntryFilter<E>> messageFilters) {
		Preconditions.checkNotNull(messageFilters, "'messageFilters' argument cannot be null.");
		for (ILogEntryFilter<E> filter : messageFilters) {
			Preconditions.checkNotNull(filter, "'filter' elements of 'messageFilters' argument cannot be null.");
		}
		this.logEntryFilters = Collections.unmodifiableList(messageFilters);
		this.size = 0;
	}

	public void filter(final File file) {
		Preconditions.checkNotNull(file, "'file' argument cannot be null.");
		BufferedReader in = null;
		size = 0;
		try {
			in = new BufferedReader(new FileReader(file));
			String str;
			E entry;
			while ((str = in.readLine()) != null) {
				size++;
				entry = applyFilters(str, logEntryFilters);
				if (entry != null) {
					notifyObservers(entry);
				}
			}
			in.close();
		} catch (IOException ioe) {
			LOGGER.warn(String.format("IO error reading file '%s'", file.getAbsolutePath()), ioe);
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	public void filter(final File[] files) {
		Preconditions.checkNotNull(files, "'files' argument cannot be null.");
		DecimalFormat df = new DecimalFormat("#.#");

		int current = 0;
		int previous = 0;
		for (File f : files) {
			long start = System.nanoTime();
			filter(f);
			long end = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
			current = size - previous;
			LOGGER.info(String.format("%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s entries",
					f.getName(),
					end,
					df.format(current / (double) end),
					current));
			previous = current;
		}
	}

	public int size() {
		return size;
	}

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
}
