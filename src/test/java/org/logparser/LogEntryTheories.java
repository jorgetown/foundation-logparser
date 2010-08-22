package org.logparser;

import java.util.Calendar;
import java.util.Date;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;

/**
 * Using {@link LogEntry} data points to test adherence to Object's equals and hashcode contracts.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(Theories.class)
public class LogEntryTheories extends ObjectTheories {
	private static final String ENTRY = "10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /statusCheck.do HTTP/1.1\" 200 1779 2073";
	private static final String ACTION = "statusCheck.do";
	private static final String DURATION = "2073.0";
	private static Calendar calendar = Calendar.getInstance();
	private static Date date;
	
	static {
		calendar.set(2008, 11, 15, 17, 15, 00);
		date = calendar.getTime();
	}

	@DataPoint
	public static LogEntry nullEntry = null;
	@DataPoint
	public static LogEntry x = new LogEntry(ENTRY, date, ACTION, DURATION);
	@DataPoint
	public static LogEntry y = new LogEntry(ENTRY, date, ACTION, DURATION);
	@DataPoint
	public static LogEntry z = x;
	@DataPoint
	public static LogEntry w = new LogEntry(
			"10.118.101.132 - - [15/Dec/2008:17:15:00 +0000] \"POST /edit.do HTTP/1.1\" 200 1779 1100", date, "edit.do", "1100.0");
}
