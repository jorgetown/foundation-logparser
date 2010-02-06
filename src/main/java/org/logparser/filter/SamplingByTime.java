package org.logparser.filter;

import java.util.Calendar;

import net.jcip.annotations.Immutable;

import org.logparser.ITimeComparable;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampler.
 * 
 * In this particular case, it extracts log entries each time the interval
 * between any 2 entries is longer than the value given by the 'timeInMillis'
 * argument.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SamplingByTime<E extends ITimeComparable> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final long timeInMillis;
	private Calendar previous;

	public SamplingByTime(final IMessageFilter<E> filter, final long timeInMillis) {
		this.filter = filter;
		this.timeInMillis = timeInMillis;
		this.previous = Calendar.getInstance();
	}

	public E parse(final String text) {
		E entry = filter.parse(text);
		if (entry == null) {
			return entry;
		}
		E sampled = null;
		if (previous.getTime() == null
				|| (entry.getTime() - previous.getTimeInMillis() > timeInMillis)) {
			sampled = entry;
		}
		previous.setTimeInMillis(entry.getTime());
		return sampled;
	}

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}
