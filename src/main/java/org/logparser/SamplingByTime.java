package org.logparser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

import com.google.common.base.Preconditions;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a sampler.
 * 
 * In this particular case, it extracts log entries each time the time interval
 * between any 2 entries is longer than the value given by {@code timeInMillis}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SamplingByTime<E extends ITimestampedEntry> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final long timeInMillis;
	private final Map<String, E> sampleTable;

	public SamplingByTime(final IMessageFilter<E> filter, final long time) {
		this(filter, time, TimeUnit.MILLISECONDS);
	}

	public SamplingByTime(final IMessageFilter<E> filter, final long time, final TimeUnit timeUnit) {
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

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}