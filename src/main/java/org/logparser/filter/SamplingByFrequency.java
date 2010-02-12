package org.logparser.filter;

import net.jcip.annotations.Immutable;

import org.logparser.Preconditions;

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
public class SamplingByFrequency<E> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final int frequency;
	private int count;

	public SamplingByFrequency(final IMessageFilter<E> filter, final int frequency) {
		Preconditions.checkNotNull(filter);
		this.filter = filter;
		this.frequency = frequency;
		this.count = 0;
	}

	public E parse(final String text) {
		if (count >= frequency) {
			count = 0;
			return filter.parse(text);
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
