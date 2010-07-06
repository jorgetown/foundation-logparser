package org.logparser;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;

import com.google.common.base.Preconditions;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a sampler.
 * 
 * In this particular case, it extracts log entries at the rate given by {@code frequency}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SamplingByFrequency<E extends ITimestampedEntry> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final int frequency;
	private final Map<String, Integer> sampleTable;

	public SamplingByFrequency(final IMessageFilter<E> filter, final int frequency) {
		Preconditions.checkNotNull(filter);
		this.filter = filter;
		this.frequency = frequency;
		this.sampleTable = new HashMap<String, Integer>();
	}

	public E parse(final String text) {
		E entry = filter.parse(text);
		if (entry != null) {
			String action = entry.getAction();
			if (!sampleTable.containsKey(action)) {
				sampleTable.put(action, 0);
				return entry;
			}
			int i = sampleTable.get(action);
			i++;
			if (i >= frequency) {
				sampleTable.put(action, 0);
				return entry;
			}
			sampleTable.put(action, i);
		}
		return null;
	}

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public int getFrequency() {
		return frequency;
	}
}