package org.logparser.time;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.logparser.Preconditions;

/**
 * Generic comparator of log entries' timestamps.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
public class TimeComparator<E> {
	private final String field;
	private final long timeInMillis;

	public TimeComparator(final String field, final long time, final TimeUnit timeUnit) {
		Preconditions.checkNotNull(field);
		Preconditions.checkNotNull(timeUnit);
		this.field = field;
		this.timeInMillis = timeUnit.toMillis(time);
	}

	/**
	 * Given two log entries, determines if they are {@code timeInMillis} apart.
	 * 
	 * @param firstEntry
	 * @param secondEntry
	 * @return
	 */
	public boolean isIntervalApart(E firstEntry, E secondEntry) {
		try {
			Field field1 = firstEntry.getClass().getDeclaredField(field);
			field1.setAccessible(true);
			Field field2 = secondEntry.getClass().getDeclaredField(field);
			field2.setAccessible(true);
			if (field1.get(firstEntry) instanceof Number) {
				long t1 = field1.getLong(firstEntry);
				long t2 = field2.getLong(secondEntry);
				return t2 - t1 > timeInMillis;
			} else if (field1.get(firstEntry) instanceof Date) {
				Date t1 = (Date) field1.get(firstEntry);
				Date t2 = (Date) field2.get(secondEntry);
				return t2.getTime() - t1.getTime() > timeInMillis;
			} else if (field1.get(firstEntry) instanceof String) {
				String t1 = (String) field1.get(firstEntry);
				String t2 = (String) field2.get(secondEntry);
				return Long.valueOf(t2) - Long.valueOf(t1) > timeInMillis;
			}
			return false;
		} catch (Throwable t) {
			throw new IllegalArgumentException("Fatal error accessing log entry object by reflection", t);
		}
	}
}
