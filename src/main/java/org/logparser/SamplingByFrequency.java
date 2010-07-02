package org.logparser;

import net.jcip.annotations.Immutable;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampler.
 * 
 * In this particular case, it extracts log entries at the rate given by {@code
 * frequency}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SamplingByFrequency<E extends ITimestampedEntry> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final int frequency;
	private int count;
	private E max;
	private E min;

	public SamplingByFrequency(final IMessageFilter<E> filter, final int frequency) {
		Preconditions.checkNotNull(filter);
		this.filter = filter;
		this.frequency = frequency;
		this.count = 0;
	}

	public E parse(final String text) {
		E entry = filter.parse(text);
		if (entry != null) {
			if (max == null || (entry.getDuration() > max.getDuration())) {
				max = entry;
			}
			if (min == null || (entry.getDuration() < min.getDuration())) {
				min = entry;
			}
		}
		if (count >= frequency) {
			count = 0;
			return entry;
		}
		count++;
		return null;
	}

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public int getFrequency() {
		return frequency;
	}
}