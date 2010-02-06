package org.logparser.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.Immutable;

import org.logparser.filter.IMessageFilter;

/**
 * Implementation of {@link ILogParser} that reads and processes a log file
 * concurrently. The processing of log entries is done in the background.
 * 
 * @author jorge.decastro
 * 
 * @param <E>
 */
@Immutable
public class BackgroundLogParser<E> extends AbstractLogParser<E> {
	private final List<IMessageFilter<E>> messageFilters;
	private final AtomicInteger count;
	private List<E> filteredEntries;
	private final ExecutorService background;
	private final BlockingQueue<String> queue;

	public BackgroundLogParser(final IMessageFilter<E>... messageFilter) {
		this(Arrays.asList(messageFilter));
	}

	public BackgroundLogParser(final List<IMessageFilter<E>> messageFilters) {
		this.messageFilters = Collections.unmodifiableList(messageFilters);
		this.count = new AtomicInteger();
		this.filteredEntries = new ArrayList<E>();
		this.background = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		this.queue = new LinkedBlockingQueue<String>(5000);
	}

	public List<E> parse(final String filePathAndName) {

		BackgroundParser worker = new BackgroundParser(queue, messageFilters);
		Future<Collection<E>> future = background.submit(worker);

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePathAndName));
			String str;
			while ((str = in.readLine()) != null) {
				count.incrementAndGet();
				queue.put(str);
			}
			in.close();
			// signal EOF
			worker.readComplete();

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

		try {
			filteredEntries = new ArrayList<E>(future.get());
			background.shutdown();
			return Collections.unmodifiableList(filteredEntries);
		} catch (InterruptedException ie) {
			// TODO handle properly
			ie.printStackTrace();
		} catch (ExecutionException ee) {
			// TODO handle properly
			ee.printStackTrace();
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
		this.background.shutdownNow();
		this.filteredEntries.clear();
		this.queue.clear();
		this.filteredEntries = null;
	}
}
