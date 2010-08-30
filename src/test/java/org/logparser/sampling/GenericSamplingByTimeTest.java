package org.logparser.sampling;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.ILogEntryFilter;
import org.logparser.LogEntry;
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
	private static final String SAMPLE_ENTRY_A = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.a HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_B = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.b HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_C = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.c HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_D = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.d HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_E = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.e HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ENTRY_F = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /action.f HTTP/1.1\" 200 1779 2073";
	private static final String SAMPLE_ACTION_A = "/action.a";
	private static final String SAMPLE_ACTION_B = "/action.b";
	private static final double SAMPLE_DURATION = 2073D;
	private LogEntry entryA;
	private LogEntry entryB;
	private LogEntry entryC;
	private LogEntry entryD;
	private LogEntry entryE;
	private LogEntry entryF;

	private GenericSamplingByTime<LogEntry> underTest;
	@Mock
	private ILogEntryFilter<LogEntry> mockFilter;
	@Mock
	private TimeComparator<LogEntry> mockTimeComparator;

	@Before
	public void setUp() {
		entryA = new LogEntry(1L, SAMPLE_ACTION_A, SAMPLE_DURATION);
		entryB = new LogEntry(2L, SAMPLE_ACTION_A, SAMPLE_DURATION);
		entryC = new LogEntry(3L, SAMPLE_ACTION_A, SAMPLE_DURATION);
		entryD = new LogEntry(4L, SAMPLE_ACTION_B, SAMPLE_DURATION);
		entryE = new LogEntry(5L, SAMPLE_ACTION_B, SAMPLE_DURATION);
		entryF = new LogEntry(6L, SAMPLE_ACTION_B, SAMPLE_DURATION);
		underTest = new GenericSamplingByTime<LogEntry>(mockFilter, mockTimeComparator);
	}
	
	@After
	public void tearDown() {
		entryA = null;
		entryB = null;
		entryC = null;
		entryD = null;
		entryE = null;
		entryF = null;
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testNullFilterArgument() {
		new GenericSamplingByTime<LogEntry>(null, mockTimeComparator);
	}

	@Test(expected = NullPointerException.class)
	public void testNullTimeComparatorArgument() {
		new GenericSamplingByTime<LogEntry>(mockFilter, null);
	}

	@Test
	public void testUnfilteredEntryIsNotSampled() {
		when(mockFilter.parse(anyString())).thenReturn(null);

		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_A);

		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(nullValue()));
	}

	@Test
	public void testMultipleUnfilteredEntriesAreNotSampled() {
		when(mockFilter.parse(anyString())).thenReturn(null);

		entryA = underTest.parse(SAMPLE_ENTRY_A);
		entryB = underTest.parse(SAMPLE_ENTRY_B);

		verify(mockFilter, times(2)).parse(anyString());
		assertThat(entryA, is(nullValue()));
		assertThat(entryB, is(nullValue()));
	}

	@Test
	public void testFirstFilteredEntryIsSampled() {
		when(mockFilter.parse(anyString())).thenReturn(entryA);

		LogEntry sampled = underTest.parse(SAMPLE_ENTRY_A);

		verify(mockFilter, times(1)).parse(anyString());
		assertThat(sampled, is(notNullValue()));
		assertThat(sampled, is(equalTo(entryA)));
	}

	@Test
	public void testFirstFilteredEntriesAreSampled() {
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);

		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_D);

		verify(mockFilter, times(2)).parse(anyString());
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(notNullValue()));
		assertThat(sampledB, is(equalTo(entryD)));
	}

	@Test
	public void testFilteredEntryIsSampledIfWithinTimeInterval() {
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockTimeComparator.isIntervalApart(any(LogEntry.class), any(LogEntry.class))).thenReturn(true);

		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);

		verify(mockFilter, times(2)).parse(anyString());
		verify(mockTimeComparator, times(1)).isIntervalApart(any(LogEntry.class), any(LogEntry.class));
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(notNullValue()));
		assertThat(sampledB, is(equalTo(entryB)));
	}

	@Test
	public void testFilteredEntryIsNotSampledIfNotWithinTimeInterval() {
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(entryC);
		when(mockTimeComparator.isIntervalApart(any(LogEntry.class), any(LogEntry.class))).thenReturn(false);

		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampledC = underTest.parse(SAMPLE_ENTRY_C);

		verify(mockFilter, times(3)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(LogEntry.class), any(LogEntry.class));
		assertThat(sampledA, is(notNullValue()));
		assertThat(sampledA, is(equalTo(entryA)));
		assertThat(sampledB, is(nullValue()));
		assertThat(sampledC, is(nullValue()));
	}

	@Test
	public void testFilteredEntriesAreSampledIfWithinTimeInterval() {
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);
		when(mockFilter.parse(SAMPLE_ENTRY_E)).thenReturn(entryE);
		when(mockTimeComparator.isIntervalApart(any(LogEntry.class),any(LogEntry.class))).thenReturn(true);

		LogEntry sampleA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampleB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampleD = underTest.parse(SAMPLE_ENTRY_D);
		LogEntry sampleE = underTest.parse(SAMPLE_ENTRY_E);

		verify(mockFilter, times(4)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(LogEntry.class), any(LogEntry.class));
		assertThat(sampleA, is(notNullValue()));
		assertThat(sampleA, is(equalTo(entryA)));
		assertThat(sampleB, is(notNullValue()));
		assertThat(sampleB, is(equalTo(entryB)));
		assertThat(sampleD, is(notNullValue()));
		assertThat(sampleD, is(equalTo(entryD)));
		assertThat(sampleE, is(notNullValue()));
		assertThat(sampleE, is(equalTo(entryE)));
	}

	@Test
	public void testFilteredEntriesAreNotSampledIfNotWithinTimeInterval() {
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);
		when(mockFilter.parse(SAMPLE_ENTRY_E)).thenReturn(entryE);
		when(mockTimeComparator.isIntervalApart(any(LogEntry.class), any(LogEntry.class))).thenReturn(false);

		LogEntry sampleA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampleB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampleD = underTest.parse(SAMPLE_ENTRY_D);
		LogEntry sampleE = underTest.parse(SAMPLE_ENTRY_E);

		verify(mockFilter, times(4)).parse(anyString());
		verify(mockTimeComparator, times(2)).isIntervalApart(any(LogEntry.class), any(LogEntry.class));
		assertThat(sampleA, is(notNullValue()));
		assertThat(sampleA, is(equalTo(entryA)));
		assertThat(sampleB, is(nullValue()));
		assertThat(sampleD, is(notNullValue()));
		assertThat(sampleD, is(equalTo(entryD)));
		assertThat(sampleE, is(nullValue()));
	}

	@Test
	public void testMutipleFilteredEntriesAreSampledIfWithinTimeInterval() {
		when(mockFilter.parse(SAMPLE_ENTRY_A)).thenReturn(entryA);
		when(mockFilter.parse(SAMPLE_ENTRY_B)).thenReturn(entryB);
		when(mockFilter.parse(SAMPLE_ENTRY_C)).thenReturn(entryC);
		when(mockFilter.parse(SAMPLE_ENTRY_D)).thenReturn(entryD);
		when(mockFilter.parse(SAMPLE_ENTRY_E)).thenReturn(entryE);
		when(mockFilter.parse(SAMPLE_ENTRY_F)).thenReturn(entryF);
		when(mockTimeComparator.isIntervalApart(entryA, entryB)).thenReturn(false);
		when(mockTimeComparator.isIntervalApart(entryA, entryC)).thenReturn(true);
		when(mockTimeComparator.isIntervalApart(entryD, entryE)).thenReturn(true);
		when(mockTimeComparator.isIntervalApart(entryD, entryF)).thenReturn(false);

		LogEntry sampledA = underTest.parse(SAMPLE_ENTRY_A);
		LogEntry sampledB = underTest.parse(SAMPLE_ENTRY_B);
		LogEntry sampledC = underTest.parse(SAMPLE_ENTRY_C);
		LogEntry sampledD = underTest.parse(SAMPLE_ENTRY_D);
		LogEntry sampledE = underTest.parse(SAMPLE_ENTRY_E);
		LogEntry sampledF = underTest.parse(SAMPLE_ENTRY_F);

		verify(mockFilter, times(6)).parse(anyString());
		verify(mockTimeComparator, times(4)).isIntervalApart(any(LogEntry.class), any(LogEntry.class));
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