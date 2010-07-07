package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
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
	private static final String ORIGINAL_ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
	private static final String ACTION = "statusCheck.do";
	private static final String ANOTHER_ACTION = "edit.do";
	private static final String DURATION = "2073.0";
	private Calendar calendar;
	private LogEntry x;
	private LogEntry y;
	private LogEntry z;
	private LogEntry notx;

	@Before
	public void setUp() {
		calendar = Calendar.getInstance();
		calendar.set(2008, 11, 15, 17, 15, 00);
		Date date = calendar.getTime();
		underTest = new LogEntry(ORIGINAL_ENTRY, date, ACTION, DURATION);
		x = new LogEntry(ORIGINAL_ENTRY, date, ACTION, DURATION);
		y = new LogEntry(ORIGINAL_ENTRY, date, ACTION, DURATION);
		z = new LogEntry(ORIGINAL_ENTRY, date, ACTION, DURATION);
		notx = new LogEntry(ORIGINAL_ENTRY, date, ANOTHER_ACTION, DURATION);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testLogEntryImmutability() {
		// only need to test mutability w/ {@link Dates} since the other arguments are immutable
		Date d = underTest.getDate();
		d.setMinutes(30);
		assertFalse("External date change mutated object", d.equals(underTest.getDate()));
		assertEquals(15, underTest.getDate().getMinutes());
	}

	@Test
	public void testEqualsToSelf() {
		assertTrue("Class equal to itself.", x.equals(x));
	}

	@Test
	public void testIncompatibleTypeIsFalse() {
		assertFalse("Passing incompatible object to equals should return false", x.equals("string"));
	}

	@Test
	public void testNullReferenceIsFalse() {
		assertFalse("Passing null to equals should return false", x.equals(null));
	}

	@Test
	public void testEqualsIsReflexiveAndSymmetric() {
		assertTrue("Reflexive test fail x,y", x.equals(y));
		assertTrue("Symmetric test fail y", y.equals(x));

	}

	@Test
	public void testEqualsIsTransitive() {
		assertTrue("Transitive test fails x,y", x.equals(y));
		assertTrue("Transitive test fails y,z", y.equals(z));
		assertTrue("Transitive test fails x,z", x.equals(z));
	}

	@Test
	public void testEqualsIsConsistent() {
		assertTrue("Consistent test fail x,y", x.equals(y));
		assertTrue("Consistent test fail x,y", x.equals(y));
		assertTrue("Consistent test fail x,y", x.equals(y));
		assertFalse(notx.equals(x));
		assertFalse(notx.equals(x));
		assertFalse(notx.equals(x));
	}

	@Test
	public void testHashcodeIsConsistent() {
		int hashcode = x.hashCode();
		assertEquals("Consistent hashcode test fails", hashcode, x.hashCode());
		assertEquals("Consistent hashcode test fails", hashcode, x.hashCode());
	}

	@Test
	public void testTwoEqualObjectsProduceSameHashcode() {
		int xhashcode = x.hashCode();
		int yhashcode = y.hashCode();
		assertEquals("Equal object, return equal hashcode test fails", xhashcode, yhashcode);
	}

	@Test
	public void testTwoDifferentObjectsProduceDifferentHashcode() {
		int xhashcode = x.hashCode();
		int yhashcode = notx.hashCode();
		assertTrue("Equal object, return unequal hashcode test fails", !(xhashcode == yhashcode));
	}

	@Test
	public void testToCsvString() {
		assertEquals(String.format("\"%s\", \"%s\", %s", underTest.getDate(), underTest.getAction(), underTest.getDuration()), underTest.toCsvString());
	}

	@Test
	public void testJsonString() throws JsonGenerationException, JsonMappingException, IOException {
		Date date = new Date();
		underTest = new LogEntry("", date, ACTION, DURATION);
		String expected = String.format("{\"timestamp\":%s,\"action\":\"%s\",\"duration\":%s,\"message\":\"%s\"}", date.getTime(), ACTION, DURATION, "");
		assertThat(underTest.toJsonString(), is(equalTo(expected)));
	}
}