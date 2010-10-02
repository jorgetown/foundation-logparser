package org.logparser.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.logparser.Constants.DEFAULT_OUTPUT_DIR;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Tests for the {@link Config}.
 * 
 * @author jorge.decastro
 * 
 */
public class ConfigTest {
	private static final String FILENAME_PATTERN = ".*.log";
	private static final String[] INPUT_DIRS = new String[] { DEFAULT_OUTPUT_DIR };
	private Config underTest;
	private FilterProvider filterProvider;
	private LogFilesProvider logFilesProvider;

	@Before
	public void setUp() {
		filterProvider = new FilterProvider(".*", ".*", ".*", ".*", ".*", ".*", new InfiniteTimeInterval(), new InfiniteTimeInterval());
		logFilesProvider = new LogFilesProvider(FILENAME_PATTERN, INPUT_DIRS, DEFAULT_OUTPUT_DIR, null);

		underTest = new Config(filterProvider, logFilesProvider);
		underTest.setFriendlyName("Test Log");
	}

	@After
	public void tearDown() {
		filterProvider = null;
		logFilesProvider = null;
		underTest = null;
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationFailsOnNullRequiredFilterProviderArgument() {
		underTest = new Config(null, logFilesProvider);
		assertThat(underTest, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationFailsOnNullRequiredLogFilesProviderArgument() {
		underTest = new Config(filterProvider, null);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testCreationSucceedsOnPresenceOfRequiredArguments() {
		assertThat(underTest.getFilterProvider(), is(notNullValue()));
		assertThat(underTest.getFilterProvider(), is(equalTo(filterProvider)));
		assertThat(underTest.getLogFilesProvider(), is(notNullValue()));
		assertThat(underTest.getLogFilesProvider(), is(equalTo(logFilesProvider)));
	}
}
