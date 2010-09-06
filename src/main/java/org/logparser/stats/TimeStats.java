package org.logparser.stats;

import static org.logparser.Constants.LINE_SEPARATOR;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.jcip.annotations.Immutable;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.logparser.ITimestampedEntry;

import com.google.common.base.Preconditions;

/**
 * Provides a statistical summary of a collection of log
 * {@link ITimestampedEntry}s keyed by a given time criteria (e.g.: day of the
 * month, hour of the day, etc).
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "timeStats" })
public class TimeStats<E extends ITimestampedEntry> extends AbstractStats<E> {
	public static final int DEFAULT_TIME_CRITERIA = Calendar.DAY_OF_MONTH;
	private static final long serialVersionUID = 3662219442973110796L;
	private final Map<Integer, StatisticalSummary> timeStats;
	private final Calendar calendar;
	private final int timeCriteria;

	public TimeStats() {
		this(DEFAULT_TIME_CRITERIA);
	}

	public TimeStats(final int timeCriteria) {
		timeStats = new TreeMap<Integer, StatisticalSummary>();
		calendar = Calendar.getInstance();
		this.timeCriteria = timeCriteria;
	}

	@Override
	public void add(final E newEntry) {
		Preconditions.checkNotNull(newEntry);
		calendar.setTimeInMillis(newEntry.getTimestamp());
		int time = calendar.get(timeCriteria);
		SummaryStatistics summaryStatistics = getNewOrExistingSummaryStatistics(time);
		summaryStatistics.addValue(newEntry.getDuration());
		timeStats.put(time, summaryStatistics);
	}
	
	public void add(final int time, final StatisticalSummary stats) {
		Preconditions.checkNotNull(stats);
		timeStats.put(time, stats);
	}

	private SummaryStatistics getNewOrExistingSummaryStatistics(final int time) {
		SummaryStatistics summaryStatistics = null;
		if (timeStats.containsKey(time)) {
			summaryStatistics = (SummaryStatistics) timeStats.get(time);
		} else {
			summaryStatistics = new SummaryStatistics();
		}
		return summaryStatistics;
	}

	@JsonIgnore
	public int getTimeCriteria() {
		return timeCriteria;
	}

	@JsonIgnore
	public StatisticalSummary getSummaryStatistics(final int time) {
		if (timeStats.containsKey(time)) {
			SummaryStatistics summaryStats = (SummaryStatistics) timeStats.get(time);
			return summaryStats.copy();
		}
		return null;
	}

	public Map<Integer, StatisticalSummary> getTimeStats() {
		return Collections.unmodifiableMap(timeStats);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\tTime, \t#, \tMean, \tStandard Deviation, \tMax, \tMin");
		for (Entry<Integer, StatisticalSummary> entry : timeStats.entrySet()) {
			sb.append(LINE_SEPARATOR);
			StatisticalSummary summary = entry.getValue();
			sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s, \t%s",
					entry.getKey(), 
					summary.getN(), 
					summary.getMean(),
					summary.getStandardDeviation(), 
					summary.getMax(),
					summary.getMin()));
		}
		sb.append(LINE_SEPARATOR);
		return sb.toString();
	}
}
