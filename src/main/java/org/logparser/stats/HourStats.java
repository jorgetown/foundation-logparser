package org.logparser.stats;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
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
 * Provides a statistical summary of a keyed collection of log
 * {@link ITimestampedEntry}s. Typically, the collection of log
 * {@link ITimestampedEntry}s is keyed by their
 * {@link ITimestampedEntry#getAction()}.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "hourStats" })
public class HourStats<E extends ITimestampedEntry> implements Serializable, ICsvSerializable<HourStats<E>>, IJsonSerializable<HourStats<E>> {
	private static final long serialVersionUID = -6956010383538378498L;
	private final Map<String, Map<Integer, TimeStats<E>>> hourStats;
	private final Calendar calendar;
	private final ObjectMapper jsonMapper;

	public HourStats() {
		hourStats = new TreeMap<String, Map<Integer, TimeStats<E>>>();
		calendar = Calendar.getInstance();
		jsonMapper = new ObjectMapper();
	}

	public void add(final E newEntry) {
		Preconditions.checkNotNull(newEntry);

		String key = newEntry.getAction();
		Map<Integer, TimeStats<E>> timeStatsByKey = null;
		if (hourStats.containsKey(key)) {
			timeStatsByKey = hourStats.get(key);
		} else {
			timeStatsByKey = new TreeMap<Integer, TimeStats<E>>();
		}

		calendar.setTimeInMillis(newEntry.getTimestamp());
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

		TimeStats<E> hourlyStats = null;
		if (timeStatsByKey.containsKey(dayOfMonth)) {
			hourlyStats = timeStatsByKey.get(dayOfMonth);
		} else {
			hourlyStats = new TimeStats<E>(Calendar.HOUR_OF_DAY);
		}

		hourlyStats.add(newEntry);
		timeStatsByKey.put(dayOfMonth, hourlyStats);
		hourStats.put(key, timeStatsByKey);
	}
	
	public void addAll(final List<E> logEntries) {
		Preconditions.checkNotNull(logEntries);
		for (E entry : logEntries) {
			add(entry);
		}
	}
	
	@JsonIgnore
	public Map<Integer, TimeStats<E>> getTimeStats(final String key) {
		if (hourStats.containsKey(key)) {
			Map<Integer, TimeStats<E>> timeStats = hourStats.get(key);
			return timeStats;
		}
		return null;
	}

	public Map<String, Map<Integer, TimeStats<E>>> getHourStats() {
		return Collections.unmodifiableMap(hourStats);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : hourStats.entrySet()) {
			sb.append(entries.getKey());
			sb.append(LINE_SEPARATOR);

			for (Entry<Integer, TimeStats<E>> values : entries.getValue().entrySet()) {
				sb.append("\tDay, ");
				for (Integer i : values.getValue().getTimeStats().keySet()) {
					sb.append("\t#, \tMean, \tStandard Deviation, \tMax, \tMin\t");
				}
				sb.append(LINE_SEPARATOR);
				sb.append("\t");
				sb.append(values.getKey());
				sb.append(",");
				for (Entry<Integer, StatisticalSummary> stats : values.getValue().getTimeStats().entrySet()) {
					StatisticalSummary summary = stats.getValue();
					sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s\t",
							summary.getN(), 
							summary.getMean(),
							summary.getStandardDeviation(), 
							summary.getMax(),
							summary.getMin()));
				}
				sb.append(LINE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	public String toCsvString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : hourStats.entrySet()) {
			sb.append(StringEscapeUtils.escapeCsv(entries.getKey()));
			sb.append(LINE_SEPARATOR);

			for (Entry<Integer, TimeStats<E>> values : entries.getValue().entrySet()) {
				for (Integer i : values.getValue().getTimeStats().keySet()) {
					sb.append(", , , , ");
					sb.append(i);
					sb.append(", , ");
				}
				sb.append(LINE_SEPARATOR);

				sb.append(", Day, ");
				for (Integer i : values.getValue().getTimeStats().keySet()) {
					sb.append("#, Mean, Standard Deviation, Max, Min, , ");
				}
				sb.append(LINE_SEPARATOR);
				sb.append(",");
				sb.append(values.getKey());
				sb.append(",");
				for (Entry<Integer, StatisticalSummary> stats : values.getValue().getTimeStats().entrySet()) {
					StatisticalSummary summary = stats.getValue();
					sb.append(String.format("%s, %s, %s, %s, %s, ,", 
							summary.getN(), 
							StringEscapeUtils.escapeCsv(Double.toString(summary.getMean())), 
							StringEscapeUtils.escapeCsv(Double.toString(summary.getStandardDeviation())),
							StringEscapeUtils.escapeCsv(Double.toString(summary.getMax())),
							StringEscapeUtils.escapeCsv(Double.toString(summary.getMin()))));
				}
				sb.append(LINE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	public HourStats<E> fromCsvString(final String csvString) {
		throw new NotImplementedException("HourStats does not implement CSV deserialization.");
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

	public HourStats<E> fromJsonString(String jsonString) {
		throw new NotImplementedException("HourStats does not implement JSON deserialization.");
	}
}
