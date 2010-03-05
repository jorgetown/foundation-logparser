package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Tests for {@link TimeComparator}.
 * 
 * @author jorge.decastro
 * 
 */
public class TimeComparatorTest {
	private static final String FIELD_TO_COMPARE = "milliseconds";
	private TimeComparator<TestMessage> tc;

	@Test(expected = NullPointerException.class)
	public void testNullFieldArgument() {
		new TimeComparator<TestMessage>(null, 0, TimeUnit.MILLISECONDS);
	}

	@Test(expected = NullPointerException.class)
	public void testNullTimeUnitArgument() {
		new TimeComparator<TestMessage>(FIELD_TO_COMPARE, 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingFieldArgument() {
		tc = new TimeComparator<TestMessage>("aMissingField", 0, TimeUnit.MILLISECONDS);
		TestMessage entry1 = new TestMessage(1000);
		TestMessage entry2 = new TestMessage(2000);
		tc.isIntervalApart(entry1, entry2);
	}

	@Test
	public void testEntriesAreNotApartEnough() {
		tc = new TimeComparator<TestMessage>(FIELD_TO_COMPARE, 3000, TimeUnit.MILLISECONDS);
		TestMessage entry1 = new TestMessage(1000);
		TestMessage entry2 = new TestMessage(2000);
		boolean isApart = tc.isIntervalApart(entry1, entry2);
		assertThat(isApart, is(false));
	}

	@Test
	public void testEntriesAreApartEnough() {
		tc = new TimeComparator<TestMessage>(FIELD_TO_COMPARE, 3000, TimeUnit.MILLISECONDS);
		TestMessage entry1 = new TestMessage(1000);
		TestMessage entry2 = new TestMessage(4001);
		boolean isApart = tc.isIntervalApart(entry1, entry2);
		assertThat(isApart, is(true));
	}

	private static class TestMessage {
		private final long milliseconds;

		public TestMessage(final long milliseconds) {
			this.milliseconds = milliseconds;
		}
	}
}
