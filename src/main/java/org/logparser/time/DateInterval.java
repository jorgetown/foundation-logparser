package org.logparser.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.jcip.annotations.Immutable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Represents a finite length of time marked off by two {@link Date}s endpoints.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class DateInterval implements ITimeInterval {
	public static final String DATE_FORMAT = "yyyy/MM/dd";
	private final Date end;
	private final Date begin;

	private static final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};

	public DateInterval(final Date begin, final Date end) {
		Preconditions.checkNotNull(begin, "'begin' argument cannot be null.");
		Preconditions.checkNotNull(end, "'end' argument cannot be null.");
		this.begin = new Date(begin.getTime());
		this.end = new Date(end.getTime());
	}

	/**
	 * Answers whether a given {@link Date} lies between the two endpoints.
	 * 
	 * @param date the {@link Date} being compared.
	 * @return true if {@code date} is between {@code begin} and {@code end}.
	 */
	public boolean isBetweenInstants(final Date date) {
		Preconditions.checkNotNull(date);
		return date.after(begin) && date.before(end);
	}

	public Date getBegin() {
		return new Date(begin.getTime());
	}

	public Date getEnd() {
		return new Date(end.getTime());
	}

	public static String formatDate(final Date date) {
		return dateFormatter.get().format(date);
	}

	public static Date parseDate(final String date) throws ParseException {
		return dateFormatter.get().parse(date);
	}

	@JsonCreator
	public static DateInterval valueOf(@JsonProperty("begin") String begin, @JsonProperty("end") String end) {
		Date today = new Date();
		Calendar cal = new GregorianCalendar();
		if (Strings.isNullOrEmpty(begin)) {
			// no 'begin' given? set date 100yrs back into the past
			cal.setTime(today);
			cal.add(Calendar.YEAR, -100);
			begin = formatDate(cal.getTime());
		}
		if (Strings.isNullOrEmpty(end)) {
			// no 'end' given? set date 100yrs in the future
			cal.setTime(today);
			cal.add(Calendar.YEAR, 100);
			end = formatDate(cal.getTime());
		}
		try {
			return new DateInterval(parseDate(begin), parseDate(end));
		} catch (ParseException pe) {
			// If the date format is wrong, fail quickly
			throw new IllegalArgumentException(String.format("Unable to parse one or both of 'dateInterval' arguments begin='%s', end='%s' with pattern '%s'; check JSON config file.",
					begin,
					end,
					DATE_FORMAT));
		}
	}

	@Override
	public String toString() {
		return String.format("{begin=%s, end=%s}", formatDate(begin), formatDate(end));
	}
}
