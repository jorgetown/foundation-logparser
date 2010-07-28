package org.logparser.time;

import java.util.Calendar;
import java.util.Date;

import net.jcip.annotations.Immutable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents a finite length of time marked off by two {@link Instant}s in time.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class TimeInterval implements ITimeInterval {
	private final Instant before;
	private final Instant after;
	private final Calendar cal;
	private final Calendar from;
	private final Calendar to;
	
	public TimeInterval(final Instant instant) {
		this(instant, instant);
	}
	
	@JsonCreator
	public TimeInterval(@JsonProperty("after") final String after, @JsonProperty("before") final String before) {
		this(Instant.valueOf(after), Instant.valueOf(before));
	}

	public TimeInterval(final Instant after, final Instant before) {
		this.after = after;
		this.before = before;
		this.cal = Calendar.getInstance();
		this.from = Calendar.getInstance();
		this.to = Calendar.getInstance();
	}

	/**
	 * Answers whether a given {@link Date} lies between two time instants.
	 * 
	 * @param date the {@link Date} being compared.
	 * @return true if {@code date} lies after earliest time instant and before
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
	
	public Instant getAfter() {
		return after;
	}
	
	public Instant getBefore() {
		return before;
	}
	
	@Override
	public String toString() {
		return String.format("{after=%s, before=%s}", after, before);
	}
}
