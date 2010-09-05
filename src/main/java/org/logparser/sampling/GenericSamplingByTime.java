package org.logparser.sampling;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;

import org.logparser.ILogEntryFilter;
import org.logparser.ITimestampedEntry;
import org.logparser.time.TimeComparator;

import com.google.common.base.Preconditions;

/**
 * An {@link ILogEntryFilter} decorator that applies a sampling method to the
 * {@link ILogEntryFilter} it decorates.
 * 
 * It implements stratified sampling, with systematic sampling applied within each stratum. 
 * Each sample is selected from the (sub-)population at a regular/systematic interval.
 * 
 * In this particular case, it extracts {@link ITimestampedEntry}s each time the
 * time interval between any 2 entries is longer than the time given by the given {@link TimeComparator}.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Stratified_sampling">Stratified sampling</a>
 * @see <a href="http://en.wikipedia.org/wiki/Systematic_sampling">Systematic sampling</a>
 * @see <a href="http://en.wikipedia.org/wiki/Decorator_pattern">Decorator pattern</a>
 * @author jorge.decastro
 * 
 */
@Immutable
public class GenericSamplingByTime<E extends ITimestampedEntry> implements ILogEntryFilter<E> {
	private final ILogEntryFilter<E> filter;
	private final TimeComparator<E> timeComparator;
	private final Map<String, E> sampleTable;

	public GenericSamplingByTime(final ILogEntryFilter<E> filter, final TimeComparator<E> timeComparator) {
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

	public ILogEntryFilter<E> getFilter() {
		return filter;
	}

	public TimeComparator<E> getTimeComparator() {
		return timeComparator;
	}
}
