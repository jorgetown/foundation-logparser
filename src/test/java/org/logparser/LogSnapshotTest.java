package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link LogSnapshot}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class LogSnapshotTest {
	private LogSnapshot<TestMessage> underTest;
	@Mock
	private SortedMap<String, Integer> mockSummary;
	@Mock
	private SortedMap<String, Integer> mockTimeBreakdown;
	

	@Test(expected = NullPointerException.class)
	public void testNullFilteredEntriesArgument() {
		new LogSnapshot<TestMessage>(null, 0, mockSummary, mockTimeBreakdown);
	}

	@Test
	public void testNotNullFilteredEntriesArguments() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0, mockSummary, mockTimeBreakdown);
		assertThat(underTest, is(notNullValue()));
	}

	@Test
	public void testTotalEntriesArgument() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 1000, mockSummary, mockTimeBreakdown);
		assertThat(underTest.getTotalEntries(), is(equalTo(1000)));
	}

	@Test
	public void testEmptyFilteredEntries() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0, mockSummary, mockTimeBreakdown);
		assertThat(underTest.getFilteredEntries().size(), is(equalTo(0)));
	}

	@Test
	public void testNotEmptyFilteredEntries() {
		List<TestMessage> filteredEntries = new ArrayList<TestMessage>();
		TestMessage tm = new TestMessage(1000);
		filteredEntries.add(tm);
		underTest = new LogSnapshot<TestMessage>(filteredEntries, 0, mockSummary, mockTimeBreakdown);
		assertThat(underTest.getFilteredEntries().size(), is(equalTo(1)));
		assertThat(underTest.getFilteredEntries(), hasItem(tm));
	}
}
