package org.logparser;

import net.jcip.annotations.Immutable;

import org.logparser.time.ITimeComparable;
import org.logparser.time.TimeComparator;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampler.
 * 
 * In this particular case, it extracts log entries each time the interval
 * between any 2 entries is longer than the value given by the given
 * {@link TimeComparator}.
 * 
 * Note: if reflection's performance is an issue make your log entry implement
 * {@link ITimeComparable}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class GenericSamplingByTime<E extends IStatsCapable> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final TimeComparator<E> timeComparator;
	private E previous;
	private E max;
	private E min;

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

			// preserve max & min in the sample, regardless of time difference
			if (max == null || (entry.getElapsedTime() > max.getElapsedTime())) {
				sampled = entry;
				max = entry;
			}
			if (min == null || (entry.getElapsedTime() < min.getElapsedTime())) {
				sampled = entry;
				min = entry;
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

	public E getMax() {
		return max;
	}

	public E getMin() {
		return min;
	}
}
