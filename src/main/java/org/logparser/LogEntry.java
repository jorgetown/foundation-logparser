package org.logparser;

import java.io.Serializable;
import java.util.Date;

import net.jcip.annotations.Immutable;

/**
 * Represents a single log entry.
 * 
 * @author jorge.decastro
 */
@Immutable
public class LogEntry implements Serializable, ITimestampedEntry {
	private static final long serialVersionUID = -1019020702743392905L;
	// TODO refactor to simplify by using date as a long
	private final Date date;
	private final String url;
	private final String milliseconds;
	private final String message;
	private final long timestamp;
	private volatile int hashCode;

	public LogEntry(final String message, final Date date, final String url, final String milliseconds) {
		// defensive copy since {@link Date}s are not immutable
		this.date = new Date(date.getTime());
		this.message = message;
		this.url = url;
		this.milliseconds = milliseconds;
		this.timestamp = date.getTime();
	}

	public String getMessage() {
		return message;
	}

	public String getUrl() {
		return url;
	}

	public String getMilliseconds() {
		return milliseconds;
	}

	public Date getDate() {
		// defensive copy since {@link Date}s are not immutable
		return new Date(date.getTime());
	}

	public long getTime() {
		return date.getTime();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof LogEntry))
			return false;
		final LogEntry entry = (LogEntry) other;
		return (date == null ? entry.date == null : date.equals(entry.date))
				&& (milliseconds == null ? entry.milliseconds == null : milliseconds.equals(entry.milliseconds))
				&& (url == null ? entry.url == null : url.equals(entry.url))
				&& (message == null ? entry.message == null : message.equals(entry.message));
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = 17;
			result = 31 * result + date.hashCode();
			result = 31 * result + milliseconds.hashCode();
			result = 31 * result + url.hashCode();
			result = 31 * result + message.hashCode();
			hashCode = result;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format("{%s; %s; %s; %sms}", message.replaceAll("\"", ""), date, url, milliseconds);
	}

	public String toCsvString() {
		return String.format("\"%s\", \"%s\", %s", date, url, Double.valueOf(milliseconds));
	}

	public String getAction() {
		return url;
	}

	public double getDuration() {
		return Double.valueOf(milliseconds);
	}

	public long getTimestamp() {
		return timestamp;
	}
}