package org.logparser.filter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.logparser.Message;
import org.logparser.time.ITimeInterval;

/**
 * Message filter implementation to extract {@link Message}s from a log file.
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
	 * The date format to expect from access log entries.
	 */
	private static final String MESSAGE_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final String MESSAGE_REGEX_PATTERN = "\\s-\\s-\\s\\[(.*)\\](.*)\\s.*[GETPOST]\\s/(.*)\\sHTTP.*\\s((\\d+$))";

	/**
	 * Access log date format.
	 */
	private static final DateFormat MESSAGE_DATE_FORMATTER = new SimpleDateFormat(MESSAGE_DATE_FORMAT);

	public MessageFilter(final ITimeInterval timeInterval) {
		this(timeInterval, ".*");
	}

	public MessageFilter(final ITimeInterval timeInterval, final String filterPattern) {
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

	private Date getDateFromString(String dateTime) {
		Date date;
		try {
			date = MESSAGE_DATE_FORMATTER.parse(dateTime);
		} catch (ParseException pe) {
			// If the date format is wrong, fail quickly
			throw new IllegalArgumentException(
					String.format("Unable to parse the date for String :%s\n Expected format is: %s",
									dateTime, MESSAGE_DATE_FORMAT), pe);
		}
		return date;
	}

	private String cleanUpUrl(String url) {
		// Only need the path, not the queryString
		String[] splitOnQuestionMark = url.split("\\?");
		String path = splitOnQuestionMark[0];
		String[] splitOnPathSeparator = path.split("/");
		return splitOnPathSeparator[splitOnPathSeparator.length - 1];
	}
	
	public Pattern getPattern(){
		return pattern;
	}
}