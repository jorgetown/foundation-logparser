package org.logparser.time;

import java.util.Calendar;
import java.util.Date;

import net.jcip.annotations.Immutable;

/**
 * Represents a finite length of time marked off by two {@link Instant}s in time.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class SimpleTimeInterval implements ITimeInterval {
	private final Instant before;
	private final Instant after;
	private static final Calendar cal;
	private static final Calendar from;
	private static final Calendar to;

	static {
		// because {@link Calendar}s are expensive to create
		cal = Calendar.getInstance();
		from = Calendar.getInstance();
		to = Calendar.getInstance();
	}

	public SimpleTimeInterval(final Instant instant) {
		this(instant, instant);
	}

	public SimpleTimeInterval(final Instant after, final Instant before) {
		this.after = after;
		this.before = before;
	}

	/**
	 * Answers whether a given {@link Date} lies between two time instants.
	 * 
	 * @param date the {@link Date} being compared.
	 * @return true if {@link date} lies after earliest time instant and before
	 *         latest time instant.
	 */
	public boolean isBetweenInstants(final Date date) {
		return isBefore(date) && isAfter(date);
	}

	public boolean isBefore(final Date date) {
		// defensive copy since {@link Date}s aren't immutable
		Date d = new Date(date.getTime());
		cal.setTime(d);
		to.setTime(d);
		to.set(Calendar.HOUR_OF_DAY, before.getHour());
		to.set(Calendar.MINUTE, before.getMinute());

		return cal.before(to);
	}

	public boolean isAfter(final Date date) {
		// defensive copy since {@link Date}s aren't immutable
		Date d = new Date(date.getTime());
		cal.setTime(d);
		from.setTime(d);
		from.set(Calendar.HOUR_OF_DAY, after.getHour());
		from.set(Calendar.MINUTE, after.getMinute());

		return cal.after(from);
	}

	@Override
	public String toString() {
		return String.format("{after=%s, before=%s}", after, before);
	}
}
