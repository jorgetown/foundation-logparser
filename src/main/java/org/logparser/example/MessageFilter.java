package org.logparser.example;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.logparser.ILogSummaryFilter;
import org.logparser.IMessageFilter;
import org.logparser.LogSnapshot;
import org.logparser.Preconditions;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Message filter implementation to extract {@link Message}s from the example
 * log file. The {@link Message}s match patterns such as:
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
public class MessageFilter implements IMessageFilter<Message>, ILogSummaryFilter {
	private final Pattern pattern;
	
	private final Pattern timestampPattern;
	private final Pattern actionPattern;
	private final Pattern durationPattern;
	
	private final Pattern filter;
	private final ITimeInterval timeInterval;
	private final Map<String, Integer> summary;

	private final ThreadLocal<DateFormat> DATE_FORMATTER = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(MESSAGE_DATE_FORMAT);
		}
	};
	private static final String MESSAGE_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final String MESSAGE_REGEX_PATTERN = "\\[(.*)\\](?:.*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))(?:.*)?\\sHTTP.*\\s((\\d)(.*))";
	
	private static final String MESSAGE_REGEX_TIMESTAMP_PATTERN = "\\[((.+?))\\]";
	private static final String MESSAGE_REGEX_ACTION_PATTERN = "\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))";
	private static final String MESSAGE_REGEX_DURATION_PATTERN = "HTTP.*\\s((\\d)(.*))";
	
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
		timestampPattern = Pattern.compile(MESSAGE_REGEX_TIMESTAMP_PATTERN);
		actionPattern = Pattern.compile(MESSAGE_REGEX_ACTION_PATTERN);
		durationPattern = Pattern.compile(MESSAGE_REGEX_DURATION_PATTERN);
		this.timeInterval = timeInterval;
		this.filter = Pattern.compile(filterPattern);
		this.summary = new HashMap<String, Integer>();
	}

	public Message parse2(final String text) {
		Matcher m = pattern.matcher(text);
		if (m.find()) {
			Date date = getDateFromString(m.group(1));
			String url = m.group(2);
			if (filter.matcher(url).matches()) {
				updateSummary(url);
			}
			if (timeInterval.isBetweenInstants(date) && filter.matcher(url).matches()) {
				return new Message(text, date, url, m.group(5));
			}
		}
		return null;
	}
	
	public Message parse(final String text) {
		Date date = null;
		String action = null;
		String duration = null;
		
//		long start = System.nanoTime();
		Matcher m = timestampPattern.matcher(text);
//		long end = (System.nanoTime() - start);
//		System.out.println(String.format("Ellapsed = %sns\n", end));
		
		if (m.find()) {
//			System.out.println(String.format("\nTIMESTAMP MATCHER\n"));
//			for (int i = 0; i < m.groupCount(); i++) {
//			System.out.println(String.format("Group # = %d, Regex = %s", i, m.group(i)));
//			}
			date = getDateFromString(m.group(1));
			m = actionPattern.matcher(text);
			
			if (m.find()) {
//				System.out.println(String.format("\nACTION MATCHER\n"));
//				for (int i = 0; i < m.groupCount(); i++) {
//				System.out.println(String.format("Group # = %d, Regex = %s", i, m.group(i)));
//				}
				action = m.group(1);
				if (filter.matcher(action).matches()) {
					updateSummary(action);
				}
				
				long start = System.nanoTime();
				m = durationPattern.matcher(text);
				long end = (System.nanoTime() - start);
				System.out.println(String.format("Ellapsed = %sns\n", end));
				
				
				if (m.find()) {
//					for (int i = 0; i < m.groupCount(); i++) {
//					System.out.println(String.format("\nDURATION MATCHER\n"));
//					System.out.println(String.format("Group # = %d, Regex = %s", i, m.group(i)));
//					}
					duration = m.group(1);
					if (timeInterval.isBetweenInstants(date) && filter.matcher(action).matches()) {
						return new Message(text, date, action, duration);
					}	
				}
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

	private void updateSummary(final String key) {
		if (summary.containsKey(key)) {
			Integer value = summary.get(key);
			value++;
			summary.put(key, value);
		} else {
			summary.put(key, 1);
		}
	}

	public Pattern getPattern() {
		return pattern;
	}

	public Map<String, Integer> getSummary() {
		return Collections.unmodifiableMap(summary);
	}

	public void reset() {
		summary.clear();
	}
	
	public static void main(String args[]) {
		MessageFilter mf = new MessageFilter();
		String logEntry = "10.117.101.80 - - [15/Dec/2008:00:00:15 +0000] \"GET /context/path/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14";
		mf.parse(logEntry);
	}
}