package org.logparser;

import net.jcip.annotations.Immutable;

import org.logparser.time.TimeComparator;

import com.google.common.base.Preconditions;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampler.
 * 
 * In this particular case, it extracts log entries each time the interval
 * between any 2 entries is longer than the value given by the given
 * {@link TimeComparator}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class GenericSamplingByTime<E extends ITimestampedEntry> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final TimeComparator<E> timeComparator;
	private E previous;

	public GenericSamplingByTime(final IMessageFilter<E> filter, final TimeComparator<E> timeComparator) {
		Preconditions.checkNotNull(filter);
		Preconditions.checkNotNull(timeComparator);
		this.filter = filter;
		this.timeComparator = timeComparator;
		this.previous = null;
	}

	public E parse(final String text) {
		E entry = filter.parse(text);
		E sampled = null;
		if (entry != null) {
			if (previous == null
					|| (previous != null && timeComparator.isIntervalApart(previous, entry))) {
				sampled = entry;
				previous = entry;
			}
		}
		return sampled;
	}

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public TimeComparator<E> getTimeComparator() {
		return timeComparator;
	}
}
