package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link LogSnapshot}.
 * 
 * @author jorge.decastro
 * 
 */
public class LogSnapshotTest {
	private LogSnapshot<TestMessage> underTest;

	@Test(expected = NullPointerException.class)
	public void testNullFilteredEntriesArgument() {
		new LogSnapshot<TestMessage>(null, 0);
	}

	@Test
	public void testNotNullFilteredEntriesArguments() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0);
		assertThat(underTest, is(notNullValue()));
	}

	@Test
	public void testTotalEntriesArgument() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 1000);
		assertThat(underTest.getTotalEntries(), is(equalTo(1000)));
	}

	@Test
	public void testEmptyFilteredEntries() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0);
		assertThat(underTest.getFilteredEntries().size(), is(equalTo(0)));
	}

	@Test
	public void testNotEmptyFilteredEntries() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		TestMessage tm = new TestMessage(1000);
		filteredEntries.add(tm);
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0);
		assertThat(underTest.getFilteredEntries().size(), is(equalTo(1)));
		assertThat(underTest.getFilteredEntries(), hasItem(tm));
	}
}
