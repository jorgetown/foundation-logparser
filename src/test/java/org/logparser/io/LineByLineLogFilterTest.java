package org.logparser.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.logparser.IMessageFilter;
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
		IMessageFilter<TestMessage> filter = null;
		new LineByLineLogFilter<TestMessage>(filter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<IMessageFilter<TestMessage>> filters = null;
		new LineByLineLogFilter<TestMessage>(filters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(null);
		new LineByLineLogFilter<TestMessage>(filters);
	}
}
