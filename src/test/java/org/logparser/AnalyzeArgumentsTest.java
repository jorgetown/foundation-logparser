package org.logparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit testing {@link AnalyzeArguments}.
 * 
 * @author jorge.decastro
 */
public class AnalyzeArgumentsTest {
	private AnalyzeArguments analyzeArguments;

	@Before
	public void setUp() {
		analyzeArguments = new AnalyzeArguments();
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateNullArguments() {
		analyzeArguments.validate(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateEmptyArguments() {
		analyzeArguments.validate(new String[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTooManyArguments() {
		analyzeArguments.validate(new String[] { 
				"firstArgument",
				"secondArgument", 
				"thirdArgument", 
				"fourthArgument",
				"fifthArgument",
				"sixthArgument" });
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTooFewArguments() {
		analyzeArguments.validate(new String[] { 
				"firstArgument" });
	}

	@Test
	public void validateMandatoryArguments() {
		analyzeArguments.validate(new String[] { "firstArgument", "secondArgument" });
	}

	@Test
	public void validateOptionalArguments() {
		analyzeArguments.validate(new String[] { 
				"firstArgument",
				"secondArgument", 
				"thirdArgument", 
				"fourthArgument",
				"fifthArgument" });
	}

	@Test
	public void extractPaths() {
		analyzeArguments.parse(new String[] { ".", "test.log" });

		assertEquals(Arrays.toString(new String[] {"."}), Arrays.toString(analyzeArguments.getPaths()));
	}

	@Test(expected = PatternSyntaxException.class)
	public void parseInvalidMandatorySecondArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "*";
		analyzeArguments.parse(new String[] { ARG_FILENAME, ARG_PATTERN });
	}
	
	@Test(expected = PatternSyntaxException.class)
	public void parseInvalidOptionalThirdArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "*";
		analyzeArguments.parse(new String[] { ".", ARG_FILENAME, ARG_PATTERN });
	}

	@Test
	public void parseValidOptionalSecondArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";

		analyzeArguments.parse(new String[] { ".", ARG_FILENAME, ARG_PATTERN });

		assertTrue(analyzeArguments.getPattern().matcher("save.do").matches());
		assertTrue(analyzeArguments.getPattern().matcher("reload.do").matches());
		assertFalse(analyzeArguments.getPattern().matcher("edit.do").matches());
	}

	@Test
	public void parseMissingOptionalThirdArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";

		analyzeArguments.parse(new String[] { ".", ARG_FILENAME, ARG_PATTERN });
	}

	@Test
	public void parseValidOptionalTimeArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";
		String ARG_TIME_AFTER = "17:35";
		String ARG_TIME_BEFORE = "19:11";

		analyzeArguments.parse(new String[] { 
				".", 
				ARG_FILENAME, 
				ARG_PATTERN,
				ARG_TIME_AFTER, 
				ARG_TIME_BEFORE });

		assertEquals(17, analyzeArguments.getAfter().getHour());
		assertEquals(35, analyzeArguments.getAfter().getMinute());
		assertEquals(19, analyzeArguments.getBefore().getHour());
		assertEquals(11, analyzeArguments.getBefore().getMinute());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseInvalidOptionalTimeArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";
		String ARG_TIME_AFTER = "1a:35";
		String ARG_TIME_BEFORE = "19:b1";

		analyzeArguments.parse(new String[] { 
				".", 
				ARG_FILENAME, 
				ARG_PATTERN,
				ARG_TIME_AFTER, 
				ARG_TIME_BEFORE });
	}

	private static String makeOSIndependentPath() {
		return makeOSIndependentFilepath("");
	}

	private static String makeOSIndependentFilepath(final String filename) {
		String ARG_FILENAME = System.getProperty("user.home");
		ARG_FILENAME += System.getProperty("file.separator");
		return ARG_FILENAME += filename;
	}
}
