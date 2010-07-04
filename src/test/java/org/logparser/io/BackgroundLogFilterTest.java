package org.logparser.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.logparser.FilterConfig;
import org.logparser.IMessageFilter;
import org.logparser.TestMessage;

/**
 * Unit tests for {@link BackgroundLogFilter}.
 * 
 * @author jorge.decastro
 * 
 */
public class BackgroundLogFilterTest {

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("unchecked")
	public void testNullFilter() {
		IMessageFilter<TestMessage> filter = null;
		new BackgroundLogFilter<TestMessage>(new FilterConfig(), filter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<IMessageFilter<TestMessage>> filters = null;
		new BackgroundLogFilter<TestMessage>(new FilterConfig(), filters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(null);
		new BackgroundLogFilter<TestMessage>(new FilterConfig(), filters);
	}
}
