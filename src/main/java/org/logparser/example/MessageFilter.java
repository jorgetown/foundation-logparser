package org.logparser.example;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.logparser.Preconditions;
import org.logparser.filter.IMessageFilter;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;


/**
 * Message filter implementation to extract {@link Message}s from a log file.
 * The {@link Message}s match patterns such as:
 * 
 * <pre>
 * 10.118.101.132 - - [15/Dec/2008:17:00:00 +0000] "POST /statusCheck.do HTTP/1.1" 200 1779 2073
 * 10.117.101.80 - - [15/Dec/2008:17:00:09 +0000] "GET /lock.do?loid=26.0.1112948292&event=unlock&eventId=37234673 HTTP/1.1" 200 - 14
 * ...
 * </pre>
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class MessageFilter implements IMessageFilter<Message> {
	private final Pattern pattern;
	private final Pattern filter;
	private final ITimeInterval timeInterval;

	/**
	 * The date format to expect from the log entries to be filtered.
	 */
	private final ThreadLocal<DateFormat> DATE_FORMATTER = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(MESSAGE_DATE_FORMAT);
		}
	};
	private static final String MESSAGE_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final String MESSAGE_REGEX_PATTERN = "\\s-\\s-\\s\\[(.*)\\](.*)\\s.*[GETPOST]\\s/(.*)\\sHTTP.*\\s((\\d+$))";

	public MessageFilter() {
		this(new InfiniteTimeInterval());
	}
	
	public MessageFilter(final String filterPattern) {
		this(new InfiniteTimeInterval(), filterPattern);
	}
	
	public MessageFilter(final ITimeInterval timeInterval) {
		this(timeInterval, ".*");
	}

	public MessageFilter(final ITimeInterval timeInterval, final String filterPattern) {
		Preconditions.checkNotNull(timeInterval);
		Preconditions.checkNotNull(filterPattern);
		pattern = Pattern.compile(MESSAGE_REGEX_PATTERN);
		this.timeInterval = timeInterval;
		this.filter = Pattern.compile(filterPattern);
	}

	public Message parse(final String text) {
		Matcher m = pattern.matcher(text);
		if (m.find()) {
			Date date = getDateFromString(m.group(1));
			String url = cleanUpUrl(m.group(3));
			if (timeInterval.isBetweenInstants(date) && filter.matcher(url).matches()) {
				return new Message(text, date, url, m.group(4));
			}
		}
		return null;
	}

	private Date getDateFromString(final String dateTime) {
		Date date;
		try {
			date = DATE_FORMATTER.get().parse(dateTime);
		} catch (ParseException pe) {
			// If the date format is wrong, fail quickly
			throw new IllegalArgumentException(
					String.format("Unable to parse the date for String :%s\n Expected format is: %s", dateTime, MESSAGE_DATE_FORMAT), pe);
		}
		return date;
	}

	private String cleanUpUrl(String url) {
		// Only need the path, not the queryString
		String[] split = url.split("\\?");
		url = split[0];
		split = url.split("/");
		return split[split.length - 1];
	}

	public Pattern getPattern() {
		return pattern;
	}
}