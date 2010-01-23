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
	public void testAnyDateIsAlwaysBetweenAnInfiniteTimeInterval() {
		// Given
		InfiniteTimeInterval timeInterval = new InfiniteTimeInterval();

		// When
		boolean isBetween = timeInterval.isBetweenInstants(new Date());

		// Then
		assertTrue(isBetween);
	}
}
