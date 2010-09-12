package org.logparser;

import static org.logparser.Constants.CSV_VALUE_SEPARATOR;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;

/**
 * Represents a single log entry.
 * 
 * @author jorge.decastro
 */
@Immutable
@JsonPropertyOrder({ "timestamp", "action", "duration" })
public final class LogEntry implements Serializable, ITimestampedEntry, ICsvSerializable<ITimestampedEntry>, IJsonSerializable<ITimestampedEntry> {
	private static final long serialVersionUID = -1019020702743392905L;
	private final long timestamp;
	private final String action;
	private final double duration;
	private volatile int hashCode;

	@JsonCreator
	public LogEntry(@JsonProperty("timestamp") final long timestamp, @JsonProperty("action") final String action, @JsonProperty("duration") final double duration) {
		this.timestamp = timestamp;
		this.action = action;
		this.duration = duration;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getAction() {
		return action;
	}

	public double getDuration() {
		return duration;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof LogEntry))
			return false;
		final LogEntry entry = (LogEntry) other;
		return (timestamp == entry.timestamp)
				&& (action == null ? entry.action == null : action.equals(entry.action))
				&& (Double.doubleToLongBits(duration) == Double.doubleToLongBits(entry.duration));
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = 17;
			result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
			result = 31 * result + (action == null ? 0 : action.hashCode());
			long longDuration = Double.doubleToLongBits(duration);
			result = 31 * result + (int) (longDuration ^ (longDuration >>> 32));
			hashCode = result;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format("{%s, %s, %s}", new Date(timestamp), action, duration);
	}

	public String toCsvString() {
		return String.format("%s, %s, %s", timestamp, StringEscapeUtils.escapeCsv(action), duration);
	}

	public LogEntry fromCsvString(final String csvString) {
		Preconditions.checkNotNull(csvString);
		String[] fields = csvString.split(CSV_VALUE_SEPARATOR);
		if (fields.length == 3) {
			return new LogEntry(Long.parseLong(fields[0].trim()), StringEscapeUtils.unescapeCsv(fields[1].trim()), Double.parseDouble(fields[2].trim()));
		}
		return null;
	}

	public String toJsonString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
			// TODO proper exception handling
		} catch (JsonGenerationException jge) {
		} catch (JsonMappingException jme) {
		} catch (IOException ioe) {
		}
		return null;
	}

	public LogEntry fromJsonString(final String jsonString) {
		Preconditions.checkNotNull(jsonString);
		ObjectMapper mapper = new ObjectMapper();
		LogEntry entry = null;
		try {
			entry = mapper.readValue(jsonString, LogEntry.class);
			// TODO proper exception handling
		} catch (JsonParseException jpe) {
		} catch (JsonMappingException jme) {
		} catch (IOException ioe) {
		}
		return entry;
	}
}
