package org.logparser.stats;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.logparser.ICsvSerializable;
import org.logparser.IJsonSerializable;
import org.logparser.ITimestampedEntry;

import com.google.common.base.Preconditions;

/**
 * Provides a statistical summary of a collection of log
 * {@link ITimestampedEntry}s, grouped by the day of the month and keyed by
 * {@link ITimestampedEntry#getAction()}.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "dayStats" })
public class DayStats<E extends ITimestampedEntry> implements Serializable, ICsvSerializable<DayStats<E>>, IJsonSerializable<DayStats<E>> {
	private static final long serialVersionUID = 6551391859868552192L;
	private final Map<String, TimeStats<E>> dayStats;
	private final ObjectMapper jsonMapper;

	public DayStats() {
		dayStats = new TreeMap<String, TimeStats<E>>();
		jsonMapper = new ObjectMapper();
	}

	public void add(final E newEntry) {
		Preconditions.checkNotNull(newEntry);
		String key = newEntry.getAction();
		TimeStats<E> timeStats = null;
		if (dayStats.containsKey(key)) {
			timeStats = dayStats.get(key);
		} else {
			timeStats = new TimeStats<E>();
		}
		timeStats.add(newEntry);
		dayStats.put(key, timeStats);
	}
	
	public void addAll(final List<E> logEntries) {
		Preconditions.checkNotNull(logEntries);
		for (E entry : logEntries) {
			add(entry);
		}
	}
	
	@JsonIgnore
	public TimeStats<E> getTimeStats(final String key) {
		if (dayStats.containsKey(key)) {
			TimeStats<E> timeStats = dayStats.get(key);
			return timeStats;
		}
		return null;
	}

	public Map<String, TimeStats<E>> getDayStats() {
		return Collections.unmodifiableMap(dayStats);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, TimeStats<E>> entries : dayStats.entrySet()) {
			sb.append(entries.getKey());
			sb.append(LINE_SEPARATOR);
			sb.append("\tDay, \t#, \tMean, \tStandard Deviation, \tMax, \tMin");
			for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
				sb.append(LINE_SEPARATOR);
				StatisticalSummary summary = timeStats.getValue();
				sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s, \t%s",
						timeStats.getKey(), 
						summary.getN(), 
						summary.getMean(),
						summary.getStandardDeviation(), 
						summary.getMax(),
						summary.getMin()));
			}
			sb.append(LINE_SEPARATOR);
		}

		return sb.toString();
	}

	public String toCsvString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, TimeStats<E>> entries : dayStats.entrySet()) {
			sb.append(StringEscapeUtils.escapeCsv(entries.getKey()));
			sb.append(LINE_SEPARATOR);
			sb.append(", Day, #, Mean, Standard Deviation, Max, Min");
			for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
				sb.append(LINE_SEPARATOR);
				StatisticalSummary summary = timeStats.getValue();
				sb.append(String.format(", %s, %s, %s, %s, %s, %s", 
						timeStats.getKey(), 
						summary.getN(), 
						StringEscapeUtils.escapeCsv(Double.toString(summary.getMean())),
						StringEscapeUtils.escapeCsv(Double.toString(summary.getStandardDeviation())), 
						StringEscapeUtils.escapeCsv(Double.toString(summary.getMax())),
						StringEscapeUtils.escapeCsv(Double.toString(summary.getMin()))));
			}
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	public DayStats<E> fromCsvString(final String csvString) {
		throw new NotImplementedException("DayStats does not implement CSV deserialization.");
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

	public DayStats<E> fromJsonString(String jsonString) {
		throw new NotImplementedException("DayStats does not implement JSON deserialization.");
	}
}
