package org.logparser.time;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;
import org.logparser.time.SimpleTimeInterval;
import org.logparser.time.Time;

/**
 * Tests for {@link SimpleTimeInterval}.
 * 
 * @author jorge.decastro
 * 
 */
public class SimpleTimeIntervalTest {
	private static final Calendar cal;

	static {
		cal = Calendar.getInstance();
	}

	@Test
	public void testDateIsBetweenTimeInterval() {
		// Given
		cal.set(2010, 1, 14, 14, 23, 10);
		Time after = new Time(12, 30);
		Time before = new Time(19, 30);
		SimpleTimeInterval timeInterval = new SimpleTimeInterval(after, before);

		// When
		boolean isBetween = timeInterval.isBetweenInstants(cal.getTime());

		// Then
		assertTrue(isBetween);
	}

	@Test
	public void testDateIsNotBetweenTimeInterval() {
		// Given
		cal.set(2010, 1, 14, 14, 23, 10);
		Time after = new Time(10, 30);
		Time before = new Time(11, 45);
		SimpleTimeInterval timeInterval = new SimpleTimeInterval(after, before);

		// When
		boolean isBetween = timeInterval.isBetweenInstants(cal.getTime());

		// Then
		assertFalse(isBetween);
	}

	@Test
	public void testDateIsAfterGivenTime() {
		// Given
		cal.set(2010, 1, 14, 14, 23, 10);
		Time time = new Time(13, 30);
		SimpleTimeInterval timeInterval = new SimpleTimeInterval(time);

		// When
		boolean isAfter = timeInterval.isAfter(cal.getTime());

		// Then
		assertTrue(isAfter);
	}

	@Test
	public void testDateIsNotAfterGivenTime() {
		// Given
		cal.set(2010, 1, 14, 14, 23, 10);
		Time time = new Time(17, 30);
		SimpleTimeInterval timeInterval = new SimpleTimeInterval(time);

		// When
		boolean isAfter = timeInterval.isAfter(cal.getTime());

		// Then
		assertFalse(isAfter);
	}

	@Test
	public void testDateIsBeforeGivenTime() {
		// Given
		cal.set(2010, 1, 14, 14, 23, 10);
		Time time = new Time(17, 30);
		SimpleTimeInterval timeInterval = new SimpleTimeInterval(time);

		// When
		boolean isBefore = timeInterval.isBefore(cal.getTime());

		// Then
		assertTrue(isBefore);
	}

	@Test
	public void testDateIsNotBeforeGivenTime() {
		// Given
		cal.set(2010, 1, 14, 14, 23, 10);
		Time time = new Time(11, 30);
		SimpleTimeInterval timeInterval = new SimpleTimeInterval(time);

		// When
		boolean isBefore = timeInterval.isBefore(cal.getTime());

		// Then
		assertFalse(isBefore);
	}
}
