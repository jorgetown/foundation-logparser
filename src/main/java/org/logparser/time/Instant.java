package org.logparser.time;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

/**
 * Represents a time instant as an hh:mm pair.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class Instant {
	private static final Pattern TIME_FORMAT = Pattern.compile("(\\d{1,2})\\:((\\d{1,2}))");
	private final int hour;
	private final int minute;

	public Instant(final int hour, final int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	public Instant(final String hour, final String minute) {
		this(Integer.valueOf(hour), Integer.valueOf(minute));
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}
	
	public static Instant valueOf(final String timeString) {
		Matcher m = TIME_FORMAT.matcher(timeString);
		if (m.find()) {
			return new Instant(m.group(1), m.group(2));
		}
		throw new IllegalArgumentException(String.format("Error parsing time instant from argument :%s\n"
						+ "Expected format is: %s", timeString, TIME_FORMAT.pattern()));
	}

	@Override
	public String toString() {
		return hour + ":" + minute;
	}
}
