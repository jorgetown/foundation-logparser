package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.logparser.Config.GroupBy;
import org.logparser.io.LogFiles;

/**
 * Tests for the {@link Config}.
 * 
 * @author jorge.decastro
 * 
 */
public class ConfigTest {
	private static final String[] BASE_DIRS = new String[] { "." };
	private Config underTest;

	@Before
	public void setUp() {
		underTest = new Config();
		underTest.setFriendlyName("Test Log");
		underTest.setSampleEntry("[15/Dec/2009:00:00:15 +0000] GET /path/something.html?event=execute&eventId=37087422 HTTP/1.1 200 14 300");
		underTest.setTimestampPattern("^\\[((.*))\\]");
		underTest.setTimestampFormat("dd/MMM/yyyy:HH:mm:ss");
		underTest.setActionPattern("\\[.*\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))");
		underTest.setDurationPattern(".*HTTP.*\\s((\\d)(.*))$");
		underTest.setLogFiles(new LogFiles(".*.log", BASE_DIRS));
	}

	@After
	public void tearDown() {
		underTest = null;
	}

	@Test
	public void testValidationGuaranteesPresenceOfRequiredProperties() {
		underTest.validate();
		assertThat(underTest.getTimestampFormat(), is(equalTo("dd/MMM/yyyy:HH:mm:ss")));
		assertThat(underTest.getTimestampPattern(), is(equalTo("^\\[((.*))\\]")));
		assertThat(underTest.getActionPattern(), is(equalTo("\\[.*\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))")));
		assertThat(underTest.getDurationPattern(), is(equalTo(".*HTTP.*\\s((\\d)(.*))$")));
		assertThat(underTest.getLogFiles(), is(notNullValue()));
		assertThat(underTest.getLogFiles().getBaseDirs(), is(equalTo(BASE_DIRS)));
		assertThat(underTest.getLogFiles().getFilenamePattern().pattern(), is(equalTo(".*.log")));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidationFailsIfRequiredTimestampFormatIsNull() {
		underTest.setTimestampFormat(null);
		underTest.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidationFailsIfRequiredTimestampPatternIsNull() {
		underTest.setTimestampPattern(null);
		underTest.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidationFailsIfRequiredActionPatternIsNull() {
		underTest.setActionPattern(null);
		underTest.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidationFailsIfRequiredDurationPatternIsNull() {
		underTest.setDurationPattern(null);
		underTest.validate();
	}

	@Test
	public void testOptionalFilterPatternHasDefaultValue() {
		underTest = new Config();
		assertThat(underTest.getFilterPattern(), is(equalTo(Config.DEFAULT_FILTER_PATTERN)));
	}

	@Test
	public void testOverrideOfOptionalFilterPatternReturnsTheOverride() {
		underTest = new Config();
		underTest.setFilterPattern("*.action");
		assertThat(underTest.getFilterPattern(), is(equalTo("*.action")));
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

	@Test
	public void testOptionalGroupByPropertyHasDefaultValue() {
		underTest = new Config();
		assertThat(underTest.getGroupBy(), is(equalTo(GroupBy.DAY_OF_MONTH)));
	}

	@Test
	public void testOverrideOfOptionalGroupByPropertyReturnsTheOverride() {
		underTest = new Config();
		underTest.setGroupBy(GroupBy.DAY_OF_WEEK);
		assertThat(underTest.getGroupBy(), is(equalTo(GroupBy.DAY_OF_WEEK)));
	}

	@Test
	public void testGroupByToCalendarConversionHasDefaultValue() {
		underTest = new Config();
		assertThat(underTest.groupByToCalendar(), is(equalTo(Calendar.DAY_OF_MONTH)));
	}

	@Test
	public void testOverrideOfGroupByToCalendarConversionReturnsTheOverride() {
		underTest = new Config();
		underTest.setGroupBy(GroupBy.DAY_OF_WEEK);
		assertThat(underTest.groupByToCalendar(), is(equalTo(Calendar.DAY_OF_WEEK)));
	}
}
