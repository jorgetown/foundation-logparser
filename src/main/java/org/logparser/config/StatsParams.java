package org.logparser.config;

import java.util.Calendar;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.logparser.IObserver;
import org.logparser.stats.PercentagePredicate;
import org.logparser.stats.PredicateArguments;
import org.logparser.stats.StandardDeviationPredicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Configuration parameters for the statistics {@link IObserver}
 * implementations.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class StatsParams {
	public enum GroupBy {
		DAY_OF_MONTH, DAY_OF_WEEK
	};

	public enum PredicateType {
		STDEV, PERCENTAGE
	};

	private final GroupBy groupBy;
	private final PredicateType predicateType;
	private final double predicateValue;
	private final Predicate<PredicateArguments> predicate;

	@JsonCreator
	public StatsParams(@JsonProperty("predicateType") final PredicateType predicateType, @JsonProperty("predicateValue") final double predicateValue) {
		this(predicateType, predicateValue, GroupBy.DAY_OF_MONTH);
	}

	@JsonCreator
	public StatsParams(@JsonProperty("predicateType") final PredicateType predicateType, @JsonProperty("predicateValue") final double predicateValue, @JsonProperty("groupBy") final GroupBy groupBy) {
		Preconditions.checkNotNull(predicateType, "'predicateType' argument cannot be null.");
		Preconditions.checkArgument(predicateValue > 0, "Predicate 'value' argument must be a positive number.");
		Preconditions.checkNotNull(groupBy, "'groupBy' argument cannot be null.");
		
		this.predicateType = predicateType;
		this.predicateValue = predicateValue;
		predicate = predicateType.equals(PredicateType.PERCENTAGE) ? new PercentagePredicate(predicateValue) : new StandardDeviationPredicate(predicateValue);
		this.groupBy = groupBy;
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

	public GroupBy getGroupBy() {
		return groupBy;
	}

	public int groupByToCalendar() {
		switch (groupBy) {
		case DAY_OF_WEEK:
			return Calendar.DAY_OF_WEEK;
		default:
			return Calendar.DAY_OF_MONTH;
		}
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
