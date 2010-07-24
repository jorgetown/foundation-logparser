package org.logparser.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.logparser.Config;
import org.logparser.ILogEntryFilter;
import org.logparser.TestMessage;

/**
 * Unit tests for {@link InMemoryLogFilter}.
 * 
 * @author jorge.decastro
 * 
 */
public class InMemoryLogFilterTest {

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("unchecked")
	public void testNullFilter() {
		ILogEntryFilter<TestMessage> messageFilter = null;
		new InMemoryLogFilter<TestMessage>(new Config(), messageFilter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<ILogEntryFilter<TestMessage>> messageFilters = null;
		new InMemoryLogFilter<TestMessage>(new Config(), messageFilters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<ILogEntryFilter<TestMessage>> messageFilters = new ArrayList<ILogEntryFilter<TestMessage>>();
		messageFilters.add(null);
		new InMemoryLogFilter<TestMessage>(new Config(), messageFilters);
	}
}
