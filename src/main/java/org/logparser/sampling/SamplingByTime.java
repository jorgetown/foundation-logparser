package org.logparser.sampling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

import org.logparser.ILogEntryFilter;
import org.logparser.ITimestampedEntry;

import com.google.common.base.Preconditions;

/**
 * An {@link ILogEntryFilter} decorator that applies a sampling method to the
 * {@link ILogEntryFilter} it decorates.
 * 
 * It implements stratified sampling, with systematic sampling applied within each stratum. 
 * Each sample is selected from the (sub-)population at a regular/systematic interval. 
 * 
 * In this particular case, it extracts log entries each time the time interval 
 * between any 2 entries is longer than the value given by {@code timeInMillis}.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Stratified_sampling">Stratified sampling</a>
 * @see <a href="http://en.wikipedia.org/wiki/Systematic_sampling">Systematic sampling</a>
 * @see <a href="http://en.wikipedia.org/wiki/Decorator_pattern">Decorator pattern</a>
 * @author jorge.decastro
 * 
 */
@Immutable
public final class SamplingByTime<E extends ITimestampedEntry> implements ILogEntryFilter<E> {
	private final ILogEntryFilter<E> filter;
	private final long timeInMillis;
	private final Map<String, E> sampleTable;

	public SamplingByTime(final ILogEntryFilter<E> filter, final int time) {
		this(filter, time, TimeUnit.MILLISECONDS);
	}

	public SamplingByTime(final ILogEntryFilter<E> filter, final int time, final TimeUnit timeUnit) {
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

	public ILogEntryFilter<E> getFilter() {
		return filter;
	}

	public long getTimeInMillis() {
		return timeInMillis;
	}
}