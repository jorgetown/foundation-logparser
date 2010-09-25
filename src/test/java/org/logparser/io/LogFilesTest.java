package org.logparser.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.logparser.Constants.DEFAULT_OUTPUT_DIR;

import org.junit.Test;

/**
 * Tests for {@link LogFiles}.
 * 
 * @author jorge.decastro
 * 
 */
public class LogFilesTest {
	private static final String FILENAME_PATTERN = ".*.log$";
	private static final String[] BASE_DIRS = new String[] { DEFAULT_OUTPUT_DIR };

	@Test(expected = IllegalArgumentException.class)
	public void testNullFilenamePatternArgument() {
		new LogFiles(null, BASE_DIRS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyFilenamePatternArgument() {
		new LogFiles("", BASE_DIRS);
	}

	@Test
	public void testNullInputDirsArgumentReturnsDefault() {
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, null);
		assertThat(underTest.getInputDirs(), is(equalTo(BASE_DIRS)));
	}

	@Test
	public void testEmptyInputDirsArgumentReturnsDefault() {
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, new String[] {});
		assertThat(underTest.getInputDirs(), is(equalTo(BASE_DIRS)));
	}

	@Test
	public void testMultipleInputDirsArgument() {
		String aDir = "/a/dir";
		String bDir = "/b/dir";
		String[] dirs = new String[] { aDir, bDir };
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, dirs);
		assertThat(underTest.getInputDirs(), is(equalTo(dirs)));
	}
	
	@Test
	public void testNullOutputDirArgumentReturnsDefault() {
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, BASE_DIRS, null);
		assertThat(underTest.getOutputDir(), is(equalTo(DEFAULT_OUTPUT_DIR)));
	}

	@Test
	public void testEmptyOutputDirArgumentReturnsDefault() {
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, BASE_DIRS, "");
		assertThat(underTest.getOutputDir(), is(equalTo(DEFAULT_OUTPUT_DIR)));
	}
}
