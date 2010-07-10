package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
 
/**
 * Tests for {@link AbstractLogFilter}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractLogFilterTest {
	private AbstractLogFilter<TestMessage> underTest;
	private List<TestMessage> entries;
	@Mock
	private IMessageFilter<TestMessage> mockMessageFilter;
	@Mock
	private IMessageFilter<TestMessage> mockMessageFilter2;
	@Mock
	private IMessageFilter<TestMessage> mockMessageFilter3;
	@Mock
	private Map<String, Integer> mockSummary;
	@Mock
	private Map<Integer, Integer> mockTimeBreakdown;
 
	@Before
	public void setup() {
		entries = new ArrayList<TestMessage>();
		underTest = new AbstractLogFilter<TestMessage>() {
 
			@Override
			public LogSnapshot<TestMessage> filter(String filepath) {
				return new LogSnapshot<TestMessage>(entries, 0, mockSummary, mockTimeBreakdown);
			}

			@Override
			protected void updateLogSummary(TestMessage entry) {
			}

			@Override
			protected void updateLogTimeBreakdown(TestMessage entry) {
			}
		};
	}
 
	@Test(expected = NullPointerException.class)
	public void testNullFilterList() {
		underTest.applyFilters("", null);
	}
 
	@Test
	public void testFilteringNullEntry() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(mockMessageFilter);
		when(mockMessageFilter.parse(null)).thenReturn(null);
		TestMessage tm = underTest.applyFilters(null, filters);
		verify(mockMessageFilter, times(1)).parse(anyString());
		assertThat(tm, is(nullValue()));
	}
 
	@Test
	public void testFilteringNonMatchingEntry() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(mockMessageFilter);
		when(mockMessageFilter.parse(anyString())).thenReturn(null);
		TestMessage tm = underTest.applyFilters("A Log Entry", filters);
		verify(mockMessageFilter, times(1)).parse(anyString());
		assertThat(tm, is(nullValue()));
	}
 
	@Test
	public void testFilteringMatchingEntry() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(mockMessageFilter);
		TestMessage tm1 = new TestMessage(1000);
		when(mockMessageFilter.parse(anyString())).thenReturn(tm1);
		TestMessage tm2 = underTest.applyFilters("A Log Entry", filters);
		verify(mockMessageFilter, times(1)).parse(anyString());
		assertThat(tm2, is(equalTo(tm1)));
	}
 
	@Test
	public void testNoMatchingFilters() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(mockMessageFilter);
		filters.add(mockMessageFilter2);
		when(mockMessageFilter.parse(anyString())).thenReturn(null);
		when(mockMessageFilter2.parse(anyString())).thenReturn(null);
		TestMessage tm = underTest.applyFilters("A Log Entry", filters);
		verify(mockMessageFilter, times(1)).parse(anyString());
		verify(mockMessageFilter2, times(1)).parse(anyString());
		assertThat(tm, is(nullValue()));
	}
 
	@Test
	public void testFilteringReturnsAtFirstMatchingFilter() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(mockMessageFilter);
		filters.add(mockMessageFilter2);
		filters.add(mockMessageFilter3);
		TestMessage tm1 = new TestMessage(1000);
		when(mockMessageFilter.parse(anyString())).thenReturn(null);
		when(mockMessageFilter2.parse(anyString())).thenReturn(tm1);
		TestMessage tm2 = underTest.applyFilters("A Log Entry", filters);
		verify(mockMessageFilter, times(1)).parse(anyString());
		verify(mockMessageFilter2, times(1)).parse(anyString());
		verify(mockMessageFilter3, times(0)).parse(anyString());
		assertThat(tm2, is(equalTo(tm1)));
	}
}