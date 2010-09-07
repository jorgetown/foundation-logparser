package org.logparser.stats;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PercentagePredicate}.
 * 
 * @author jorge.decastro
 * 
 */
public class PercentagePredicateTest {
	private PercentagePredicate underTest;

	@Before
	public void setUp() {
		underTest = new PercentagePredicate(10);
	}

	@After
	public void tearDown() {
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testNullArguments() {
		assertThat(underTest.apply(null), is(false));
	}

	@Test
	public void testPercentageGivenIsPercentageObtained() {
		underTest = new PercentagePredicate(20);
		assertThat(underTest.getPercentage(), is(20D));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPercentageGivenMustBePositiveNumber() {
		underTest = new PercentagePredicate(-10);
	}

	@Test
	public void testDeltaOfAveragesBelowOrEqualToPercentageGivenYieldsFalse() {
		SummaryStatistics stats = new SummaryStatistics();
		stats.addValue(70);
		stats.addValue(90);
		double current = 85;
		PredicateArguments arguments = new PredicateArguments(stats, current);
		assertThat(underTest.apply(arguments), is(false));
	}

	@Test
	public void testDeltaOfAveragesAbovePercentageGivenYieldsTrue() {
		SummaryStatistics stats = new SummaryStatistics();
		stats.addValue(70);
		stats.addValue(90);
		double current = 100;
		PredicateArguments arguments = new PredicateArguments(stats, current);
		assertThat(underTest.apply(arguments), is(true));
	}
}
