package org.logparser.config;

import static org.logparser.Constants.DEFAULT_DECIMAL_FORMAT;

import java.text.DecimalFormat;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.logparser.ITimestampedEntry;
import org.logparser.LogSnapshot;
import org.logparser.stats.AbstractStats;
import org.logparser.stats.DayStats;
import org.logparser.stats.HourStats;
import org.logparser.stats.MinuteStats;
import org.logparser.stats.PercentagePredicate;
import org.logparser.stats.PredicateArguments;
import org.logparser.stats.StandardDeviationPredicate;
import org.logparser.stats.WeekDayStats;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Responsible for providing bespoke instances of {@link AbstractStats}
 * implementations.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class StatsProvider {
	public enum GroupBy {
		DAY_OF_MONTH, DAY_OF_WEEK
	};

	public enum PredicateType {
		STDEV, PERCENTAGE
	};

	private final GroupBy[] groupBy;
	private final boolean storeFilteredEntries;
	private final DecimalFormat decimalFormat;
	private final boolean detailed;
	private final PredicateType predicateType;
	private final double predicateValue;
	private final Predicate<PredicateArguments> predicate;

	@JsonCreator
	public StatsProvider(
			@JsonProperty("groupBy") final GroupBy[] groupBy,
			@JsonProperty("storeFilteredEntries") final boolean storeFilteredEntries,
			@JsonProperty("decimalFormat") final DecimalFormat decimalFormat,
			@JsonProperty("detailed") final boolean detailed,
			@JsonProperty("predicateType") final PredicateType predicateType,
			@JsonProperty("predicateValue") final double predicateValue) {

		this.storeFilteredEntries = storeFilteredEntries;
		this.decimalFormat = decimalFormat != null ? decimalFormat : new DecimalFormat(DEFAULT_DECIMAL_FORMAT);
		this.detailed = detailed;
		Preconditions.checkArgument(predicateValue > 0, "Predicate 'value' argument must be a positive number.");
		this.predicateType = Preconditions.checkNotNull(predicateType, "'predicateType' argument cannot be null.");
		this.predicateValue = predicateValue;
		this.groupBy = Preconditions.checkNotNull(groupBy, "'groupBy' argument cannot be null.");
		this.predicate = predicateType.equals(PredicateType.PERCENTAGE) ? new PercentagePredicate(predicateValue) : new StandardDeviationPredicate(predicateValue);
	}

	public boolean isStoreFilteredEntries() {
		return storeFilteredEntries;
	}

	public DecimalFormat getDecimalFormat() {
		return decimalFormat;
	}

	public boolean isDetailed() {
		return detailed;
	}

	public Predicate<PredicateArguments> getPredicate() {
		return predicate;
	}

	public PredicateType getPredicateType() {
		return predicateType;
	}

	public double getPredicateValue() {
		return predicateValue;
	}

	public GroupBy[] getGroupBy() {
		return groupBy;
	}

	public <E extends ITimestampedEntry> LogSnapshot<E> buildLogSnapshot() {
		return new LogSnapshot<E>(storeFilteredEntries, decimalFormat);
	}

	public <E extends ITimestampedEntry> DayStats<E> buildDayStats() {
		return new DayStats<E>(detailed, decimalFormat);
	}

	public <E extends ITimestampedEntry> WeekDayStats<E> buildWeekDayStats() {
		return new WeekDayStats<E>(detailed, decimalFormat);
	}

	public <E extends ITimestampedEntry> HourStats<E> buildHourStats() {
		return new HourStats<E>(detailed, decimalFormat);
	}

	public <E extends ITimestampedEntry> MinuteStats<E> buildMinuteStats() {
		return new MinuteStats<E>(detailed, decimalFormat);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
