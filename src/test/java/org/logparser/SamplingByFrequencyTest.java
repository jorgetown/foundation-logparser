package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
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
	IMessageFilter<TestMessage> mockFilter;

	@Test(expected = NullPointerException.class)
	public void testNullMessageFilter() {
		underTest = new SamplingByFrequency<TestMessage>(null, 1);
	}

	@Test
	public void testUnfilteredEntryIsNotSampled() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 1);
		when(mockFilter.parse(anyString())).thenReturn(null);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinFrequencyRate() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 1);
		TestMessage filtered = new TestMessage(1000);
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(filtered)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinFrequencyRate() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 3); // sample 1 in 3
		TestMessage filtered = new TestMessage(1000);
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		TestMessage sampled = underTest.parse(SAMPLE_ENTRY);
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinFrequencyRate() {
		underTest = new SamplingByFrequency<TestMessage>(mockFilter, 3); // sample 1 in 3
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
		assertThat(sampledList.size(), is(equalTo(3)));
	}
}