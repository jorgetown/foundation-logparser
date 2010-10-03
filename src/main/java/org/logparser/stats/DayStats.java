package org.logparser.stats;

import static org.logparser.Constants.DEFAULT_DECIMAL_FORMAT;
import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.logparser.ICsvSerializable;
import org.logparser.IJsonSerializable;
import org.logparser.ITimestampedEntry;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Provides a statistical summary of a collection of log
 * {@link ITimestampedEntry}s, grouped by the day of the month and keyed by
 * {@link ITimestampedEntry#getAction()}.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@JsonPropertyOrder({ "dayStats" })
public class DayStats<E extends ITimestampedEntry> extends AbstractStats<E> implements ICsvSerializable<DayStats<E>>, IJsonSerializable<DayStats<E>> {
	private static final long serialVersionUID = 6551391859868552192L;
	protected final Map<String, TimeStats<E>> dayStats;
	protected transient final ObjectMapper jsonMapper;
	protected final ThreadLocal<DateFormat> outputFormat;
	protected final DecimalFormat df;
	protected final boolean detailed;

	public DayStats() {
		this(false, new DecimalFormat(DEFAULT_DECIMAL_FORMAT));
	}

	public DayStats(final boolean detailed, final DecimalFormat decimalFormat) {
		this.df = Preconditions.checkNotNull(decimalFormat, "'decimalFormat' argument cannot be null.");
		dayStats = new TreeMap<String, TimeStats<E>>();
		jsonMapper = new ObjectMapper();
		this.outputFormat = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(DEFAULT_REPORT_DATE_FORMAT);
			}
		};
		this.detailed = detailed;
	}

	@Override
	public void consume(final E newEntry) {
		Preconditions.checkNotNull(newEntry);
		String key = newEntry.getAction();
		TimeStats<E> timeStats = getNewOrExistingTimeStats(key);
		timeStats.consume(newEntry);
		dayStats.put(key, timeStats);
	}

	@JsonIgnore
	protected TimeStats<E> getNewOrExistingTimeStats(final String key) {
		TimeStats<E> timeStats = null;
		if (dayStats.containsKey(key)) {
			timeStats = dayStats.get(key);
		} else {
			timeStats = new TimeStats<E>();
		}
		return timeStats;
	}

	public Map<String, TimeStats<E>> filter(final Predicate<? super PredicateArguments> predicate) {
		Map<String, TimeStats<E>> filtered = new TreeMap<String, TimeStats<E>>();
		SummaryStatistics movingStats;
		StatisticalSummary currentStats;
		PredicateArguments arguments;
		TimeStats<E> timeStats;
		for (Entry<String, TimeStats<E>> entries : dayStats.entrySet()) {
			movingStats = new SummaryStatistics();
			timeStats = new TimeStats<E>();
			for (Entry<Integer, StatisticalSummary> times : entries.getValue().getTimeStats().entrySet()) {
				currentStats = times.getValue();
				movingStats.addValue(currentStats.getMean()); // calculate MA
				arguments = new PredicateArguments(movingStats, currentStats.getMean());
				if (predicate.apply(arguments)) {
					timeStats.add(times.getKey(), times.getValue());
					filtered.put(entries.getKey(), timeStats);
				}
			}
		}
		return filtered;
	}

	@JsonIgnore
	public TimeStats<E> getTimeStats(final String key) {
		return dayStats.get(key);
	}

	public Map<String, TimeStats<E>> getDayStats() {
		return Collections.unmodifiableMap(dayStats);
	}

	public Function<Integer, String> formatToShortDate = new Function<Integer, String>() {
		private final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("M/d");
			}
		};

		public String apply(final Integer date) {
			return formatDate(dateFormatter.get(), "" + date);
		}
	};

	@Override
	public String toString() {
		return toString(dayStats);
	}

	public String toString(final Map<String, TimeStats<E>> dayStats) {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);

		for (Entry<String, TimeStats<E>> entry : dayStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			Tuple tuple = calculateSummary(entry.getValue().getTimeStats().values());
			sb.append(entry.getKey());
			sb.append(",");
			sb.append(String.format(" %s days, ~%s/day, ~%sms/day", tuple.count, tuple.avgCount, df.format(tuple.avgTime)));
			sb.append(LINE_SEPARATOR);
			if (detailed) {
				writeColumns(sb, entry.getValue());
				sb.append(LINE_SEPARATOR);
			}
		}

		return sb.toString();
	}

	protected void writeColumns(StringBuilder sb, final TimeStats<E> timeStats) {
		sb.append("\tDate, \t#, \tMean, \tStandard Deviation, \tMax, \tMin");
		for (Entry<Integer, StatisticalSummary> entry : timeStats.getTimeStats().entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = entry.getValue();
			sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s, \t%s",
					formatDate(outputFormat.get(), "" + entry.getKey()),
					summary.getN(),
					df.format(summary.getMean()),
					df.format(summary.getStandardDeviation()),
					df.format(summary.getMax()),
					df.format(summary.getMin())));
		}
	}

	public String toCsvString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		for (Entry<String, TimeStats<E>> entry : dayStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			Tuple tuple = calculateSummary(entry.getValue().getTimeStats().values());
			sb.append(StringEscapeUtils.escapeCsv(entry.getKey()));
			sb.append(", ");
			sb.append(String.format(" %s days, avg %s/day, avg %sms/day",
					tuple.count,
					tuple.avgCount,
					StringEscapeUtils.escapeCsv(df.format(tuple.avgTime))));
			sb.append(LINE_SEPARATOR);
			if (detailed) {
				writeCsvColumns(sb, entry.getValue());
				sb.append(LINE_SEPARATOR);
			}
		}
		return sb.toString();
	}

	protected void writeCsvColumns(StringBuilder sb, final TimeStats<E> timeStats) {
		sb.append(", Date, #, Mean, Standard Deviation, Max, Min");
		for (Entry<Integer, StatisticalSummary> entry : timeStats.getTimeStats().entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = entry.getValue();
			sb.append(String.format(", %s, %s, %s, %s, %s, %s",
					formatDate(outputFormat.get(), "" + entry.getKey()),
					summary.getN(),
					StringEscapeUtils.escapeCsv(df.format(summary.getMean())),
					StringEscapeUtils.escapeCsv(df.format(summary.getStandardDeviation())),
					StringEscapeUtils.escapeCsv(df.format(summary.getMax())),
					StringEscapeUtils.escapeCsv(df.format(summary.getMin()))));
		}
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

	public DayStats<E> fromJsonString(final String jsonString) {
		throw new NotImplementedException("DayStats does not implement JSON deserialization.");
	}

	private Tuple calculateSummary(final Collection<StatisticalSummary> stats) {
		int count = 0;
		int avgCount = 0;
		double avgTime = 0D;
		for (StatisticalSummary stat : stats) {
			avgCount += stat.getN();
			avgTime += stat.getMean();
		}
		count = stats.size();
		avgCount = avgCount > 0 ? Math.round(avgCount / count) : 0;
		avgTime = avgTime > 0 ? avgTime / count : 0;
		return new Tuple(count, avgCount, avgTime);
	}
}
