package org.logparser;

import java.io.Serializable;
import java.util.Date;

import net.jcip.annotations.Immutable;

/**
 * Represents a parsed log entry.
 * 
 * @author jorge.decastro
 */
@Immutable
public final class Message implements Serializable, ITimeComparable {
	private static final long serialVersionUID = -1019020702743392905L;
	// TODO refactor to simplify by using date as a long
	private final Date date;
	private final String url;
	private final String milliseconds;
	private final String message;
	private volatile int hashCode;

	public Message(final String message, final Date date, final String url, final String milliseconds) {
		// defensive copy since {@link Date}s are not immutable
		this.date = new Date(date.getTime());
		this.message = message;
		this.url = url;
		this.milliseconds = milliseconds;
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
		if (!(other instanceof Message))
			return false;
		final Message m = (Message) other;
		return (date == null ? m.date == null : date.equals(m.date))
				&& (milliseconds == null ? m.milliseconds == null : milliseconds.equals(m.milliseconds))
				&& (url == null ? m.url == null : url.equals(m.url))
				&& (message == null ? m.message == null : message.equals(m.message));
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
		return String.format("{[%s],[%s], %s, %sms}", message, date, url, milliseconds);
	}

	public String toCsvString() {
		return String.format("\"%s\", \"%s\", \"%s\"", date, url, milliseconds);
	}
}