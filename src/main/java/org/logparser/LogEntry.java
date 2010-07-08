package org.logparser;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import net.jcip.annotations.Immutable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

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
	private final String action;
	private final String duration;
	private final String message;
	private final long timestamp;
	private volatile int hashCode;
	private final ObjectMapper mapper;

	public LogEntry(final String message, final Date date, final String action, final String duration) {
		// defensive copy since {@link Date}s are not immutable
		this.date = new Date(date.getTime());
		this.message = message;
		this.action = action;
		this.duration = duration;
		this.timestamp = date.getTime();
		this.mapper = new ObjectMapper();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public String getAction() {
		return action;
	}

	public double getDuration() {
		return Double.valueOf(duration);
	}

	@JsonIgnore
	public Date getDate() {
		// defensive copy since {@link Date}s are not immutable
		return new Date(date.getTime());
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof LogEntry))
			return false;
		final LogEntry entry = (LogEntry) other;
		return (date == null ? entry.date == null : date.equals(entry.date))
				&& (duration == null ? entry.duration == null : duration.equals(entry.duration))
				&& (action == null ? entry.action == null : action.equals(entry.action))
				&& (message == null ? entry.message == null : message.equals(entry.message));
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = 17;
			result = 31 * result + date.hashCode();
			result = 31 * result + duration.hashCode();
			result = 31 * result + action.hashCode();
			result = 31 * result + message.hashCode();
			hashCode = result;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format("{%s; %s; %s; %s}", message.replaceAll("\"", ""), date, action, duration);
	}

	public String toCsvString() {
		return String.format("\"%s\", \"%s\", %s", date, action, Double.valueOf(duration));
	}

	public String toJsonString() throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(this);
	}
}