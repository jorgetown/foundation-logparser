package org.logparser.stats;

import static org.logparser.Constants.DEFAULT_DECIMAL_FORMAT;
import static org.logparser.Constants.LINE_SEPARATOR;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.logparser.ITimestampedEntry;

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
@JsonPropertyOrder({ "minuteStats" })
public class MinuteStats<E extends ITimestampedEntry> extends HourStats<E> {
	private static final long serialVersionUID = -2775546286837777666L;

	public MinuteStats() {
		this(false, new DecimalFormat(DEFAULT_DECIMAL_FORMAT));
	}

	public MinuteStats(final boolean detailed, final DecimalFormat decimalFormat) {
		super(detailed, decimalFormat);
	}

	@Override
	protected TimeStats<E> getNewOrExistingHourStats(Map<Integer, TimeStats<E>> dayStatsByKey, final int dayOfMonth) {
		TimeStats<E> minuteStats = null;
		if (dayStatsByKey.containsKey(dayOfMonth)) {
			minuteStats = dayStatsByKey.get(dayOfMonth);
		} else {
			minuteStats = new TimeStats<E>(Calendar.MINUTE);
		}
		return minuteStats;
	}

	public Map<String, Map<Integer, TimeStats<E>>> getMinuteStats() {
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
			sb.append(String.format(" %s minutes, ~%s/minute, ~%sms", tuple.count, tuple.avgCount, df.format(tuple.avgTime)));
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	public String toCsvString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, Map<Integer, TimeStats<E>>> entries : timeStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			Tuple tuple = calculateSummary(entries.getValue());
			sb.append(StringEscapeUtils.escapeCsv(entries.getKey()));
			sb.append(",");
			sb.append(String.format(" %s mins, avg %s/min, avg %sms/min",
					tuple.count,
					tuple.avgCount,
					StringEscapeUtils.escapeCsv(df.format(tuple.avgTime))));
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}
}
