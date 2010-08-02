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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.ILogEntryFilter;
import org.logparser.LogEntry;
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
	private static final String SAMPLE_ENTRY_A = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.a HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_B = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.b HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_C = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.c HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_D = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.d HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_E = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.e HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_F = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.f HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ACTION_A = "/action.a";
	private static final String SAMPLE_ACTION_B = "/action.b";
	private static final String SAMPLE_DURATION = "2073";

	private SamplingByTime<LogEntry> underTest;
	@Mock
	ILogEntryFilter<LogEntry> mockFilter;

	@Test(expected = NullPointerException.class)
	public void testCreationFailsIfNullMessageFilterGiven() {
		underTest = new SamplingByTime<LogEntry>(null, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testUnfilteredEntryIsNotSampled() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 1, TimeUnit.SECONDS);
		
		when(mockFilter.parse(anyString())).thenReturn(null);
		
		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_A);
		
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testFirstFilteredEntryIsSampled() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 1, TimeUnit.SECONDS);
		LogEntry filtered = new LogEntry(SAMPLE_ENTRY_A, new Date(), SAMPLE_ACTION_A, SAMPLE_DURATION);
		
		when(mockFilter.parse(anyString())).thenReturn(filtered);
		
		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_A);
		
		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(filtered)));
	}

	@Test
	public void testFirstFilteredEntriesAreSampled() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 1, TimeUnit.SECONDS);
		LogEntry entryA = new LogEntry(SAMPLE_ENTRY_A, new Date(), SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryB = new LogEntry(SAMPLE_ENTRY_B, new Date(), SAMPLE_ACTION_B, SAMPLE_DURATION);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		
		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);
		
		verify(mockFilter, times(2)).parse(anyString());
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(notNullValue()));
		assertThat(sampledB, is(equalTo(entryB)));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinTimeInterval() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 1, TimeUnit.SECONDS);
		Date dateA = new Date();
		dateA.setTime(1280589260565L);
		Date dateB = new Date();
		dateB.setTime(1280589262565L); // 1280589260565 + 2000 = 2000ms later
		LogEntry entryA = new LogEntry(SAMPLE_ENTRY_A, dateA, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryB = new LogEntry(SAMPLE_ENTRY_B, dateB, SAMPLE_ACTION_A, SAMPLE_DURATION);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		
		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);
		
		verify(mockFilter, times(2)).parse(anyString());
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(notNullValue()));
		assertThat(sampledB, is(equalTo(entryB)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinTimeInterval() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 3, TimeUnit.SECONDS); // if time interval > 3secs
		Date dateA = new Date();
		dateA.setTime(1280589260565L);
		Date dateB = new Date();
		dateB.setTime(1280589262565L); // 1280589260565 + 2000 = 2000ms later
		Date dateC = new Date();
		dateC.setTime(1280589263565L); // 1280589262565 + 1000 = 1000ms later
		LogEntry entryA = new LogEntry(SAMPLE_ENTRY_A, dateA, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryB = new LogEntry(SAMPLE_ENTRY_B, dateB, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryC = new LogEntry(SAMPLE_ENTRY_C, dateC, SAMPLE_ACTION_A, SAMPLE_DURATION);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(entryC);
		
		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampledC = underTest.parse(SAMPLE_ENTRY_C);
		
		verify(mockFilter, times(3)).parse(anyString());
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(nullValue()));
		assertThat(sampledC, is(nullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinTimeInterval() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 3, TimeUnit.SECONDS); // if time interval > 3secs
		Date dateA = new Date();
		dateA.setTime(1280589260565L);
		Date dateB = new Date();
		dateB.setTime(1280589264565L); // 1280589260565 + 4000 = 4000ms later
		Date dateC = new Date();
		dateC.setTime(1280589268565L); // 1280589264565 + 4000 = 4000ms later
		Date dateD = new Date();
		dateD.setTime(1280589272565L); // 1280589268565 + 4000 = 4000ms later
		LogEntry entryA = new LogEntry(SAMPLE_ENTRY_A, dateA, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryB = new LogEntry(SAMPLE_ENTRY_B, dateB, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryC = new LogEntry(SAMPLE_ENTRY_C, dateC, SAMPLE_ACTION_B, SAMPLE_DURATION);
		LogEntry entryD = new LogEntry(SAMPLE_ENTRY_D, dateD, SAMPLE_ACTION_B, SAMPLE_DURATION);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(entryC);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);
		
		LogEntry sampleA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampleB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampleC = underTest.parse(SAMPLE_ENTRY_C);
		LogEntry sampleD = underTest.parse(SAMPLE_ENTRY_D);
		
		verify(mockFilter, times(4)).parse(anyString());
		assertThat(sampleA, is(notNullValue()));
		assertThat(sampleA, is(equalTo(entryA)));
		assertThat(sampleB, is(notNullValue()));
		assertThat(sampleB, is(equalTo(entryB)));
		assertThat(sampleC, is(notNullValue()));
		assertThat(sampleC, is(equalTo(entryC)));
		assertThat(sampleD, is(notNullValue()));
		assertThat(sampleD, is(equalTo(entryD)));
	}

	@Test
	public void testFilteredEntriesAreNotSampledIfNotWithinTimeInterval() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 3, TimeUnit.SECONDS); // if time interval > 3secs
		Date dateA = new Date();
		dateA.setTime(1280589260565L);
		Date dateB = new Date();
		dateB.setTime(1280589261565L); // 1280589260565 + 1000 = 1000ms later
		Date dateC = new Date();
		dateC.setTime(1280589260565L);
		Date dateD = new Date();
		dateD.setTime(1280589261565L); // 1280589261565 + 1000 = 1000ms later
		LogEntry entryA = new LogEntry(SAMPLE_ENTRY_A, dateA, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryB = new LogEntry(SAMPLE_ENTRY_B, dateB, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryC = new LogEntry(SAMPLE_ENTRY_C, dateC, SAMPLE_ACTION_B, SAMPLE_DURATION);
		LogEntry entryD = new LogEntry(SAMPLE_ENTRY_D, dateD, SAMPLE_ACTION_B, SAMPLE_DURATION);
		
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(entryC);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);
		
		LogEntry sampleA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampleB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampleC = underTest.parse(SAMPLE_ENTRY_C);
		LogEntry sampleD = underTest.parse(SAMPLE_ENTRY_D);
		
		verify(mockFilter, times(4)).parse(anyString());
		assertThat(sampleA, is(notNullValue()));
		assertThat(sampleA, is(equalTo(entryA)));
		assertThat(sampleB, is(nullValue()));
		assertThat(sampleC, is(notNullValue()));
		assertThat(sampleC, is(equalTo(entryC)));
		assertThat(sampleD, is(nullValue()));
	}

	@Test
	public void testMutipleFilteredEntriesAreSampledIfWithinTimeInterval() {
		underTest = new SamplingByTime<LogEntry>(mockFilter, 2, TimeUnit.SECONDS); // if time interval > 2secs
		Date dateA = new Date();
		dateA.setTime(1280589260565L);
		Date dateB = new Date();
		dateB.setTime(1280589261565L); // 1280589260565 + 1000 = 1000ms later
		Date dateC = new Date();
		dateC.setTime(1280589264565L); // 1280589261565 + 3000 = 3000ms later
		Date dateD = new Date();
		dateD.setTime(1280589260565L);
		Date dateE = new Date();
		dateE.setTime(1280589263565L); // 1280589260565 + 3000 = 3000ms later
		Date dateF = new Date();
		dateF.setTime(1280589264565L); // 1280589263565 + 1000 = 1000ms later
		LogEntry entryA = new LogEntry(SAMPLE_ENTRY_A, dateA, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryB = new LogEntry(SAMPLE_ENTRY_B, dateB, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryC = new LogEntry(SAMPLE_ENTRY_C, dateC, SAMPLE_ACTION_A, SAMPLE_DURATION);
		LogEntry entryD = new LogEntry(SAMPLE_ENTRY_D, dateD, SAMPLE_ACTION_B, SAMPLE_DURATION);
		LogEntry entryE = new LogEntry(SAMPLE_ENTRY_E, dateE, SAMPLE_ACTION_B, SAMPLE_DURATION);
		LogEntry entryF = new LogEntry(SAMPLE_ENTRY_F, dateF, SAMPLE_ACTION_B, SAMPLE_DURATION);

		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(entryC);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);
		when(mockFilter.parse(SAMPLE_ENTRY_E)).thenReturn(entryE);
		when(mockFilter.parse(SAMPLE_ENTRY_F)).thenReturn(entryF);
		
		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampledC = underTest.parse(SAMPLE_ENTRY_C);
		LogEntry sampledD = underTest.parse(SAMPLE_ENTRY_D);
		LogEntry sampledE = underTest.parse(SAMPLE_ENTRY_E);
		LogEntry sampledF = underTest.parse(SAMPLE_ENTRY_F);
		
		verify(mockFilter, times(6)).parse(anyString());
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(nullValue()));
		assertThat(sampledC, is(notNullValue()));
		assertThat(sampledC, is(equalTo(entryC)));
		assertThat(sampledD, is(notNullValue()));
		assertThat(sampledD, is(equalTo(entryD)));
		assertThat(sampledE, is(notNullValue()));
		assertThat(sampledE, is(equalTo(entryE)));
		assertThat(sampledF, is(nullValue()));
	}
}