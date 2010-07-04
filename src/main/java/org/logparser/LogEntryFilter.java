package org.logparser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;
import org.logparser.time.Instant;
import org.logparser.time.SimpleTimeInterval;

import com.google.common.base.Preconditions;

/**
 * Message filter implementation to parse log entries.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class LogEntryFilter implements IMessageFilter<LogEntry> {
	private final Pattern timestampPattern;
	private final Pattern actionPattern;
	private final Pattern durationPattern;
	private final Pattern filterPattern;
	private final ITimeInterval timeInterval;
	private final FilterConfig filterConfig;

	/**
	 * The date format to expect from the log entries to be filtered.
	 */
	private final ThreadLocal<DateFormat> dateFormatter;

	public LogEntryFilter(final FilterConfig filterConfig) {
		Preconditions.checkNotNull(filterConfig);
		this.filterConfig = filterConfig;
		Preconditions.checkNotNull(filterConfig.getTimestampFormat());
		Preconditions.checkNotNull(filterConfig.getTimestampPattern());
		Preconditions.checkNotNull(filterConfig.getActionPattern());
		Preconditions.checkNotNull(filterConfig.getDurationPattern());
		Preconditions.checkNotNull(filterConfig.getFilterPattern());
		this.dateFormatter = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(filterConfig.getTimestampFormat());
			}
		};
		this.timestampPattern = Pattern.compile(filterConfig.getTimestampPattern());
		this.actionPattern = Pattern.compile(filterConfig.getActionPattern());
		this.durationPattern = Pattern.compile(filterConfig.getDurationPattern());
		this.filterPattern = Pattern.compile(filterConfig.getFilterPattern());

		Instant after = filterConfig.getAfter();
		Instant before = filterConfig.getBefore();
		if (after != null && before != null) {
			this.timeInterval = new SimpleTimeInterval(after, before);
		} else {
			this.timeInterval = new InfiniteTimeInterval();
		}
	}

	public LogEntry parse(final String text) {
		Matcher m = timestampPattern.matcher(text);
		Date date = null;
		String action = null;
		String duration = null;
		if (m.find()) {
			date = getDateFromString(m.group(1));
			m = actionPattern.matcher(text);
			if (m.find()) {
				action = m.group(1);
				m = durationPattern.matcher(text);
				if (m.find()) {
					duration = m.group(1);
					if (timeInterval.isBetweenInstants(date) && filterPattern.matcher(action).matches()) {						
						return new LogEntry(text, date, action, duration);
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
					String.format("Unable to parse the date for String :%s\n Expected format is: %s",
									dateTime, filterConfig.getTimestampFormat()), pe);
		}
		return date;
	}

	public Pattern getTimestampPattern() {
		return timestampPattern;
	}
	
	public DateFormat getDateFormatter() {
		return dateFormatter.get();
	}
}
