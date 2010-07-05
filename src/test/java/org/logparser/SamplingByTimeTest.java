package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link SamplingByTime}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class SamplingByTimeTest {
	private static final String SAMPLE_ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
	private SamplingByTime<TestMessage> underTest;
	@Mock
	IMessageFilter<TestMessage> mockFilter;

	@Test(expected = NullPointerException.class)
	public void testNullMessageFilter() {
		underTest = new SamplingByTime<TestMessage>(null, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testUnfilteredEntryIsNotSampled() {
		underTest = new SamplingByTime<TestMessage>(mockFilter, 1, TimeUnit.SECONDS);
		when(mockFilter.parse(anyString())).thenReturn(null);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinTimeInterval() {
		underTest = new SamplingByTime<TestMessage>(mockFilter, 1, TimeUnit.SECONDS);
		TestMessage filtered = new TestMessage(1000);
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(filtered)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinTimeInterval() {
		underTest = new SamplingByTime<TestMessage>(mockFilter, 3, TimeUnit.SECONDS); // if time interval > 3secs
		TestMessage t1 = new TestMessage(1000);
		TestMessage t2 = new TestMessage(2000);
		TestMessage t3 = new TestMessage(3000);
		when(mockFilter.parse("t1")).thenReturn(t1);
		when(mockFilter.parse("t2")).thenReturn(t2);
		when(mockFilter.parse("t3")).thenReturn(t3);
		TestMessage sampled1 = underTest.parse("t1");
		TestMessage sampled2 = underTest.parse("t2");
		TestMessage sampled3 = underTest.parse("t3");
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(nullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinTimeInterval() {
		underTest = new SamplingByTime<TestMessage>(mockFilter, 3, TimeUnit.SECONDS); // if time interval > 3secs
		TestMessage t1 = new TestMessage(1000);
		TestMessage t2 = new TestMessage(2000);
		TestMessage t3 = new TestMessage(7000);
		when(mockFilter.parse("t1")).thenReturn(t1);
		when(mockFilter.parse("t2")).thenReturn(t2);
		when(mockFilter.parse("t3")).thenReturn(t3);
		TestMessage sampled1 = underTest.parse("t1");
		TestMessage sampled2 = underTest.parse("t2");
		TestMessage sampled3 = underTest.parse("t3");
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled1, is(equalTo(t1)));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
		assertThat(sampled3, is(equalTo(t3)));
	}
}