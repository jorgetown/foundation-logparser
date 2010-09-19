package org.logparser.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Calendar;

import org.junit.Test;
import org.logparser.config.StatsParams.PredicateType;
import org.logparser.stats.PercentagePredicate;

/**
 * Unit tests for {@link StatsParams}.
 * 
 * @author jorge.decastro
 * 
 */
public class StatsParamsTest {
	private static double PREDICATE_VALUE = 10;
	private static PredicateType PREDICATE_TYPE = PredicateType.PERCENTAGE;
	private StatsParams underTest;

	@Test(expected = NullPointerException.class)
	public void testNullPredicateTypeArgument() {
		underTest = new StatsParams(null, PREDICATE_VALUE);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testNotNullPredicateTypeArgument() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE);
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getPredicate(), is(instanceOf(PercentagePredicate.class)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeNumberForPredicateValue() {
		underTest = new StatsParams(PREDICATE_TYPE, -2D);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testValidNumberForPredicateValue() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE);
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getPredicateValue(), is(PREDICATE_VALUE));
	}

	@Test(expected = NullPointerException.class)
	public void testNullGroupByArgument() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE, null);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testOptionalGroupByArgumentHasDefaultValue() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE);
		assertThat(underTest.getGroupBy(), is(equalTo(StatsParams.GroupBy.DAY_OF_MONTH)));
	}

	@Test
	public void testOverrideOfOptionalGroupByArgumentReturnsTheOverride() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE, StatsParams.GroupBy.DAY_OF_WEEK);
		assertThat(underTest.getGroupBy(), is(equalTo(StatsParams.GroupBy.DAY_OF_WEEK)));
	}

	@Test
	public void testGroupByToCalendarConversionHasDefaultValue() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE);
		assertThat(underTest.groupByToCalendar(), is(equalTo(Calendar.DAY_OF_MONTH)));
	}

	@Test
	public void testOverrideOfGroupByToCalendarConversionReturnsTheOverride() {
		underTest = new StatsParams(PREDICATE_TYPE, PREDICATE_VALUE, StatsParams.GroupBy.DAY_OF_WEEK);
		assertThat(underTest.groupByToCalendar(), is(equalTo(Calendar.DAY_OF_WEEK)));
	}
}
