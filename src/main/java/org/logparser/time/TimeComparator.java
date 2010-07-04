package org.logparser.time;

import java.util.concurrent.TimeUnit;

import org.logparser.ITimestampedEntry;

import com.google.common.base.Preconditions;

/**
 * Generic comparator of log entries' timestamps.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
public class TimeComparator<E extends ITimestampedEntry> {
	private final long timeInMillis;

	public TimeComparator(final long time, final TimeUnit timeUnit) {
		Preconditions.checkNotNull(timeUnit);
		this.timeInMillis = timeUnit.toMillis(time);
	}

	/**
	 * Given two log entries, determines if they are {@code timeInMillis} apart.
	 * 
	 * @param firstEntry
	 * @param secondEntry
	 * @return
	 */
	public boolean isIntervalApart(final E firstEntry, final E secondEntry) {
		Preconditions.checkNotNull(firstEntry);
		Preconditions.checkNotNull(secondEntry);
		return isIntervalApart(firstEntry.getTimestamp(), secondEntry.getTimestamp());
	}

	public boolean isIntervalApart(final long t1, final long t2) {
		return Math.abs(t1 - t2) > timeInMillis;
	}
}