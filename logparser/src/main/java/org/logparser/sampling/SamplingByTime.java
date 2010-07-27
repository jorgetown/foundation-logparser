package org.logparser.sampling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

import org.logparser.ILogEntryFilter;
import org.logparser.ITimestampedEntry;

import com.google.common.base.Preconditions;

/**
 * An {@link ILogEntryFilter} implementation that acts as a sampler.
 * 
 * In this particular case, it extracts log entries each time the time interval
 * between any 2 entries is longer than the value given by {@code timeInMillis}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SamplingByTime<E extends ITimestampedEntry> implements ILogEntryFilter<E> {
	private final ILogEntryFilter<E> filter;
	private final long timeInMillis;
	private final Map<String, E> sampleTable;

	public SamplingByTime(final ILogEntryFilter<E> filter, final int time) {
		this(filter, time, TimeUnit.MILLISECONDS);
	}

	public SamplingByTime(final ILogEntryFilter<E> filter, final int time, final TimeUnit timeUnit) {
		Preconditions.checkNotNull(filter);
		Preconditions.checkNotNull(timeUnit);
		this.filter = filter;
		this.timeInMillis = timeUnit.toMillis(time);
		this.sampleTable = new HashMap<String, E>();
	}

	public E parse(final String text) {
		E entry = filter.parse(text);
		if (entry != null) {
			String action = entry.getAction();
			if (!sampleTable.containsKey(action)) {
				sampleTable.put(action, entry);
				return entry;
			}
			E previous = sampleTable.get(action);
			if (entry.getTimestamp() - previous.getTimestamp() > timeInMillis) {
				sampleTable.put(action, entry);
				return entry;
			}
		}
		return null;
	}

	public ILogEntryFilter<E> getFilter() {
		return filter;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}