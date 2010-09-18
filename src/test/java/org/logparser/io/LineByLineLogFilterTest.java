package org.logparser.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.logparser.ILogEntryFilter;
import org.logparser.LogEntry;

/**
 * Unit tests for {@link LineByLineLogFilter}.
 * 
 * @author jorge.decastro
 * 
 */
public class LineByLineLogFilterTest {

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("unchecked")
	public void testLogFilterIsNotCreatedIfNullFilterArgumentGiven() {
		ILogEntryFilter<LogEntry> filter = null;
		new LineByLineLogFilter<LogEntry>(filter);
	}

	@Test(expected = NullPointerException.class)
	public void testLogFilterIsNotCreatedIfNullListOfFiltersGiven() {
		List<ILogEntryFilter<LogEntry>> filters = null;
		new LineByLineLogFilter<LogEntry>(filters);
	}

	@Test(expected = NullPointerException.class)
	public void testLogFilterIsNotCreatedIfListOfNullFiltersGiven() {
		List<ILogEntryFilter<LogEntry>> filters = new ArrayList<ILogEntryFilter<LogEntry>>();
		filters.add(null);
		new LineByLineLogFilter<LogEntry>(filters);
	}
}
