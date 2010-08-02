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
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.ILogEntryFilter;
import org.logparser.LogEntry;
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
	private static final String SAMPLE_ENTRY_1 = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.1 HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_2 = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.2 HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_3 = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.3 HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_4 = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.4 HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_5 = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.5 HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_6 = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.6 HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ACTION_1 = "/action.1";
	private static final String SAMPLE_ACTION_2 = "/action.2";
	private static final String SAMPLE_DURATION = "2073";

	private SamplingByFrequency<LogEntry> underTest;

	@Mock
	ILogEntryFilter<LogEntry> mockFilter;

	@Test(expected = NullPointerException.class)
	public void testNullMessageFilter() {
		underTest = new SamplingByFrequency<LogEntry>(null, 1);
	}

	@Test
	public void testUnfilteredEntryIsNotSampled() {
		underTest = new SamplingByFrequency<LogEntry>(mockFilter, 1);

		when(mockFilter.parse(anyString())).thenReturn(null);

		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_1);

		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinSamplingInterval() {
		underTest = new SamplingByFrequency<LogEntry>(mockFilter, 1);

		LogEntry entry = new LogEntry(SAMPLE_ENTRY_1, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);

		when(mockFilter.parse(anyString())).thenReturn(entry);

		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_1);

		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(entry)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinSamplingInterval() {
		underTest = new SamplingByFrequency<LogEntry>(mockFilter, 2); // sample every 2nd entry
		LogEntry entry1 = new LogEntry(SAMPLE_ENTRY_1, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);
		LogEntry entry2 = new LogEntry(SAMPLE_ENTRY_2, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);
		LogEntry entry3 = new LogEntry(SAMPLE_ENTRY_3, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);

		when(mockFilter.parse(anyString())).thenReturn(entry1);
		when(mockFilter.parse(anyString())).thenReturn(entry2);
		when(mockFilter.parse(anyString())).thenReturn(entry3);

		LogEntry sampled1 = underTest.parse(SAMPLE_ENTRY_1);
		LogEntry sampled2 = underTest.parse(SAMPLE_ENTRY_2);
		LogEntry sampled3 = underTest.parse(SAMPLE_ENTRY_3);

		verify(mockFilter, times(3)).parse(anyString());
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinSamplingInterval() {
		underTest = new SamplingByFrequency<LogEntry>(mockFilter, 3); // sample every 3rd entry
		LogEntry entry = new LogEntry(SAMPLE_ENTRY_1, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);

		when(mockFilter.parse(anyString())).thenReturn(entry);

		List<LogEntry> sampledList = new ArrayList<LogEntry>();
		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_1);
		for (int i = 1; i <= 9; i++) {
			if (sampled != null) {
				sampledList.add(sampled);
			}
			sampled = underTest.parse(SAMPLE_ENTRY_1);
		}

		verify(mockFilter, times(10)).parse(anyString());
		assertThat(sampledList.size(), is(equalTo(3)));
	}

	@Test
	public void testMultipleFilteredEntriesAreSampledIfWithinSamplingInterval() {
		underTest = new SamplingByFrequency<LogEntry>(mockFilter, 2); // sample every 2nd entry
		LogEntry entry1 = new LogEntry(SAMPLE_ENTRY_1, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);
		LogEntry entry2 = new LogEntry(SAMPLE_ENTRY_2, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);
		LogEntry entry3 = new LogEntry(SAMPLE_ENTRY_3, new Date(), SAMPLE_ACTION_1, SAMPLE_DURATION);
		LogEntry entry4 = new LogEntry(SAMPLE_ENTRY_4, new Date(), SAMPLE_ACTION_2, SAMPLE_DURATION);
		LogEntry entry5 = new LogEntry(SAMPLE_ENTRY_5, new Date(), SAMPLE_ACTION_2, SAMPLE_DURATION);
		LogEntry entry6 = new LogEntry(SAMPLE_ENTRY_6, new Date(), SAMPLE_ACTION_2, SAMPLE_DURATION);

		when(mockFilter.parse(SAMPLE_ENTRY_1)).thenReturn(entry1);
		when(mockFilter.parse(SAMPLE_ENTRY_2)).thenReturn(entry2);
		when(mockFilter.parse(SAMPLE_ENTRY_3)).thenReturn(entry3);
		when(mockFilter.parse(SAMPLE_ENTRY_4)).thenReturn(entry4);
		when(mockFilter.parse(SAMPLE_ENTRY_5)).thenReturn(entry5);
		when(mockFilter.parse(SAMPLE_ENTRY_6)).thenReturn(entry6);

		LogEntry sampled1 = underTest.parse(SAMPLE_ENTRY_1);
		LogEntry sampled2 = underTest.parse(SAMPLE_ENTRY_2);
		LogEntry sampled3 = underTest.parse(SAMPLE_ENTRY_3);
		LogEntry sampled4 = underTest.parse(SAMPLE_ENTRY_4);
		LogEntry sampled5 = underTest.parse(SAMPLE_ENTRY_5);
		LogEntry sampled6 = underTest.parse(SAMPLE_ENTRY_6);

		verify(mockFilter, times(6)).parse(anyString());
		assertThat(sampled1, is(notNullValue()));
		assertThat(sampled2, is(nullValue()));
		assertThat(sampled3, is(notNullValue()));
		assertThat(sampled4, is(notNullValue()));
		assertThat(sampled5, is(nullValue()));
		assertThat(sampled6, is(notNullValue()));
	}
}