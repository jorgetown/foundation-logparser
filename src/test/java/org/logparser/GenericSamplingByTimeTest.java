package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.time.TimeComparator;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link GenericSamplingByTime}
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericSamplingByTimeTest {
	private GenericSamplingByTime<TestMessage> sampler;
	@Mock
	private IMessageFilter<TestMessage> filter;
	@Mock
	private TimeComparator<TestMessage> timeComparator;

	@Test(expected = NullPointerException.class)
	public void testNullFilterArgument() {
		new GenericSamplingByTime<TestMessage>(null, timeComparator);
	}

	@Test(expected = NullPointerException.class)
	public void testNullTimeComparatorArgument() {
		new GenericSamplingByTime<TestMessage>(filter, null);
	}

	@Test
	public void testSingleUnfilteredEntry() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		when(filter.parse(anyString())).thenReturn(null);
		TestMessage tm = sampler.parse("Unfiltered Log Entry");
		verify(filter, times(1)).parse(anyString());
		assertThat(tm, is(nullValue()));
	}

	@Test
	public void testMultipleUnfilteredEntries() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		when(filter.parse(anyString())).thenReturn(null);
		TestMessage tm1 = sampler.parse("Unfiltered Log Entry");
		TestMessage tm2 = sampler.parse("Another Unfiltered Log Entry");
		verify(filter, times(2)).parse(anyString());
		assertThat(tm1, is(nullValue()));
		assertThat(tm2, is(nullValue()));
	}

	@Test
	public void testSingleFilteredEntry() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		TestMessage tm = new TestMessage(1000L);
		when(filter.parse(anyString())).thenReturn(tm);
		TestMessage tm2 = sampler.parse("Filtered Log Entry");
		verify(filter, times(1)).parse(anyString());
		assertThat(tm2, is(equalTo(tm)));
	}

	@Test
	public void testMultipleFilteredEntry() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		TestMessage tm1 = new TestMessage(1000L);
		TestMessage tm2 = new TestMessage(2000L);
		when(filter.parse("Filtered Log Entry")).thenReturn(tm1);
		when(filter.parse("Unfiltered Log Entry")).thenReturn(null);
		when(filter.parse("Another Filtered Log Entry")).thenReturn(tm2);
		TestMessage tm4 = sampler.parse("Filtered Log Entry");
		TestMessage tm5 = sampler.parse("Unfiltered Log Entry");
		TestMessage tm6 = sampler.parse("Another Filtered Log Entry");
		verify(filter, times(3)).parse(anyString());
		assertThat(tm4, is(equalTo(tm1)));
		assertThat(tm5, is(nullValue()));
		assertThat(tm6, is(equalTo(tm2)));
	}

	@Test
	public void testMaxFilteredEntry() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		TestMessage tm1 = new TestMessage(6000L);
		TestMessage tm2 = new TestMessage(4000L);
		TestMessage tm3 = new TestMessage(2000L);
		when(filter.parse("Entry 1")).thenReturn(tm1);
		when(filter.parse("Entry 2")).thenReturn(tm2);
		when(filter.parse("Entry 3")).thenReturn(tm3);
		sampler.parse("Entry 1");
		sampler.parse("Entry 2");
		sampler.parse("Entry 3");
		verify(filter, times(3)).parse(anyString());
		assertThat(sampler.getMax(), is(equalTo(tm1)));
	}

	@Test
	public void testMinFilteredEntry() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		TestMessage tm1 = new TestMessage(6000L);
		TestMessage tm2 = new TestMessage(4000L);
		TestMessage tm3 = new TestMessage(2000L);
		when(filter.parse("Entry 1")).thenReturn(tm1);
		when(filter.parse("Entry 2")).thenReturn(tm2);
		when(filter.parse("Entry 3")).thenReturn(tm3);
		sampler.parse("Entry 1");
		sampler.parse("Entry 2");
		sampler.parse("Entry 3");
		verify(filter, times(3)).parse(anyString());
		assertThat(sampler.getMin(), is(equalTo(tm3)));
	}
}
