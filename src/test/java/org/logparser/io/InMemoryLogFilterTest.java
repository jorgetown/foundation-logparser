package org.logparser.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.logparser.IMessageFilter;
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
		IMessageFilter<TestMessage> messageFilter = null;
		new InMemoryLogFilter<TestMessage>(messageFilter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<IMessageFilter<TestMessage>> messageFilters = null;
		new InMemoryLogFilter<TestMessage>(messageFilters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<IMessageFilter<TestMessage>> messageFilters = new ArrayList<IMessageFilter<TestMessage>>();
		messageFilters.add(null);
		new InMemoryLogFilter<TestMessage>(messageFilters);
	}
}
