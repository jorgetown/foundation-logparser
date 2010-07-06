package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.time.TimeComparator;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link GenericSamplingByTime}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericSamplingByTimeTest {
	private GenericSamplingByTime<TestMessage> underTest;
	@Mock
	private IMessageFilter<TestMessage> mockFilter;
	@Mock
	private TimeComparator<TestMessage> mockTimeComparator;

	@Test(expected = NullPointerException.class)
	public void testNullFilterArgument() {
		new GenericSamplingByTime<TestMessage>(null, mockTimeComparator);
	}

	@Test(expected = NullPointerException.class)
	public void testNullTimeComparatorArgument() {
		new GenericSamplingByTime<TestMessage>(mockFilter, null);
	}

	@Test
	public void testSingleUnfilteredEntry() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		when(mockFilter.parse(anyString())).thenReturn(null);
		TestMessage tm = underTest.parse("Unfiltered Log Entry");
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(tm, is(nullValue()));
	}

	@Test
	public void testMultipleUnfilteredEntries() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		when(mockFilter.parse(anyString())).thenReturn(null);
		TestMessage tm1 = underTest.parse("Unfiltered Log Entry");
		TestMessage tm2 = underTest.parse("Another Unfiltered Log Entry");
		verify(mockFilter, times(2)).parse(anyString());
		assertThat(tm1, is(nullValue()));
		assertThat(tm2, is(nullValue()));
	}

	@Test
	public void testSingleFilteredEntry() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm = new TestMessage(1000L);
		when(mockFilter.parse(anyString())).thenReturn(tm);
		TestMessage tm2 = underTest.parse("Filtered Log Entry");
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(tm2, is(equalTo(tm)));
	}

	@Test
	@Ignore
	public void testMultipleFilteredEntry() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage(1000L);
		TestMessage tm2 = new TestMessage(2001L);
		when(mockFilter.parse("Filtered Log Entry")).thenReturn(tm1);
		when(mockFilter.parse("Unfiltered Log Entry")).thenReturn(null);
		when(mockFilter.parse("Another Filtered Log Entry")).thenReturn(tm2);
		TestMessage tm4 = underTest.parse("Filtered Log Entry");
		TestMessage tm5 = underTest.parse("Unfiltered Log Entry");
		TestMessage tm6 = underTest.parse("Another Filtered Log Entry");
		verify(mockFilter, times(3)).parse(anyString());
		assertThat(tm4, is(equalTo(tm1)));
		assertThat(tm5, is(nullValue()));
		assertThat(tm6, is(equalTo(tm2)));
	}
}