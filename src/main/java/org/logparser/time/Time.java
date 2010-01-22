package org.logparser.time;

import net.jcip.annotations.Immutable;

/**
 * Represents a time instant as an hh:mm pair.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class Time {
	private final int hour;
	private final int minute;

	public Time(final int hour, final int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	public Time(final String hour, final String minute) {
		this(Integer.valueOf(hour), Integer.valueOf(minute));
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	@Override
	public String toString() {
		return hour + ":" + minute;
	}
}
