package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link LogEntry}.
 * 
 * @author jorge.decastro
 * 
 */
public class LogEntryTest {
	private LogEntry underTest;
	private static final String ACTION = "statusCheck.do";
	private static final double DURATION = 2073.0D;
	private Calendar calendar;

	@Before
	public void setUp() {
		calendar = Calendar.getInstance();
		calendar.set(2008, 11, 15, 17, 15, 00);
		long date = calendar.getTimeInMillis();
		underTest = new LogEntry(date, ACTION, DURATION);
	}

	@After
	public void tearDown() {
		calendar = null;
		underTest = null;
	}

	@Test
	public void testEqualityOfIncompatibleTypeIsFalse() {
		assertThat(underTest.equals("string"), is(false));
	}

	@Test
	public void testToCsvString() {
		String csvString = underTest.toCsvString();
		LogEntry fromCsv = underTest.fromCsvString(csvString);
		assertThat(csvString, is(equalTo(fromCsv.toCsvString())));
	}

	@Test
	public void testFromCsvString() {
		String csvString = underTest.toCsvString();
		LogEntry fromCsv = underTest.fromCsvString(csvString);
		assertThat(fromCsv, is(equalTo(underTest)));
	}

	@Test
	public void testToJsonString() {
		String jsonString = underTest.toJsonString();
		LogEntry fromJson = underTest.fromJsonString(jsonString);
		assertThat(jsonString, is(equalTo(fromJson.toJsonString())));
	}

	@Test
	public void testFromJsonString() {
		String jsonString = underTest.toJsonString();
		LogEntry fromJson = underTest.fromJsonString(jsonString);
		assertThat(fromJson, is(equalTo(underTest)));
	}

	@Test
	public void testNullActionArgumentDoesNotCauseHashcodeException() {
		underTest = new LogEntry(new Date().getTime(), null, DURATION);
		underTest.hashCode();
	}

	@Test
	public void testHashcodeIsNotZero() {
		assertThat(underTest.hashCode(), is(not(equalTo(0))));
	}
}
