package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
	public void testNullDateIsAlwaysBetweenInstantsOfAnInfiniteTimeInterval() {
		InfiniteTimeInterval timeInterval = new InfiniteTimeInterval();

		boolean isBetween = timeInterval.isBetweenInstants(null);

		assertThat(isBetween, is(true));
	}

	@Test
	public void testAnyDateIsAlwaysBetweenInstantsOfAnInfiniteTimeInterval() {
		InfiniteTimeInterval timeInterval = new InfiniteTimeInterval();

		boolean isBetween = timeInterval.isBetweenInstants(new Date());

		assertThat(isBetween, is(true));
	}
}
