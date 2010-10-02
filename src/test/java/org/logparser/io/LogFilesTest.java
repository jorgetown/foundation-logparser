package org.logparser.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.logparser.Constants.DEFAULT_OUTPUT_DIR;

import org.junit.Test;

/**
 * Tests for {@link LogFiles}.
 * 
 * @author jorge.decastro
 * 
 */
public class LogFilesTest {
	private static final String[] INPUT_DIRS = new String[] { DEFAULT_OUTPUT_DIR };
	LogFiles underTest;

	@Test
	public void testLogFilesDefaultValues() {
		underTest = new LogFiles.Builder().build();
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getFilenamePattern().pattern(), is(equalTo(LogFiles.DEFAULT_FILENAME_PATTERN)));
		assertThat(underTest.getInputDirs(), is(equalTo(INPUT_DIRS)));
		assertThat(underTest.getOutputDir(), is(notNullValue()));
		assertThat(underTest.getPreProcessor(), is(notNullValue()));
		assertThat(underTest.getPreProcessor(), is(instanceOf(IPreProcessor.class)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullFilenamePatternArgumentThrows() {
		underTest = new LogFiles.Builder().filenamePattern(null).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testOverrideOfFilenamePatternReturnsTheOverride() {
		String filenamePatternOverride = ".*.log$";
		underTest = new LogFiles.Builder().filenamePattern(filenamePatternOverride).build();
		assertThat(underTest.getFilenamePattern().pattern(), is(equalTo(filenamePatternOverride)));
	}

	@Test(expected = NullPointerException.class)
	public void testNullInputDirsArgumentThrows() {
		underTest = new LogFiles.Builder().inputDirs(null).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testOverrideOfInputDirsArgumentReturnsTheOverride() {
		String[] inputDirsOverride = new String[] {};
		underTest = new LogFiles.Builder().inputDirs(inputDirsOverride).build();
		assertThat(underTest.getInputDirs(), is(equalTo(inputDirsOverride)));
	}

	@Test
	public void testMultipleInputDirsArgument() {
		String aDir = "/a/dir";
		String bDir = "/b/dir";
		String[] dirs = new String[] { aDir, bDir };
		LogFiles underTest = new LogFiles.Builder().inputDirs(dirs).build();
		assertThat(underTest.getInputDirs(), is(equalTo(dirs)));
	}

	@Test(expected = NullPointerException.class)
	public void testNullOutputDirArgumentThrows() {
		underTest = new LogFiles.Builder().outputDir(null).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testOverrideOfOutputDirArgumentReturnsTheOverride() {
		String overrideOutputDir = "test";
		underTest = new LogFiles.Builder().outputDir(overrideOutputDir).build();
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getOutputDir(), containsString(overrideOutputDir));
	}

	@Test(expected = NullPointerException.class)
	public void testNullPreProcessorArgumentThrows() {
		underTest = new LogFiles.Builder().preProcessor(null).build();
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testOverrideOfPreProcessorArgumentReturnsTheOverride() {
		IPreProcessor preProcessorOverride = new IdentityPreProcessor();
		underTest = new LogFiles.Builder().preProcessor(preProcessorOverride).build();
		assertThat(underTest.getPreProcessor(), is(notNullValue()));
		assertThat(underTest.getPreProcessor(), is(equalTo(preProcessorOverride)));
	}
}
