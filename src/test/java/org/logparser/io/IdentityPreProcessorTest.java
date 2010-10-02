package org.logparser.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IdentityPreProcessor}.
 * 
 * @author jorge.decastro
 * 
 */
public class IdentityPreProcessorTest {
	IdentityPreProcessor underTest;

	@Before
	public void setup() {
		underTest = new IdentityPreProcessor();
	}

	@After
	public void tearDown() {
		underTest = null;
	}

	@Test
	public void testNullListArgumentReturnsNullList() {
		List<File> output = underTest.apply(null);
		assertThat(output, is(nullValue()));
	}

	@Test
	public void testNotNullListArgumentReturnsSameNotNullList() {
		List<File> input = new ArrayList<File>();
		List<File> output = underTest.apply(input);
		assertThat(output, is(notNullValue()));
		assertThat(output, is(equalTo(input)));
	}

	@Test
	public void testListWithFileArgumentReturnsSameListWithFile() {
		List<File> input = new ArrayList<File>();
		File f = new File("");
		input.add(f);
		List<File> output = underTest.apply(input);
		assertThat(output, is(notNullValue()));
		assertThat(output, is(equalTo(input)));
		assertThat(output, hasItem(f));
	}
}
