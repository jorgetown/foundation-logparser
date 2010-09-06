package org.logparser.stats;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * {@link Predicate} implementation that allows consumers to selectively filter
 * out matching instances according to the given percentage condition.
 * 
 * @author jorge.decastro
 * 
 */
public class PercentagePredicate implements Predicate<PredicateArguments> {
	private final double percentage;

	public PercentagePredicate(final int percentage) {
		Preconditions.checkArgument(percentage > 0, "Percentage argument must be a positive integer.");
		this.percentage = percentage;
	}

	public boolean apply(final PredicateArguments arguments) {
		double movingMean = arguments.getPrevious().getMean();
		double delta = ((movingMean - arguments.getCurrent()) / movingMean) * 100D;
		return Math.abs(delta) > percentage;
	}
}
