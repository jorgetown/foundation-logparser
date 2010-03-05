package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
	public void testSingleFilteredEntry() {
		sampler = new GenericSamplingByTime<TestMessage>(filter, timeComparator);
		TestMessage tm = new TestMessage(1000L);
		when(filter.parse(anyString())).thenReturn(tm);
		sampler.parse("A Log Entry");
		verify(filter, times(1)).parse(anyString());
		assertThat(sampler.getMax(), is(equalTo(tm)));
		assertThat(sampler.getMin(), is(equalTo(tm)));
	}

	private static class TestMessage implements IStatsCapable {
		private final long milliseconds;

		public TestMessage(final long milliseconds) {
			this.milliseconds = milliseconds;
		}

		public long getElapsedTime() {
			return milliseconds;
		}
	}

}
