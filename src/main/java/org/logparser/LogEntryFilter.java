package org.logparser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.logparser.config.FilterParams;
import org.logparser.time.ITimeInterval;

import com.google.common.base.Preconditions;

/**
 * Log entry filter implementation to parse log entries.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class LogEntryFilter implements ILogEntryFilter<LogEntry> {
	private final Pattern timestampPattern;
	private final Pattern actionPattern;
	private final Pattern durationPattern;
	private final Pattern filterPattern;
	private final ITimeInterval timeInterval;

	/**
	 * The date format to expect from the log entries to be filtered.
	 */
	private final ThreadLocal<DateFormat> dateFormatter;

	public LogEntryFilter(final FilterParams filterParams) {
		Preconditions.checkNotNull(filterParams);
		Preconditions.checkNotNull(filterParams.getTimestampFormat());
		Preconditions.checkNotNull(filterParams.getTimestampPattern());
		Preconditions.checkNotNull(filterParams.getActionPattern());
		Preconditions.checkNotNull(filterParams.getDurationPattern());
		Preconditions.checkNotNull(filterParams.getFilterPattern());
		this.dateFormatter = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(filterParams.getTimestampFormat());
			}
		};
		this.timestampPattern = filterParams.getTimestampPattern();
		this.actionPattern = filterParams.getActionPattern();
		this.durationPattern = filterParams.getDurationPattern();
		this.filterPattern = filterParams.getFilterPattern();
		this.timeInterval = filterParams.getTimeInterval();
	}

	public LogEntry parse(final String text) {
		Matcher m = timestampPattern.matcher(text);
		if (m.find()) {
			Date date = getDateFromString(m.group(1));
			if (timeInterval.isBetweenInstants(date)) {
				m = actionPattern.matcher(text);
				if (m.find()) {
					String action = m.group(1);
					if (filterPattern.matcher(action).matches()) {
						m = durationPattern.matcher(text);
						if (m.find()) {
							String duration = m.group(1);
							return new LogEntry(date.getTime(), action, Double.valueOf(duration));
						}
					}
				}
			}
		}

		return null;
	}

	private Date getDateFromString(final String dateTime) {
		Date date;
		try {
			date = dateFormatter.get().parse(dateTime);
		} catch (ParseException pe) {
			// If the date format is wrong, fail quickly
			throw new IllegalArgumentException(
					String.format("Check timestamp regex '%s' or timestamp format '%s'; unable to parse '%s'", getTimestampPattern(), getTimestampFormat(), dateTime), pe);
		}
		return date;
	}

	public Pattern getTimestampPattern() {
		return timestampPattern;
	}

	public DateFormat getDateFormatter() {
		return dateFormatter.get();
	}

	public Pattern getActionPattern() {
		return actionPattern;
	}

	public Pattern getDurationPattern() {
		return durationPattern;
	}

	public Pattern getFilterPattern() {
		return filterPattern;
	}
	
	public String getTimestampFormat() {
		return dateFormatter.get().toString();
	}

	public ITimeInterval getTimeInterval() {
		return timeInterval;
	}
}
