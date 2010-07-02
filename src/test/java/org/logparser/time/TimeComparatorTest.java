package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.logparser.TestMessage;

/**
 * Tests for {@link TimeComparator}.
 * 
 * @author jorge.decastro
 * 
 */
public class TimeComparatorTest {
	private TimeComparator<TestMessage> tc;

	@Test(expected = NullPointerException.class)
	public void testNullTimeUnitArgument() {
		new TimeComparator<TestMessage>(0, null);
	}

	@Test
	public void testEntriesAreNotEnoughApart() {
		tc = new TimeComparator<TestMessage>(3000, TimeUnit.MILLISECONDS);
		TestMessage entry1 = new TestMessage(1000);
		TestMessage entry2 = new TestMessage(2000);
		boolean isApart = tc.isIntervalApart(entry1, entry2);
		assertThat(isApart, is(false));
	}

	@Test
	public void testEntriesAreEnoughApart() {
		tc = new TimeComparator<TestMessage>(3000, TimeUnit.MILLISECONDS);
		TestMessage entry1 = new TestMessage(1000);
		TestMessage entry2 = new TestMessage(4001);
		boolean isApart = tc.isIntervalApart(entry1, entry2);
		assertThat(isApart, is(true));
	}
}