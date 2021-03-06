package org.logparser.stats;

import static org.logparser.Constants.DEFAULT_DECIMAL_FORMAT;
import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
@JsonPropertyOrder({ "hourStats" })
public class HourStats<E extends ITimestampedEntry> extends AbstractStats<E> implements ICsvSerializable<HourStats<E>>, IJsonSerializable<HourStats<E>> {
	private static final long serialVersionUID = -6956010383538378498L;
	protected final Map<String, Map<Integer, TimeStats<E>>> timeStats;
	private final Calendar calendar;
	private transient final ObjectMapper jsonMapper;
	protected final DecimalFormat df;
	private final boolean detailed;

	public HourStats() {
		this(false, new DecimalFormat(DEFAULT_DECIMAL_FORMAT));
	}

	public HourStats(final boolean detailed, final DecimalFormat decimalFormat) {
		timeStats = new TreeMap<String, Map<Integer, TimeStats<E>>>();
		calendar = Calendar.getInstance();
		jsonMapper = new ObjectMapper();
		df = Preconditions.checkNotNull(decimalFormat, "'decimalFormat' argument cannot be null.");
		this.detailed = false; // there's too much detail on the minute view!
	}

	@Override
	public void consume(final E newEntry) {
		Preconditions.checkNotNull(newEntry);

		String key = newEntry.getAction();
		Map<Integer, TimeStats<E>> dayStatsByKey = getNewOrExistingDayStats(key);

		calendar.setTimeInMillis(newEntry.getTimestamp());
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

		TimeStats<E> hourlyStats = getNewOrExistingHourStats(dayStatsByKey, dayOfMonth);

		hourlyStats.consume(newEntry);
		dayStatsByKey.put(dayOfMonth, hourlyStats);
		timeStats.put(key, dayStatsByKey);
	}

	private Map<Integer, TimeStats<E>> getNewOrExistingDayStats(final String key) {
		Map<Integer, TimeStats<E>> dayStats = null;
		if (timeStats.containsKey(key)) {
			dayStats = timeStats.get(key);
		} else {
			dayStats = new TreeMap<Integer, TimeStats<E>>();
		}
		return dayStats;
	}

	protected TimeStats<E> getNewOrExistingHourStats(Map<Integer, TimeStats<E>> dayStatsByKey, final int dayOfMonth) {
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
		return timeStats.get(key);
	}

	public Map<String, Map<Integer, TimeStats<E>>> getHourStats() {
		return Collections.unmodifiableMap(timeStats);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : timeStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			Tuple tuple = calculateSummary(entries.getValue());
			sb.append(entries.getKey());
			sb.append(",");
			sb.append(String.format(" %s hours, ~%s/hour, ~%sms", tuple.count, tuple.avgCount, df.format(tuple.avgTime)));
			sb.append(LINE_SEPARATOR);
			if (detailed) {
				writeColumns(sb, entries);
			}
		}
		return sb.toString();
	}

	private void writeColumns(StringBuilder sb, final Entry<String, Map<Integer, TimeStats<E>>> entries) {
		for (Entry<Integer, TimeStats<E>> values : entries.getValue().entrySet()) {
			sb.append("\tDate, ");
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
						Double.valueOf(df.format(summary.getMean())),
						Double.valueOf(df.format(summary.getStandardDeviation())),
						Double.valueOf(df.format(summary.getMax())),
						Double.valueOf(df.format(summary.getMin()))));
			}
			sb.append(LINE_SEPARATOR);
		}
	}

	public String toCsvString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		boolean header = true;
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : timeStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			Tuple tuple = calculateSummary(entries.getValue());
			sb.append(StringEscapeUtils.escapeCsv(entries.getKey()));
			sb.append(",");
			sb.append(String.format(" %s hours, avg %s/hour, avg %sms", tuple.count, tuple.avgCount, StringEscapeUtils.escapeCsv(df.format(tuple.avgTime))));
			sb.append(LINE_SEPARATOR);
			
			if (detailed) {
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
					writeCsvColumns(sb, values);
					sb.append(LINE_SEPARATOR);
				}
				header = true;
			}
		}
		return sb.toString();
	}

	private void writeCsvColumns(StringBuilder sb, final Entry<Integer, TimeStats<E>> values) {
		for (Entry<Integer, StatisticalSummary> stats : values.getValue().getTimeStats().entrySet()) {
			StatisticalSummary summary = stats.getValue();
			sb.append(String.format("%s, %s, %s, %s, %s, ,",
					summary.getN(),
					StringEscapeUtils.escapeCsv(df.format(summary.getMean())),
					StringEscapeUtils.escapeCsv(df.format(summary.getStandardDeviation())),
					StringEscapeUtils.escapeCsv(df.format(summary.getMax())),
					StringEscapeUtils.escapeCsv(df.format(summary.getMin()))));
		}
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

	protected Tuple calculateSummary(final Map<Integer, TimeStats<E>> hourStats) {
		int count = 0;
		int avgCount = 0;
		double avgTime = 0D;
		for (Entry<Integer, TimeStats<E>> values : hourStats.entrySet()) {
			for (StatisticalSummary stat : values.getValue().getTimeStats().values()) {
				avgCount += stat.getN();
				avgTime += stat.getMean();
				count++;
			}
		}
		avgCount = avgCount > 0 ? Math.round(avgCount / count) : 0;
		avgTime = avgTime > 0 ? avgTime / count : 0;
		return new Tuple(count, avgCount, avgTime);
	}
}
