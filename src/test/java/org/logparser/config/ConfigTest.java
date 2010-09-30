package org.logparser.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

	@Before
	public void setUp() {
		FilterProvider filterProvider = new FilterProvider(".*", ".*", ".*", ".*", ".*", ".*", new InfiniteTimeInterval(), new InfiniteTimeInterval());
		LogFilesProvider logFilesProvider = new LogFilesProvider(FILENAME_PATTERN, INPUT_DIRS, DEFAULT_OUTPUT_DIR, null);

		underTest = new Config();
		underTest.setFriendlyName("Test Log");
		underTest.setLogFilesProvider(logFilesProvider);
		underTest.setFilterProvider(filterProvider);
	}

	@After
	public void tearDown() {
		underTest = null;
	}

	@Test
	public void testValidationGuaranteesPresenceOfRequiredProperties() {
		underTest.validate();
		assertThat(underTest.getFilterProvider(), is(notNullValue()));
		assertThat(underTest.getLogFilesProvider(), is(notNullValue()));
	}

	@Test
	public void testOptionalDecimalFormatHasDefaultValue() {
		underTest = new Config();
		assertThat(underTest.getDecimalFormat(), is(equalTo(Config.DEFAULT_DECIMAL_FORMAT)));
	}

	@Test
	public void testOverrideOfOptionalDecimalFormatReturnsTheOverride() {
		underTest = new Config();
		underTest.setDecimalFormat("##.00");
		assertThat(underTest.getDecimalFormat(), is(equalTo("##.00")));
	}
}
