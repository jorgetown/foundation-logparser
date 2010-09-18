package org.logparser.stats;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
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
public class HourStats<E extends ITimestampedEntry> extends AbstractStats<E> implements ICsvSerializable<HourStats<E>>, IJsonSerializable<HourStats<E>> {
	private static final long serialVersionUID = -6956010383538378498L;
	private final Map<String, Map<Integer, TimeStats<E>>> hourStats;
	private final Calendar calendar;
	private transient final ObjectMapper jsonMapper;

	public HourStats() {
		hourStats = new TreeMap<String, Map<Integer, TimeStats<E>>>();
		calendar = Calendar.getInstance();
		jsonMapper = new ObjectMapper();
	}

	@Override
	public void add(final E newEntry) {
		Preconditions.checkNotNull(newEntry);

		String key = newEntry.getAction();
		Map<Integer, TimeStats<E>> dayStatsByKey = getNewOrExistingDayStats(key);

		calendar.setTimeInMillis(newEntry.getTimestamp());
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

		TimeStats<E> hourlyStats = getNewOrExistingHourStats(dayStatsByKey, dayOfMonth);

		hourlyStats.add(newEntry);
		dayStatsByKey.put(dayOfMonth, hourlyStats);
		hourStats.put(key, dayStatsByKey);
	}

	private Map<Integer, TimeStats<E>> getNewOrExistingDayStats(final String key) {
		Map<Integer, TimeStats<E>> dayStats = null;
		if (hourStats.containsKey(key)) {
			dayStats = hourStats.get(key);
		} else {
			dayStats = new TreeMap<Integer, TimeStats<E>>();
		}
		return dayStats;
	}

	private TimeStats<E> getNewOrExistingHourStats(Map<Integer, TimeStats<E>> dayStatsByKey, final int dayOfMonth) {
		TimeStats<E> hourlyStats = null;
		if (dayStatsByKey.containsKey(dayOfMonth)) {
			hourlyStats = dayStatsByKey.get(dayOfMonth);
		} else {
			hourlyStats = new TimeStats<E>(Calendar.HOUR_OF_DAY);
		}
		return hourlyStats;
	}

	@JsonIgnore
	public Map<Integer, TimeStats<E>> getTimeStats(final String key) {
		return hourStats.get(key);
	}

	public Map<String, Map<Integer, TimeStats<E>>> getHourStats() {
		return Collections.unmodifiableMap(hourStats);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : hourStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
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
		boolean header = true;
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : hourStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			sb.append(StringEscapeUtils.escapeCsv(entries.getKey()));
			sb.append(LINE_SEPARATOR);

			for (Entry<Integer, TimeStats<E>> values : entries.getValue().entrySet()) {
				if (header) {
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
					header = false;
				}
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
			header = true;
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

	public HourStats<E> fromJsonString(final String jsonString) {
		throw new NotImplementedException("HourStats does not implement JSON deserialization.");
	}
}
