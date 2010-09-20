package org.logparser;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.logparser.config.Config;
import org.logparser.stats.HourStats;

import com.google.common.base.Preconditions;

/**
 * Represents a log file snapshot, containing filtered log {@code E}ntries and
 * log summaries.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "totalEntries", "dayStats", "hourStats" })
public final class LogSnapshot<E extends ITimestampedEntry> implements IJsonSerializable<LogSnapshot<E>>, ICsvSerializable<LogSnapshot<E>>, IObserver<E> {
	private final DecimalFormat decimalFormat;
	private final List<E> filteredEntries;
	private final HourStats<E> hourStats;
	private transient final ObjectMapper jsonMapper;
	private int totalEntries;
	private final boolean filteredEntriesStored;

	public LogSnapshot(final Config config) {
		Preconditions.checkNotNull(config);
		this.filteredEntries = new ArrayList<E>();
		this.hourStats = new HourStats<E>();
		this.filteredEntriesStored = config.isFilteredEntriesStored();
		this.jsonMapper = new ObjectMapper();
		this.decimalFormat = new DecimalFormat("####.##%");
		this.totalEntries = 0;
	}

	public void consume(final E entry) {
		totalEntries++;
		if (entry != null) {
			// avoid the overhead of storing the filtered entries if dealing with large datasets
			if (filteredEntriesStored) {
				filteredEntries.add(entry);
			}
			hourStats.consume(entry);
		}
	}

	public HourStats<E> getHourStats() {
		return hourStats;
	}

	public List<E> getFilteredEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	@Override
	public String toString() {
		return "";
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
		StringBuilder sb = new StringBuilder("DAILY BREAKDOWN");
		sb.append(LINE_SEPARATOR);
		sb.append(LINE_SEPARATOR);
		sb.append(LINE_SEPARATOR);
		sb.append("HOURLY BREAKDOWN");
		sb.append(LINE_SEPARATOR);
		sb.append(hourStats.toCsvString());
		return sb.toString();
	}

	private String asPercentOf(final int value, final int total) {
		return asPercentOf(value, total, decimalFormat);
	}

	private String asPercentOf(final int value, final int total, final DecimalFormat df) {
		double percent = value > 0 ? value / (double) total : 0D;
		return df.format(percent);
	}

	private <K> String summarizeAsString(final Map<K, Integer> summary, final int filteredEntries, final int totalEntries, final String formatString) {
		int value = 0;
		StringBuilder sb = new StringBuilder();
		for (Entry<K, Integer> entries : summary.entrySet()) {
			value = entries.getValue();
			sb.append(String.format(formatString, 
					entries.getKey(),
					entries.getValue(), 
					asPercentOf(value, filteredEntries),
					asPercentOf(value, totalEntries)));
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	public LogSnapshot<E> fromCsvString(String csvString) {
		throw new NotImplementedException("LogSnapshot does not implement CSV deserialization.");
	}

	public LogSnapshot<E> fromJsonString(String jsonString) {
		throw new NotImplementedException("LogSnapshot does not implement JSON deserialization.");
	}
}
