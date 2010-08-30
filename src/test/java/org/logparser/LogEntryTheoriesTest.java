package org.logparser;

import java.util.Calendar;
import java.util.Date;

import org.junit.experimental.theories.DataPoint;

/**
 * Using {@link LogEntry} data points to test adherence to Object's equals and hashcode contracts.
 * 
 * @author jorge.decastro
 * 
 */
public class LogEntryTheoriesTest extends ObjectTheories {
	private static final String ACTION = "statusCheck.do";
	private static final double DURATION = 2073.0D;
	private static Calendar calendar = Calendar.getInstance();
	private static long date;
	
	static {
		calendar.set(2008, 11, 15, 17, 15, 00);
		date = calendar.getTimeInMillis();
	}

	@DataPoint
	public static LogEntry nullEntry = null;
	@DataPoint
	public static LogEntry x = new LogEntry(date, ACTION, DURATION);
	@DataPoint
	public static LogEntry y = new LogEntry(date, ACTION, DURATION);
	@DataPoint
	public static LogEntry z = x;
	@DataPoint
	public static LogEntry w = new LogEntry(date, "edit.do", 1100.0D);
	@DataPoint
	public static LogEntry u = new LogEntry(new Date().getTime(), "edit.do", 1100.0D);
	@DataPoint
	public static LogEntry v = new LogEntry(date, "edit.do", 1500.0D);
}
