package org.logparser.sampling;

import java.util.HashMap;
import java.util.Map;

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
 * In this particular case, it extracts log entries at the rate given by the sampling {@code interval}. 
 * If every 3rd log {@code E}ntry is desired, for example, the sampling {@code interval} is 3.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Stratified_sampling">Stratified sampling</a>
 * @see <a href="http://en.wikipedia.org/wiki/Systematic_sampling">Systematic sampling</a>
 * @see <a href="http://en.wikipedia.org/wiki/Decorator_pattern">Decorator pattern</a>
 * @author jorge.decastro
 * 
 */
@Immutable
public final class SamplingByFrequency<E extends ITimestampedEntry> implements ILogEntryFilter<E> {
	private final ILogEntryFilter<E> filter;
	private final int samplingInterval;
	private final Map<String, Integer> sampleTable;

	public SamplingByFrequency(final ILogEntryFilter<E> filter, final int interval) {
		this.filter = Preconditions.checkNotNull(filter);
		this.samplingInterval = interval;
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
			if (i >= samplingInterval) {
				sampleTable.put(action, 0);
				return entry;
			}
			sampleTable.put(action, i);
		}
		return null;
	}

	public ILogEntryFilter<E> getFilter() {
		return filter;
	}

	public int getSamplingInterval() {
		return samplingInterval;
	}
}