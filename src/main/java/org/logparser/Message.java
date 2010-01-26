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
public class Message implements Serializable {
	private static final long serialVersionUID = -1019020702743392905L;
	private final Date date;
	private final String url;
	private final String milliseconds;
	private final String message;

	public Message(final String message, final Date date, final String url, final String milliseconds) {
		this.message = message;
		this.url = url;
		this.milliseconds = milliseconds;
		this.date = date;
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
		return date;
	}

	@Override
	public String toString() {
		return String.format("{[%s],[%s], %s, %sms}", message, date, url, milliseconds);
	}
}