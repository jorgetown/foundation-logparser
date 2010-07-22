package org.logparser.time;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		Instant after = new Instant(12, 30);
		Instant before = new Instant(19, 30);
		TimeInterval timeInterval = new TimeInterval(after, before);

		timeInterval.isBetweenInstants(null);
	}

	@Test
	public void testDateIsBetweenTimeInterval() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant after = new Instant(12, 30);
		Instant before = new Instant(19, 30);
		TimeInterval timeInterval = new TimeInterval(after, before);

		boolean isBetween = timeInterval.isBetweenInstants(cal.getTime());

		assertTrue(isBetween);
	}

	@Test
	public void testDateIsNotBetweenTimeInterval() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant after = new Instant(10, 30);
		Instant before = new Instant(11, 45);
		TimeInterval timeInterval = new TimeInterval(after, before);

		boolean isBetween = timeInterval.isBetweenInstants(cal.getTime());

		assertFalse(isBetween);
	}

	@Test
	public void testDateIsAfterGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(13, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isAfter = timeInterval.isAfter(cal.getTime());

		assertTrue(isAfter);
	}

	@Test
	public void testDateIsNotAfterGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(17, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isAfter = timeInterval.isAfter(cal.getTime());

		assertFalse(isAfter);
	}

	@Test
	public void testDateIsBeforeGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(17, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isBefore = timeInterval.isBefore(cal.getTime());

		assertTrue(isBefore);
	}

	@Test
	public void testDateIsNotBeforeGivenTime() {
		cal.set(2010, 1, 14, 14, 23, 10);
		Instant instant = new Instant(11, 30);
		TimeInterval timeInterval = new TimeInterval(instant);

		boolean isBefore = timeInterval.isBefore(cal.getTime());

		assertFalse(isBefore);
	}
}
