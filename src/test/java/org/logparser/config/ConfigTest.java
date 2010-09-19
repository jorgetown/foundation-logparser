package org.logparser.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.io.LogFiles;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Tests for the {@link Config}.
 * 
 * @author jorge.decastro
 * 
 */
public class ConfigTest {
	private static final String[] BASE_DIRS = new String[] { "." };
	private static final String FILENAME_PATTERN = ".*.log";
	private Config underTest;
	private FilterParams filterParams;

	@Before
	public void setUp() {
		filterParams = new FilterParams(".*", ".*", ".*", ".*", ".*", ".*", new InfiniteTimeInterval());

		underTest = new Config();
		underTest.setFriendlyName("Test Log");
		underTest.setLogFiles(new LogFiles(FILENAME_PATTERN, BASE_DIRS));
		underTest.setFilterParams(filterParams);
	}

	@After
	public void tearDown() {
		underTest = null;
	}

	@Test
	public void testValidationGuaranteesPresenceOfRequiredProperties() {
		underTest.validate();
		assertThat(underTest.getFilterParams(), is(notNullValue()));
		assertThat(underTest.getLogFiles(), is(notNullValue()));
		assertThat(underTest.getLogFiles().getBaseDirs(), is(equalTo(BASE_DIRS)));
		assertThat(underTest.getLogFiles().getFilenamePattern().pattern(), is(equalTo(FILENAME_PATTERN)));
	}

	@Test
	public void testOverrideOfFilenamePatternPropertyReturnsTheOverride() {
		underTest = new Config();
		LogFiles logFiles = new LogFiles(".*.extension", null);
		underTest.setLogFiles(logFiles);
		assertThat(underTest.getLogFiles().getFilenamePattern().pattern(), is(equalTo(".*.extension")));
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
