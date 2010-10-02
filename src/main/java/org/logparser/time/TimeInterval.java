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
public final class TimeInterval implements ITimeInterval {
	private final Instant end;
	private final Instant begin;
	private final Calendar cal;
	private final Calendar from;
	private final Calendar to;

	public TimeInterval(final Instant instant) {
		this(instant, instant);
	}

	@JsonCreator
	public TimeInterval(@JsonProperty("begin") final String begin, @JsonProperty("end") final String end) {
		this(Instant.valueOf(begin), Instant.valueOf(end));
	}

	public TimeInterval(final Instant begin, final Instant end) {
		this.begin = begin;
		this.end = end;
		this.cal = Calendar.getInstance();
		this.from = Calendar.getInstance();
		this.to = Calendar.getInstance();
	}

	/**
	 * Answers whether a given {@link Date} lies between two time instants.
	 * 
	 * @param date the {@link Date} being compared.
	 * @return true if {@code date} lies after earliest time instant and before latest time instant.
	 */
	public boolean isBetweenInstants(final Date date) {
		return isBefore(date) && isAfter(date);
	}

	public boolean isBefore(final Date date) {
		// defensive copy since {@link Date}s aren't immutable
		Date d = new Date(date.getTime());
		cal.setTime(d);
		to.setTime(d);
		to.set(Calendar.HOUR_OF_DAY, end.getHour());
		to.set(Calendar.MINUTE, end.getMinute());

		return cal.before(to);
	}

	public boolean isAfter(final Date date) {
		// defensive copy since {@link Date}s aren't immutable
		Date d = new Date(date.getTime());
		cal.setTime(d);
		from.setTime(d);
		from.set(Calendar.HOUR_OF_DAY, begin.getHour());
		from.set(Calendar.MINUTE, begin.getMinute());

		return cal.after(from);
	}

	public Instant getBegin() {
		return begin;
	}

	public Instant getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return String.format("{begin=%s, end=%s}", begin, end);
	}
}
