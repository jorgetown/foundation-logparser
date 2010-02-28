package org.logparser.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logparser.IMessageFilter;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link BackgroundLogParser}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class BackgroundLogParserTest {

	@Mock
	private IMessageFilter<TestMessage> filter;

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("unchecked")
	public void testNullFilter() {
		IMessageFilter<TestMessage> filter = null;
		new BackgroundLogParser<TestMessage>(filter);
	}

	@Test(expected = NullPointerException.class)
	public void testNullListOfFilters() {
		List<IMessageFilter<TestMessage>> filters = null;
		new BackgroundLogParser<TestMessage>(filters);
	}

	@Test(expected = NullPointerException.class)
	public void testListOfNullFilters() {
		List<IMessageFilter<TestMessage>> filters = new ArrayList<IMessageFilter<TestMessage>>();
		filters.add(null);
		new BackgroundLogParser<TestMessage>(filters);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNotNullFilter() {
		BackgroundLogParser<TestMessage> parser = new BackgroundLogParser<TestMessage>(filter);
		assertThat(parser.getTotalEntries(), is(equalTo(0)));
		assertThat(parser.getParsedEntries().size(), is(equalTo(0)));
		assertThat(parser.getEarliestEntry(), is(nullValue()));
		assertThat(parser.getLatestEntry(), is(nullValue()));
	}

	private static class TestMessage {

	}
}
