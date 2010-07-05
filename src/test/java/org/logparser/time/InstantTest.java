package org.logparser.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * Unit-tests for time {@link Instant}s.
 * 
 * @author jorge.decastro
 * 
 */
public class InstantTest {
	private Instant underTest;

	@Test(expected = NumberFormatException.class)
	public void testNullArgumentIsUnparsable() {
		underTest = new Instant(null, null);
		assertThat(underTest, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInstantFactoryFromMalformedArgument() {
		underTest = Instant.valueOf("153:a");
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testInstantFactoryFromValidArgument() {
		underTest = Instant.valueOf("14:30");
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getHour(), is(equalTo(14)));
		assertThat(underTest.getMinute(), is(equalTo(30)));
	}
}