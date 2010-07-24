package org.logparser.time;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

/**
 * Tests for {@link InfiniteTimeInterval}.
 * 
 * @author jorge.decastro
 * 
 */
public class InfiniteTimeIntervalTest {

	@Test
	public void testNullDateIsAlwaysBetweenAnInfiniteTimeInterval() {
		InfiniteTimeInterval timeInterval = new InfiniteTimeInterval();

		boolean isBetween = timeInterval.isBetweenInstants(null);

		assertTrue(isBetween);
	}

	@Test
	public void testAnyDateIsAlwaysBetweenAnInfiniteTimeInterval() {
		InfiniteTimeInterval timeInterval = new InfiniteTimeInterval();

		boolean isBetween = timeInterval.isBetweenInstants(new Date());

		assertTrue(isBetween);
	}
}
