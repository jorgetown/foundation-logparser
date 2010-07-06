package org.logparser;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;

import org.logparser.time.TimeComparator;

import com.google.common.base.Preconditions;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a sampler.
 * 
 * In this particular case, it extracts log entries each time the time interval
 * between any 2 entries is longer than the time given by the given
 * {@link TimeComparator}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class GenericSamplingByTime<E extends ITimestampedEntry> implements IMessageFilter<E> {
	private final IMessageFilter<E> filter;
	private final TimeComparator<E> timeComparator;
	private final Map<String, E> sampleTable;

	public GenericSamplingByTime(final IMessageFilter<E> filter, final TimeComparator<E> timeComparator) {
		Preconditions.checkNotNull(filter);
		Preconditions.checkNotNull(timeComparator);
		this.filter = filter;
		this.timeComparator = timeComparator;
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
			if (timeComparator.isIntervalApart(previous, entry)) {
				sampleTable.put(action, entry);
				return entry;
			}
		}
		return null;
	}

	public IMessageFilter<E> getFilter() {
		return filter;
	}

	public TimeComparator<E> getTimeComparator() {
		return timeComparator;
	}
}
