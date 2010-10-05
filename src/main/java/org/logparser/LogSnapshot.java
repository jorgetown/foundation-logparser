package org.logparser;

import static org.logparser.Constants.CSV_VALUE_SEPARATOR;
import static org.logparser.Constants.DEFAULT_DECIMAL_FORMAT;
import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;

/**
 * Represents a log file snapshot, containing log entries and summary.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "storeFilteredEntries", "size", "summary", "filteredEntries" })
public final class LogSnapshot<E extends ITimestampedEntry> implements IJsonSerializable<LogSnapshot<E>>, ICsvSerializable<LogSnapshot<E>>, IObserver<E> {
	private static final long serialVersionUID = 4389255038622214430L;
	private final List<E> filteredEntries;
	private final Map<String, Integer> summary;
	private transient final ObjectMapper jsonMapper;
	private int size;
	private final boolean storeFilteredEntries;
	private final DecimalFormat df;
	private volatile int hashCode;

	public LogSnapshot() {
		this(true, new DecimalFormat(DEFAULT_DECIMAL_FORMAT));
	}

	public LogSnapshot(final boolean storeFilteredEntries, final DecimalFormat decimalFormat) {
		this.filteredEntries = new ArrayList<E>();
		this.summary = new HashMap<String, Integer>();
		this.storeFilteredEntries = storeFilteredEntries;
		this.jsonMapper = new ObjectMapper();
		this.size = 0;
		this.df = Preconditions.checkNotNull(decimalFormat, "'decimalFormat' argument cannot be null.");
	}

	public void consume(final E entry) {
		if (entry != null) {
			// don't store the filtered entries if it's a large dataset
			if (storeFilteredEntries) {
				filteredEntries.add(entry);
			}
			size++;
			updateUnivariateSummary(entry);
		}
	}

	private void updateUnivariateSummary(final E entry) {
		String key = entry.getAction();
		if (summary.containsKey(key)) {
			Integer value = summary.get(key);
			value++;
			summary.put(key, value);
		} else {
			summary.put(key, 1);
		}
	}

	public boolean isStoreFilteredEntries() {
		return storeFilteredEntries;
	}

	public List<E> getFilteredEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	public Map<String, Integer> getSummary() {
		return Collections.unmodifiableMap(summary);
	}

	public int getSize() {
		return size;
	}

	public DecimalFormat getDecimalFormat() {
		return df;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Action,\t # Entries,\t % Distribution\t");
		if (!summary.isEmpty()) {
			sb.append(LINE_SEPARATOR);
			int value = 0;
			for (Entry<String, Integer> entries : summary.entrySet()) {
				value = entries.getValue();
				sb.append(String.format("%s,\t %s,\t %s\t", entries.getKey(), value, asPercentOf(value, size)));
				sb.append(LINE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	public String toJsonString() {
		try {
			return jsonMapper.writeValueAsString(this);
			// TODO proper exception handling
		} catch (JsonGenerationException jge) {
		} catch (JsonMappingException jme) {
		} catch (IOException ioe) {
		}
		return null;
	}

	public String toCsvString() {
		StringBuilder sb = new StringBuilder("Action, # Entries, % Distribution");
		if (!summary.isEmpty()) {
			sb.append(LINE_SEPARATOR);
			int value = 0;
			for (Entry<String, Integer> entries : summary.entrySet()) {
				value = entries.getValue();
				sb.append(StringEscapeUtils.escapeCsv(entries.getKey()));
				sb.append(CSV_VALUE_SEPARATOR);
				sb.append(value);
				sb.append(CSV_VALUE_SEPARATOR);
				sb.append(StringEscapeUtils.escapeCsv(asPercentOf(value, size)));
				sb.append(LINE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	private String asPercentOf(final int value, final int total) {
		return asPercentOf(value, total, df);
	}

	private String asPercentOf(final int value, final int total, final DecimalFormat df) {
		double percent = value > 0 ? value / (double) total : 0D;
		return df.format(percent * 100);
	}

	public LogSnapshot<E> fromCsvString(String csvString) {
		throw new NotImplementedException("LogSnapshot does not implement CSV deserialization.");
	}

	public LogSnapshot<E> fromJsonString(String jsonString) {
		throw new NotImplementedException("LogSnapshot does not implement JSON deserialization.");
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof LogSnapshot))
			return false;
		final LogSnapshot<?> snapshot = (LogSnapshot<?>) other;
		return (size == snapshot.size) && (storeFilteredEntries == snapshot.storeFilteredEntries)
				&& (summary == null ? snapshot.summary == null : summary.equals(snapshot.summary));
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = 17;
			result = 31 * result + size;
			result = 31 * result + (storeFilteredEntries ? 1 : 0);
			result = 31 * result + (summary == null ? 0 : summary.hashCode());
			hashCode = result;
		}
		return result;
	}
}
