package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import net.jcip.annotations.Immutable;

import org.logparser.AbstractLogFilter;
import org.logparser.FilterConfig;
import org.logparser.ILogFilter;
import org.logparser.IMessageFilter;
import org.logparser.ITimestampedEntry;
import org.logparser.LogSnapshot;

import com.google.common.base.Preconditions;

/**
 * Implementation of {@link ILogFilter} that reads and processes a log file
 * concurrently. The processing of log entries is done in the background.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held by this {@link ILogFilter}.
 */
@Immutable
public class BackgroundLogFilter<E extends ITimestampedEntry> extends AbstractLogFilter<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private List<E> filteredEntries;
	private final ExecutorService background;
	private final BlockingQueue<String> queue;
	private final Map<String, Integer> summary;
	private final Map<Integer, Integer> timeBreakdown;
	private final int groupBy;
	private final Calendar calendar;

	public BackgroundLogFilter(final FilterConfig filterConfig, final IMessageFilter<E>... messageFilter) {
		this(filterConfig, Arrays.asList(messageFilter));
	}

	public BackgroundLogFilter(final FilterConfig filterConfig, final List<IMessageFilter<E>> messageFilters) {
		Preconditions.checkNotNull(messageFilters);
		for (IMessageFilter<E> filter : messageFilters) {
			Preconditions.checkNotNull(filter);
		}
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.filteredEntries = new ArrayList<E>();
		this.background = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		this.queue = new LinkedBlockingQueue<String>(5000);
		this.summary = new TreeMap<String, Integer>();
		this.timeBreakdown = new TreeMap<Integer, Integer>();
		this.groupBy = filterConfig.groupByToCalendar();
		this.calendar = Calendar.getInstance();
	}

	public LogSnapshot<E> filter(final String filepath) {
		Preconditions.checkNotNull(filepath);
		BackgroundParser worker = new BackgroundParser(queue, messageFilters);
		Future<Collection<E>> future = background.submit(worker);

		BufferedReader in = null;
		int count = 0;
		try {
			in = new BufferedReader(new FileReader(filepath));
			String str;
			while ((str = in.readLine()) != null) {
				count++;
				queue.put(str);
			}
			in.close();
			// signal EOF
			worker.readComplete();

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

		try {
			filteredEntries = new ArrayList<E>(future.get());
			background.shutdown();
			return new LogSnapshot<E>(filteredEntries, count, summary, timeBreakdown);
		} catch (InterruptedException ie) {
			// TODO handle properly
		} catch (ExecutionException ee) {
			// TODO handle properly
		}
		return null;
	}

	private final class BackgroundParser implements Callable<Collection<E>> {

		private volatile boolean EOF;
		private final BlockingQueue<String> pipeline;
		private final List<IMessageFilter<E>> filters;

		public BackgroundParser(BlockingQueue<String> queue, final List<IMessageFilter<E>> filters) {
			this.pipeline = queue;
			this.filters = filters;
		}

		public Collection<E> call() {
			System.out.println("Client thread starting...");
			final Queue<E> list = new ConcurrentLinkedQueue<E>();
			boolean stopCondition = (pipeline.isEmpty() && EOF);

			while (!stopCondition) {
				final String head;
				E entry;
				try {
					head = pipeline.take();
					if (head != null) {
						entry = applyFilters(head, filters);
						if (entry != null) {
							list.add(entry);
						}
					}
				} catch (InterruptedException ie) {
					// TODO handle properly
					ie.printStackTrace();
				}

				stopCondition = (pipeline.isEmpty() && EOF);
			}

			System.out.println("Client thread exiting...");
			return list;
		}

		public void readComplete() {
			this.EOF = true;
		}
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
		int key = calendar.get(groupBy);
		if (timeBreakdown.containsKey(key)) {
			int value = timeBreakdown.get(key);
			value++;
			timeBreakdown.put(key, value);
		} else {
			timeBreakdown.put(key, 1);
		}
	}

	public void cleanup() {
		this.background.shutdownNow();
		this.filteredEntries.clear();
		this.queue.clear();
		this.summary.clear();
		this.timeBreakdown.clear();
		this.filteredEntries = null;
	}
}
