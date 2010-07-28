package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

/**
 * Tests for {@link StatsSnapshot}.
 * 
 * @author jorge.decastro
 * 
 */
public class StatsSnapshotTest {
	private StatsSnapshot<TestMessage> underTest;

	@Test
	public void testJsonString() throws JsonGenerationException, JsonMappingException, IOException {
		underTest = new StatsSnapshot<TestMessage>();
		String expected = "{\"mean\":0.0,\"deviation\":0.0,\"maxima\":null,\"minima\":null,\"timeBreakdown\":{},\"entries\":[]}";
		assertThat(underTest.toJsonString(), is(equalTo(expected)));
	}
}
