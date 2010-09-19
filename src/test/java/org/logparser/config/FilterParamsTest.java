package org.logparser.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Unit tests for {@link FilterParams}.
 * 
 * @author jorge.decastro
 * 
 */
public class FilterParamsTest {
	private static final String SAMPLE_LOG_ENTRY = "10.117.101.80 - - [15/Dec/2009:00:00:15 +0000] \"GET /example/action/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14";
	private static final String TIMESTAMP_PATTERN = "\\[((.*?))\\]";
	private static final String TIMESTAMP_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final String ACTION_PATTERN = "(?:\\[.*?\\].*\\s)(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))";
	private static final String DURATION_PATTERN = "(\\d+)$";
	private static final String FILTER_PATTERN = ".*save.do$";
	private ITimeInterval timeInterval = new InfiniteTimeInterval();
	private FilterParams underTest;

	@Before
	public void setUp() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN, 
				FILTER_PATTERN,
				timeInterval);
	}

	@After
	public void tearDown() {
		timeInterval = null;
		underTest = null;
		
	}

	@Test
	public void testPresenceOfRequiredProperties() {
		assertThat(underTest.getTimestampFormat(), is(equalTo(TIMESTAMP_FORMAT)));
		assertThat(underTest.getTimestampPattern().pattern(), is(equalTo(TIMESTAMP_PATTERN)));
		assertThat(underTest.getActionPattern().pattern(), is(equalTo(ACTION_PATTERN)));
		assertThat(underTest.getDurationPattern().pattern(), is(equalTo(DURATION_PATTERN)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationFailsIfRequiredTimestampFormatIsNull() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				null, 
				ACTION_PATTERN, 
				DURATION_PATTERN, 
				FILTER_PATTERN,
				timeInterval);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationFailsIfRequiredTimestampPatternIsNull() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				null,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN, 
				FILTER_PATTERN,
				timeInterval);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationFailsIfRequiredActionPatternIsNull() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				null, 
				DURATION_PATTERN, 
				FILTER_PATTERN,
				timeInterval);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationFailsIfRequiredDurationPatternIsNull() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				null, 
				FILTER_PATTERN,
				timeInterval);
	}

	@Test
	public void testOptionalFilterPatternHasDefaultValue() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN, 
				null,
				timeInterval);
		assertThat(underTest.getFilterPattern().pattern(), is(equalTo(FilterParams.DEFAULT_FILTER_PATTERN)));
	}

	@Test
	public void testOverrideOfOptionalFilterPatternReturnsTheOverride() {
		String filterPatternOverride = ".*.action";
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN, 
				filterPatternOverride,
				timeInterval);

		assertThat(underTest.getFilterPattern().pattern(), is(equalTo(filterPatternOverride)));
	}
	
	@Test
	public void testOptionalTimeIntervalHasDefaultValue() {
		underTest = new FilterParams(
				SAMPLE_LOG_ENTRY, 
				TIMESTAMP_PATTERN,
				TIMESTAMP_FORMAT, 
				ACTION_PATTERN, 
				DURATION_PATTERN, 
				FILTER_PATTERN,
				null);
		assertThat(underTest.getTimeInterval(), is(notNullValue()));
		assertThat(underTest.getTimeInterval(), is(instanceOf(ITimeInterval.class)));
	}
}
