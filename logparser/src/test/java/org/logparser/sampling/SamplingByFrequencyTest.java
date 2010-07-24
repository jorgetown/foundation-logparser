package org.logparser.sampling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.ILogEntryFilter;
import org.logparser.TestMessage;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link SamplingByFrequency}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class SamplingByFrequencyTest {
	private static final String SAMPLE_ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
	private SamplingByFrequency<TestMessage> underTest;
	@Mock
	ILogEntryFilter<TestMessage> mockFilter;

	@Test(expected = NullPointerException.class)
	public void testNullMessageFilter() {
		underTest = new SamplingByFrequency<TestMessage>(null, 1);
	}

	@Test
	public void testUnfilteredEntryIsNotSampled() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 1);
		when(mockFilter.parse(anyString())).thenReturn(null);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinSamplingInterval() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 1);
		TestMessage filtered = new TestMessage(1000);
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(filtered)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinSamplingInterval() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 2); // sample every 2nd entry
		TestMessage t1 = new TestMessage(1000);
		TestMessage t2 = new TestMessage(1000);
		TestMessage t3 = new TestMessage(1000);
		when(mockFilter.parse(anyString())).thenReturn(t1);
		when(mockFilter.parse(anyString())).thenReturn(t2);
		when(mockFilter.parse(anyString())).thenReturn(t3);
		TestMessage sampled1 = underTest.parse(SAMPLE_ENTRY);
		TestMessage sampled2 = underTest.parse(SAMPLE_ENTRY);
		TestMessage sampled3 = underTest.parse(SAMPLE_ENTRY);
		verify(mockFilter, times(3)).parse(anyString());
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinSamplingInterval() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 3); // sample every 3rd entry
		TestMessage filtered = new TestMessage(1000);
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		List<TestMessage> sampledList = new ArrayList<TestMessage>();
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		for (int i = 1; i <= 9; i++) {
			if (sampled != null) {
				sampledList.add(sampled);
			}
			sampled = underTest.parse(SAMPLE_ENTRY);
		}
		verify(mockFilter, times(10)).parse(anyString());
		assertThat(sampledList.size(), is(equalTo(3)));
	}
	
	@Test
	public void testMultipleFilteredEntriesAreSampledIfWithinSamplingInterval() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 2); // sample every 2nd entry
		TestMessage a1 = new TestMessage("Action A", 1000);
		TestMessage a2 = new TestMessage("Action A", 2000);
		TestMessage a3 = new TestMessage("Action A", 3000);
		TestMessage b1 = new TestMessage("Action B", 1000);
		TestMessage b2 = new TestMessage("Action B", 2000);
		TestMessage b3 = new TestMessage("Action B", 3000);
		when(mockFilter.parse("Action A")).thenReturn(a1);
		when(mockFilter.parse("Action A")).thenReturn(a2);
		when(mockFilter.parse("Action A")).thenReturn(a3);
		when(mockFilter.parse("Action B")).thenReturn(b1);
		when(mockFilter.parse("Action B")).thenReturn(b2);
		when(mockFilter.parse("Action B")).thenReturn(b3);
		TestMessage sampled1 = underTest.parse("Action A");
		TestMessage sampled2 = underTest.parse("Action A");
		TestMessage sampled3 = underTest.parse("Action A");
		TestMessage sampled4 = underTest.parse("Action B");
		TestMessage sampled5 = underTest.parse("Action B");
		TestMessage sampled6 = underTest.parse("Action B");
		verify(mockFilter, times(6)).parse(anyString());
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
		assertThat(sampled4, is(notNullValue()));
		assertThat(sampled5, is(nullValue()));
		assertThat(sampled6, is(notNullValue()));
	}
}