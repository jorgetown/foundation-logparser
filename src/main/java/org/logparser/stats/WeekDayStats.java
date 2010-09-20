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
@JsonPropertyOrder({ "dayStats" })
public class WeekDayStats<E extends ITimestampedEntry> extends DayStats<E> {
	private static final long serialVersionUID = -3821734276444687735L;

	public WeekDayStats() {
		super();
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

	@Override
	protected void writeColumns(StringBuilder sb, final Entry<String, TimeStats<E>> entries) {
		sb.append("\tDay, \t#, \tMean, \tStandard Deviation, \tMax, \tMin");
		for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = timeStats.getValue();
			sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s, \t%s",
					weekdayNoToString(timeStats.getKey()), 
					summary.getN(),
					Double.valueOf(df.format(summary.getMean())),
					Double.valueOf(df.format(summary.getStandardDeviation())),
					Double.valueOf(df.format(summary.getMax())),
					Double.valueOf(df.format(summary.getMin()))));
		}
	}

	@Override
	protected void writeCsvColumns(StringBuilder sb, final Entry<String, TimeStats<E>> entries) {
		sb.append(", Day, #, Mean, Standard Deviation, Max, Min");
		for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = timeStats.getValue();
			sb.append(String.format(", %s, %s, %s, %s, %s, %s",
					weekdayNoToString(timeStats.getKey()), 
					summary.getN(),
					StringEscapeUtils.escapeCsv(df.format(summary.getMean())),
					StringEscapeUtils.escapeCsv(df.format(summary.getStandardDeviation())), 
					StringEscapeUtils.escapeCsv(df.format(summary.getMax())),
					StringEscapeUtils.escapeCsv(df.format(summary.getMin()))));
		}
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
