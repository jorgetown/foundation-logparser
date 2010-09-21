package org.logparser.stats;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.util.Calendar;
import java.util.Map.Entry;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.logparser.ITimestampedEntry;

/**
 * Provides a statistical summary of a collection of log
 * {@link ITimestampedEntry}s, grouped by the day of the week and keyed by
 * {@link ITimestampedEntry#getAction()}.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "dayStats", "aggregateDayStats" })
public class WeekDayStats<E extends ITimestampedEntry> extends DayStats<E> {
	private static final long serialVersionUID = -3821734276444687735L;
	private final TimeStats<E> aggregateTimeStats;

	public WeekDayStats() {
		super();
		aggregateTimeStats = new TimeStats<E>(Calendar.DAY_OF_WEEK);
	}
	
	@Override
	public void consume(E entry) {
		super.consume(entry);
		aggregateTimeStats.consume(entry);
	}

	@JsonIgnore
	@Override
	protected TimeStats<E> getNewOrExistingTimeStats(final String key) {
		TimeStats<E> timeStats = null;
		if (dayStats.containsKey(key)) {
			timeStats = dayStats.get(key);
		} else {
			timeStats = new TimeStats<E>(Calendar.DAY_OF_WEEK);
		}
		return timeStats;
	}
	
	public TimeStats<E> getAggregateDayStats() {
		return aggregateTimeStats;
	}

	@Override
	protected void writeColumns(StringBuilder sb, final TimeStats<E> timeStats) {
		sb.append("\tDay, \t#, \tMean, \tStandard Deviation, \tMax, \tMin");
		for (Entry<Integer, StatisticalSummary> entry : timeStats.getTimeStats().entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = entry.getValue();
			sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s, \t%s",
					weekdayNoToString(entry.getKey()), 
					summary.getN(),
					Double.valueOf(df.format(summary.getMean())),
					Double.valueOf(df.format(summary.getStandardDeviation())),
					Double.valueOf(df.format(summary.getMax())),
					Double.valueOf(df.format(summary.getMin()))));
		}
	}
	
	@Override
	public String toCsvString() {
		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
		writeCsvColumns(sb, aggregateTimeStats);
		sb.append(LINE_SEPARATOR);
		for (Entry<String, TimeStats<E>> entry : dayStats.entrySet()) {
			sb.append(StringEscapeUtils.escapeCsv(entry.getKey()));
			sb.append(LINE_SEPARATOR);
			writeCsvColumns(sb, entry.getValue());
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	@Override
	protected void writeCsvColumns(StringBuilder sb, final TimeStats<E> timeStats) {
		sb.append(", Day, #, Mean, Standard Deviation, Max, Min");
		for (Entry<Integer, StatisticalSummary> entry : timeStats.getTimeStats().entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = entry.getValue();
			sb.append(String.format(", %s, %s, %s, %s, %s, %s",
					weekdayNoToString(entry.getKey()), 
					summary.getN(),
					StringEscapeUtils.escapeCsv(df.format(summary.getMean())),
					StringEscapeUtils.escapeCsv(df.format(summary.getStandardDeviation())), 
					StringEscapeUtils.escapeCsv(df.format(summary.getMax())),
					StringEscapeUtils.escapeCsv(df.format(summary.getMin()))));
		}
	}

	@Override
	public String getFormattedLabel(final int key) {
		return weekdayNoToString(key);
	}

	private String weekdayNoToString(final int day) {
		switch (day) {
		case 1:
			return "Sun";
		case 2:
			return "Mon";
		case 3:
			return "Tue";
		case 4:
			return "Wed";
		case 5:
			return "Thu";
		case 6:
			return "Fri";
		case 7:
			return "Sat";
		}
		return "";
	}
}
