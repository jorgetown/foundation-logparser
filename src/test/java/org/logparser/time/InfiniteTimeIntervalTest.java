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
	public void testAnyDateIsAlwaysBetweenInfiniteTimeInterval() {
		// Given
		InfiniteTimeInterval timeWindow = new InfiniteTimeInterval();

		// When
		boolean isBetween = timeWindow.isBetweenInstants(new Date());

		// Then
		assertTrue(isBetween);
	}
}
