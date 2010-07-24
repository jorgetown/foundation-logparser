package org.logparser.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.logparser.Config;
import org.logparser.ILogEntryFilter;
import org.logparser.TestMessage;

/**
 * Unit tests for {@link LineByLineLogFilter}.
 * 
 * @author jorge.decastro
 * 
 */
public class LineByLineLogFilterTest {

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("unchecked")
	public void testNullFilter() {
		ILogEntryFilter<TestMessage> filter = null;
		new LineByLineLogFilter<TestMessage>(new Config(), filter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<ILogEntryFilter<TestMessage>> filters = null;
		new LineByLineLogFilter<TestMessage>(new Config(), filters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<ILogEntryFilter<TestMessage>> filters = new ArrayList<ILogEntryFilter<TestMessage>>();
		filters.add(null);
		new LineByLineLogFilter<TestMessage>(new Config(), filters);
	}
}
