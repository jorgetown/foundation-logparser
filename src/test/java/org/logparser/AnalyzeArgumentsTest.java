package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit testing {@link AnalyzeArguments}.
 * 
 * @author jorge.decastro
 */
public class AnalyzeArgumentsTest {
	private AnalyzeArguments underTest;

	@Before
	public void setUp() {
		underTest = new AnalyzeArguments();
	}

	@Test(expected = NullPointerException.class)
	public void testValidateNullArguments() {
		underTest.validate(null);
	}

	@Test
	public void testValidateEmptyArguments() {
		underTest.validate(new String[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateTooManyArguments() {
		underTest.validate(new String[] { "firstArgument", "secondArgument" });
	}

	@Test
	public void testValidateTooFewArguments() {
		underTest.validate(new String[] {});
	}

	@Test
	public void testValidateSingleArgument() {
		underTest.validate(new String[] { "singleArgument" });
	}

	@Test
	public void testDefaultPathToconfigFile() {
		assertThat(underTest.getPathToConfig(), is(equalTo(AnalyzeArguments.DEFAULT_CONFIG_FILE)));
	}
}
