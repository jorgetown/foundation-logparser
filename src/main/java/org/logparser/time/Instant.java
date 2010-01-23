package org.logparser.time;

import net.jcip.annotations.Immutable;

/**
 * Represents a time instant as an hh:mm pair.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class Instant {
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

	@Override
	public String toString() {
		return hour + ":" + minute;
	}
}
