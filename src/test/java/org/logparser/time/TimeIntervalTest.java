package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link TimeInterval}.
 * 
 * @author jorge.decastro
 * 
 */
public class TimeIntervalTest {
	private Calendar cal;

	@Before
	public void setUp() {
		cal = Calendar.getInstance();
	}

	@After
	public void tearDown() {
		cal = null;
	}

	@Test(expected = NullPointerException.class)
	public void testNullDateIsNotBetweenTimeInterval() {
		Instant begin = new Instant(12, 30);
		Instant end = new Instant(19, 30);
		TimeInterval timeInterval = new TimeInterval(begin, end);

		timeInterval.isBetweenInstants(null);
		assertThat(timeInterval, is(nullValue()));
	}

	@Test
	public void testDateIsBetweenTimeInterval() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant begin = new Instant(12, 30);
		Instant end = new Instant(19, 30);
		TimeInterval timeInterval = new TimeInterval(begin, end);

		boolean isBetween = timeInterval.isBetweenInstants(cal.getTime());

		assertThat(isBetween, is(true));
	}

	@Test
	public void testDateIsNotBetweenTimeInterval() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant begin = new Instant(10, 30);
		Instant end = new Instant(11, 45);
		TimeInterval timeInterval = new TimeInterval(begin, end);

		boolean isBetween = timeInterval.isBetweenInstants(cal.getTime());

		assertThat(isBetween, is(false));
	}

	@Test
	public void testDateIsAfterGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(13, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isAfter = timeInterval.isAfter(cal.getTime());

		assertThat(isAfter, is(true));
	}

	@Test
	public void testDateIsNotAfterGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(17, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isAfter = timeInterval.isAfter(cal.getTime());

		assertThat(isAfter, is(false));
	}

	@Test
	public void testDateIsBeforeGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(17, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isBefore = timeInterval.isBefore(cal.getTime());

		assertThat(isBefore, is(true));
	}

	@Test
	public void testDateIsNotBeforeGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(11, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isBefore = timeInterval.isBefore(cal.getTime());

		assertThat(isBefore, is(false));
	}
}
