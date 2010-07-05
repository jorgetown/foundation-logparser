package org.logparser;

import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

import com.google.common.base.Preconditions;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampler.
 * 
 * In this particular case, it extracts log entries each time the interval
 * between any 2 entries is longer than the value given by {@code timeInMillis}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SamplingByTime<E extends ITimestampedEntry> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final long timeInMillis;
	private E previous;

	public SamplingByTime(final IMessageFilter<E> filter, final long time) {
		this(filter, time, TimeUnit.MILLISECONDS);
	}

	public SamplingByTime(final IMessageFilter<E> filter, final long time, final TimeUnit timeUnit) {
		Preconditions.checkNotNull(filter);
		Preconditions.checkNotNull(timeUnit);
		this.filter = filter;
		this.timeInMillis = timeUnit.toMillis(time);
		this.previous = null;
	}

	public E parse(final String text) {
		E entry = filter.parse(text);
		if (entry == null) {
			return entry;
		}
		E sampled = null;
		if (previous == null
				|| (entry.getTimestamp() - previous.getTimestamp() > timeInMillis)) {
			sampled = entry;
		}
		previous = entry;
		return sampled;
	}

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}