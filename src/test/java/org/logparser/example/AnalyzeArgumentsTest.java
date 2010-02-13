package org.logparser.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		analyzeArguments.validate(new String[] { "firstArgument",
				"secondArgument", "thirdArgument", "fourthArgument", "fifthArgument" });
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateTooFewArguments() {
		analyzeArguments.validate(new String[] { "firstArgument", "secondArgument", "thirdArgument" });
	}

	@Test
	public void validateMandatoryArguments() {
		analyzeArguments.validate(new String[] { "firstArgument", "secondArgument" });
	}

	@Test
	public void validateOptionalArguments() {
		analyzeArguments.validate(new String[] { "firstArgument",
				"secondArgument", "thirdArgument", "fourthArgument" });
	}

	@Test
	public void parseNoDirectoryJustFilename() {
		final String ARG_NO_DIR_JUST_FILENAME = "test.log";

		analyzeArguments.parse(new String[] { ARG_NO_DIR_JUST_FILENAME });

		assertEquals(ARG_NO_DIR_JUST_FILENAME, analyzeArguments.getPathFile());
		assertEquals("", analyzeArguments.getPath());
	}

	@Test
	public void parseDirectoryAndFilename() {
		String DIR = makeOSIndependentPath();
		String ARG_DIR_AND_FILENAME = String.format("%s%s", DIR, "test.log");

		analyzeArguments.parse(new String[] { ARG_DIR_AND_FILENAME });

		assertEquals(ARG_DIR_AND_FILENAME, analyzeArguments.getPathFile());
		assertEquals(DIR, analyzeArguments.getPath());
	}

	@Test(expected = PatternSyntaxException.class)
	public void parseInvalidOptionalSecondArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "*";
		analyzeArguments.parse(new String[] { ARG_FILENAME, ARG_PATTERN });
	}

	@Test
	public void parseMissingOptionalSecondArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");

		analyzeArguments.parse(new String[] { ARG_FILENAME });

		assertTrue(analyzeArguments.getPattern().matcher("save.do").matches());
		assertTrue(analyzeArguments.getPattern().matcher("2836ebbe-cd26-11de-a748-00144feabdc0.html").matches());
		assertTrue(analyzeArguments.getPattern().matcher("notification.comet").matches());
		assertTrue(analyzeArguments.getPattern().matcher("f94e9da6-55ed-11de-ab7e-00144feabdc0.img").matches());
	}

	@Test
	public void parseValidOptionalSecondArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";

		analyzeArguments.parse(new String[] { ARG_FILENAME, ARG_PATTERN });

		assertTrue(analyzeArguments.getPattern().matcher("save.do").matches());
		assertTrue(analyzeArguments.getPattern().matcher("reload.do").matches());
		assertFalse(analyzeArguments.getPattern().matcher("edit.do").matches());
	}

	@Test
	public void parseMissingOptionalThirdArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";

		analyzeArguments.parse(new String[] { ARG_FILENAME, ARG_PATTERN });
	}

	@Test
	public void parseValidOptionalTimeArgument() {
		String ARG_FILENAME = makeOSIndependentFilepath("test.log");
		String ARG_PATTERN = "save.do|reload.do";
		String ARG_TIME_AFTER = "17:35";
		String ARG_TIME_BEFORE = "19:11";

		analyzeArguments.parse(new String[] { ARG_FILENAME, ARG_PATTERN, ARG_TIME_AFTER, ARG_TIME_BEFORE });

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

		analyzeArguments.parse(new String[] { ARG_FILENAME, ARG_PATTERN, ARG_TIME_AFTER, ARG_TIME_BEFORE });
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
