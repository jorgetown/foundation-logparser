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
	private static final String DATE_FORMAT = "yyyy/MM/dd";
	private final Date before;
	private final Date after;

	private static final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};

	public DateInterval(final Date after, final Date before) {
		Preconditions.checkNotNull(after, "'after' argument cannot be null.");
		Preconditions.checkNotNull(before, "'before' argument cannot be null.");
		this.after = new Date(after.getTime());
		this.before = new Date(before.getTime());
	}

	/**
	 * Answers whether a given {@link Date} lies between the two endpoints.
	 * 
	 * @param date the {@link Date} being compared.
	 * @return true if {@code date} is between {@code before} and {@code after}.
	 */
	public boolean isBetweenInstants(final Date date) {
		Preconditions.checkNotNull(date);
		return date.after(after) && date.before(before);
	}

	public Date getAfter() {
		return new Date(after.getTime());
	}

	public Date getBefore() {
		return new Date(before.getTime());
	}

	public static String formatDate(final Date date) {
		return dateFormatter.get().format(date);
	}

	public static Date parseDate(final String date) throws ParseException {
		return dateFormatter.get().parse(date);
	}

	@JsonCreator
	public static DateInterval valueOf(@JsonProperty("after") String after, @JsonProperty("before") String before) {
		Date today = new Date();
		Calendar cal = new GregorianCalendar();
		if (Strings.isNullOrEmpty(after)) {
			// no 'after' given? set date 100yrs back into the past
			cal.setTime(today);
			cal.add(Calendar.YEAR, -100);
			after = formatDate(cal.getTime());
		}
		if (Strings.isNullOrEmpty(before)) {
			// no 'before' given? set date 100yrs in the future
			cal.setTime(today);
			cal.add(Calendar.YEAR, 100);
			before = formatDate(cal.getTime());
		}
		try {
			return new DateInterval(parseDate(after), parseDate(before));
		} catch (ParseException pe) {
			// If the date format is wrong, fail quickly
			throw new IllegalArgumentException(
					String.format("One or both of 'dateInterval' arguments before='%s', after='%s' failed to be parsed with pattern '%s'; check JSON config file.", before, after, DATE_FORMAT), pe);
		}
	}

	@Override
	public String toString() {
		return String.format("{after=%s, before=%s}", formatDate(after), formatDate(before));
	}
}
