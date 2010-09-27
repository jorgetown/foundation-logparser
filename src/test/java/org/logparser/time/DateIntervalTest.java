package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit-tests for {@link DateInterval}.
 * 
 * @author jorge.decastro
 * 
 */
public class DateIntervalTest {
	private Calendar cal;
	private Date lastWeek;
	private Date nextWeek;
	private DateInterval underTest;

	@Before
	public void setUp() {
		cal = Calendar.getInstance();
		Date today = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, -7);
		lastWeek = cal.getTime();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, 7);
		nextWeek = cal.getTime();
		underTest = new DateInterval(lastWeek, nextWeek);
	}

	@After
	public void tearDown() {
		cal = null;
		lastWeek = null;
		nextWeek = null;
		underTest = null;
	}

	@Test(expected = NullPointerException.class)
	public void testNullDateIsNotBetweenDateInterval() {
		underTest.isBetweenInstants(null);

		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testDateIsBetweenDateInterval() {
		Date today = new Date();
		boolean isBetween = underTest.isBetweenInstants(today);

		assertThat(isBetween, is(true));
	}

	@Test
	public void testDateIsNotBetweenDateInterval() {
		Date today = new Date();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date before = cal.getTime();
		underTest = new DateInterval(lastWeek, before);

		boolean isBetween = underTest.isBetweenInstants(today);

		assertThat(isBetween, is(false));
	}

	@Test
	public void testNullDateAfterReturnsInfiniteEndpoint() {
		Date today = new Date();
		String before = "3010/01/01";
		String after = null;
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		cal.add(Calendar.YEAR, -100);
		underTest = DateInterval.valueOf(after, before);
		boolean isBetween = underTest.isBetweenInstants(today);
		assertThat(isBetween, is(true));
		assertThat(underTest.getAfter(), is(not(nullValue())));
		assertThat(DateInterval.formatDate(underTest.getAfter()), is(equalTo(DateInterval.formatDate(cal.getTime()))));
	}

	@Test
	public void testNullDateBeforeReturnsInfiniteEndpoint() {
		Date today = new Date();
		String before = null;
		String after = "1000/01/01";
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		cal.add(Calendar.YEAR, 100);
		underTest = DateInterval.valueOf(after, before);
		boolean isBetween = underTest.isBetweenInstants(today);
		assertThat(isBetween, is(true));
		assertThat(underTest.getBefore(), is(not(nullValue())));
		assertThat(DateInterval.formatDate(underTest.getBefore()), is(equalTo(DateInterval.formatDate(cal.getTime()))));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnparsableDateArgument() {
		Date today = new Date();
		String before = null;
		String after = "1000-01-01";
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		cal.add(Calendar.YEAR, 100);
		underTest = DateInterval.valueOf(after, before);
		assertThat(underTest, is(nullValue()));
	}
}
