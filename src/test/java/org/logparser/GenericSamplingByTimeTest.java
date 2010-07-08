package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
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
 * Tests for {@link GenericSamplingByTime}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericSamplingByTimeTest {
	private static final String SAMPLE_ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_A = "SAMPLE ENTRY A";
	private static final String SAMPLE_ENTRY_B = "SAMPLE ENTRY B";
	private static final String SAMPLE_ENTRY_C = "SAMPLE ENTRY C";
	private static final String SAMPLE_ENTRY_D = "SAMPLE ENTRY D";
	private static final String SAMPLE_ENTRY_E = "SAMPLE ENTRY E";
	private static final String SAMPLE_ENTRY_F = "SAMPLE ENTRY E";
	
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
	public void testUnfilteredEntryIsNotSampled() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		
		when(mockFilter.parse(anyString())).thenReturn(null);
		
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testMultipleUnfilteredEntriesAreNotSampled() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		
		when(mockFilter.parse(anyString())).thenReturn(null);
		
		TestMessage tm1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage tm2 = underTest.parse(SAMPLE_ENTRY_B);
		
		verify(mockFilter, times(2)).parse(anyString());
		assertThat(tm1, is(nullValue()));
		assertThat(tm2, is(nullValue()));
	}

	@Test
	public void testFirstFilteredEntryIsSampled() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage filtered = new TestMessage(0);
		
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(filtered)));
	}

	@Test
	public void testFirstFilteredEntriesAreSampled() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm1", 0);
		TestMessage tm2 = new TestMessage("tm2", 0);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		
		TestMessage sampled1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sampled2 = underTest.parse(SAMPLE_ENTRY_B);
		
		verify(mockFilter, times(2)).parse(anyString());
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled1, is(equalTo(tm1)));
		assertThat(sampled2, is(notNullValue()));
		assertThat(sampled2, is(equalTo(tm2)));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinTimeInterval() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm", 0);
		TestMessage tm2 = new TestMessage("tm", 2000);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		when(mockTimeComparator.isIntervalApart(any(TestMessage.class), any(TestMessage.class))).thenReturn(true);
		
		TestMessage sampled1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sampled2 = underTest.parse(SAMPLE_ENTRY_B);
		
		verify(mockFilter, times(2)).parse(anyString());
		verify(mockTimeComparator, times(1)).isIntervalApart(any(TestMessage.class), any(TestMessage.class));
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled1, is(equalTo(tm1)));
		assertThat(sampled2, is(notNullValue()));
		assertThat(sampled2, is(equalTo(tm2)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinTimeInterval() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm", 1000);
		TestMessage tm2 = new TestMessage("tm", 2000);
		TestMessage tm3 = new TestMessage("tm", 3000);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(tm3);
		when(mockTimeComparator.isIntervalApart(any(TestMessage.class), any(TestMessage.class))).thenReturn(false);
		
		TestMessage sampled1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sampled2 = underTest.parse(SAMPLE_ENTRY_B);
		TestMessage sampled3 = underTest.parse(SAMPLE_ENTRY_C);
		
		verify(mockFilter, times(3)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(TestMessage.class), any(TestMessage.class));
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled1, is(equalTo(tm1)));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(nullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinTimeInterval() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm1", 1000);
		TestMessage tm2 = new TestMessage("tm1", 5000);
		TestMessage tm3 = new TestMessage("tm2", 4000);
		TestMessage tm4 = new TestMessage("tm2", 8000);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(tm3);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(tm4);
		when(mockTimeComparator.isIntervalApart(any(TestMessage.class), any(TestMessage.class))).thenReturn(true);
		
		TestMessage sample1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sample2 = underTest.parse(SAMPLE_ENTRY_B);
		TestMessage sample3 = underTest.parse(SAMPLE_ENTRY_C);
		TestMessage sample4 = underTest.parse(SAMPLE_ENTRY_D);
		
		verify(mockFilter, times(4)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(TestMessage.class), any(TestMessage.class));
		assertThat(sample1, is(notNullValue()));
		assertThat(sample1, is(equalTo(tm1)));
		assertThat(sample2, is(notNullValue()));
		assertThat(sample2, is(equalTo(tm2)));
		assertThat(sample3, is(notNullValue()));
		assertThat(sample3, is(equalTo(tm3)));
		assertThat(sample4, is(notNullValue()));
		assertThat(sample4, is(equalTo(tm4)));
	}

	@Test
	public void testFilteredEntriesAreNotSampledIfNotWithinTimeInterval() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm1", 1000);
		TestMessage tm2 = new TestMessage("tm1", 2000);
		TestMessage tm3 = new TestMessage("tm2", 4000);
		TestMessage tm4 = new TestMessage("tm2", 5000);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(tm3);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(tm4);
		when(mockTimeComparator.isIntervalApart(any(TestMessage.class), any(TestMessage.class))).thenReturn(false);
		
		TestMessage sample1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sample2 = underTest.parse(SAMPLE_ENTRY_B);
		TestMessage sample3 = underTest.parse(SAMPLE_ENTRY_C);
		TestMessage sample4 = underTest.parse(SAMPLE_ENTRY_D);
		
		verify(mockFilter, times(4)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(TestMessage.class), any(TestMessage.class));
		assertThat(sample1, is(notNullValue()));
		assertThat(sample1, is(equalTo(tm1)));
		assertThat(sample2, is(nullValue()));
		assertThat(sample3, is(notNullValue()));
		assertThat(sample3, is(equalTo(tm3)));
		assertThat(sample4, is(nullValue()));
	}
	
	@Test
	public void testEntriesWithinTimeIntervalAreSampled() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm1", 1000);
		TestMessage tm2 = new TestMessage("tm1", 2000);
		TestMessage tm3 = new TestMessage("tm1", 5000);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(tm3);
		when(mockTimeComparator.isIntervalApart(tm1, tm2)).thenReturn(false);
		when(mockTimeComparator.isIntervalApart(tm1, tm3)).thenReturn(true);
		
		TestMessage sampled1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sampled2 = underTest.parse(SAMPLE_ENTRY_B);
		TestMessage sampled3 = underTest.parse(SAMPLE_ENTRY_C);
		
		verify(mockFilter, times(3)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(TestMessage.class), any(TestMessage.class));
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled1, is(equalTo(tm1)));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
		assertThat(sampled3, is(equalTo(tm3)));
	}

	@Test
	public void testMutipleFilteredEntriesAreSampledIfWithinTimeInterval() {
		underTest = new GenericSamplingByTime<TestMessage>(mockFilter, mockTimeComparator);
		TestMessage tm1 = new TestMessage("tm1", 1000);
		TestMessage tm2 = new TestMessage("tm1", 2000);
		TestMessage tm3 = new TestMessage("tm1", 5000);
		TestMessage tm4 = new TestMessage("tm2", 5000);
		TestMessage tm5 = new TestMessage("tm2", 7000);
		TestMessage tm6 = new TestMessage("tm2", 8000);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(tm1);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(tm2);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(tm3);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(tm4);
		when(mockFilter.parse(SAMPLE_ENTRY_E)).thenReturn(tm5);
		when(mockFilter.parse(SAMPLE_ENTRY_F)).thenReturn(tm6);
		when(mockTimeComparator.isIntervalApart(tm1, tm2)).thenReturn(false);
		when(mockTimeComparator.isIntervalApart(tm1, tm3)).thenReturn(true);
		when(mockTimeComparator.isIntervalApart(tm4, tm6)).thenReturn(true);
		when(mockTimeComparator.isIntervalApart(tm6, tm5)).thenReturn(false);
		
		TestMessage sampled1 = underTest.parse(SAMPLE_ENTRY_A);
		TestMessage sampled2 = underTest.parse(SAMPLE_ENTRY_B);
		TestMessage sampled3 = underTest.parse(SAMPLE_ENTRY_C);
		TestMessage sampled4 = underTest.parse(SAMPLE_ENTRY_D);
		TestMessage sampled5 = underTest.parse(SAMPLE_ENTRY_E);
		TestMessage sampled6 = underTest.parse(SAMPLE_ENTRY_F);
		
		verify(mockFilter, times(6)).parse(anyString());
		verify(mockTimeComparator, times(4)).isIntervalApart(any(TestMessage.class), any(TestMessage.class));
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled1, is(equalTo(tm1)));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
		assertThat(sampled3, is(equalTo(tm3)));
		assertThat(sampled4, is(notNullValue()));
		assertThat(sampled4, is(equalTo(tm4)));
		assertThat(sampled5, is(notNullValue()));
		assertThat(sampled5, is(equalTo(tm6)));
		assertThat(sampled6, is(nullValue()));
	}
}