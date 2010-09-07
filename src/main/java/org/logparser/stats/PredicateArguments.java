package org.logparser.stats;

import net.jcip.annotations.Immutable;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;

import com.google.common.base.Predicate;

/**
 * Holds arguments to pass to (statistical) {@link Predicate} implementations.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class PredicateArguments {
	private final StatisticalSummary previous;
	private final double current;

	public PredicateArguments(final StatisticalSummary previous, final double current) {
		this.previous = previous;
		this.current = current;
	}

	public StatisticalSummary getPrevious() {
		return previous;
	}

	public double getCurrent() {
		return current;
	}
}