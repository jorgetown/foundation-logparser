package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.logparser.FilterConfig.GroupBy;

/**
 * Tests for the {@link FilterConfig}.
 * 
 * @author jorge.decastro
 * 
 */
public class FilterConfigTest {
	private FilterConfig underTest;

	@Before
	public void setUp() {
		underTest = new FilterConfig();
		underTest.setFriendlyName("Test Log");
		underTest.setSampleMessage("[15/Dec/2009:00:00:15 +0000] GET /path/something.html?event=execute&eventId=37087422 HTTP/1.1 200 14 300");
		underTest.setTimestampPattern("^\\[((.*))\\]");
		underTest.setTimestampFormat("dd/MMM/yyyy:HH:mm:ss");
		underTest.setActionPattern("\\[.*\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))");
		underTest.setDurationPattern(".*HTTP.*\\s((\\d)(.*))$");
	}

	@Test
	public void testValidateRequiredProperties() {
		underTest.validate();
		assertThat(underTest.getTimestampFormat(), is(equalTo("dd/MMM/yyyy:HH:mm:ss")));
		assertThat(underTest.getTimestampPattern(), is(equalTo("^\\[((.*))\\]")));
		assertThat(underTest.getActionPattern(), is(equalTo("\\[.*\\].*\\s(((?:\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+))")));
		assertThat(underTest.getDurationPattern(), is(equalTo(".*HTTP.*\\s((\\d)(.*))$")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateRequiredTimestampFormatProperty() {
		underTest.setTimestampFormat(null);
		underTest.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateRequiredTimestampPatternProperty() {
		underTest.setTimestampPattern(null);
		underTest.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateRequiredActionPatternProperty() {
		underTest.setActionPattern(null);
		underTest.validate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateRequiredDurationPatternProperty() {
		underTest.setDurationPattern(null);
		underTest.validate();
	}

	@Test
	public void testDefaultValueOfOptionalFilterPatternProperty() {
		underTest = new FilterConfig();
		assertThat(underTest.getFilterPattern(), is(equalTo(FilterConfig.DEFAULT_FILTER_PATTERN)));
	}

	@Test
	public void testDefaultValueOfOptionalFilenamePatternProperty() {
		underTest = new FilterConfig();
		assertThat(underTest.getFilenamePattern(), is(equalTo(FilterConfig.DEFAULT_FILENAME_PATTERN)));
	}

	@Test
	public void testOverrideOptionalFilterPatternProperty() {
		underTest = new FilterConfig();
		underTest.setFilterPattern("*.action");
		assertThat(underTest.getFilterPattern(), is(equalTo("*.action")));
	}

	@Test
	public void testOverrideOptionalFilenamePatternProperty() {
		underTest = new FilterConfig();
		underTest.setFilenamePattern("*.extension");
		assertThat(underTest.getFilenamePattern(), is(equalTo("*.extension")));
	}

	@Test
	public void testDefaultGroupByProperty() {
		underTest = new FilterConfig();
		assertThat(underTest.getGroupBy(), is(equalTo(GroupBy.HOUR)));
	}

	@Test
	public void testGroupByPropertySetter() {
		underTest = new FilterConfig();
		underTest.setGroupBy(GroupBy.MINUTE);
		assertThat(underTest.getGroupBy(), is(equalTo(GroupBy.MINUTE)));
	}

	@Test
	public void testDefaultGroupByPropertyConversion() {
		underTest = new FilterConfig();
		assertThat(underTest.groupByToCalendar(), is(equalTo(Calendar.HOUR_OF_DAY)));
	}

	@Test
	public void testGroupByPropertyConversion() {
		underTest = new FilterConfig();
		underTest.setGroupBy(GroupBy.MINUTE);
		assertThat(underTest.groupByToCalendar(), is(equalTo(Calendar.MINUTE)));
	}
}
