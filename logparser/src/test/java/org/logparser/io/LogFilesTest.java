package org.logparser.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Tests for {@link LogFiles}.
 * 
 * @author jorge.decastro
 * 
 */
public class LogFilesTest {
	private static final String FILENAME_PATTERN = ".*.log$";
	private static final String[] BASE_DIRS = new String[] { "." };

	@Test(expected = IllegalArgumentException.class)
	public void testNullFilenamePatternArgument() {
		new LogFiles(null, BASE_DIRS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyFilenamePatternArgument() {
		new LogFiles("", BASE_DIRS);
	}

	@Test
	public void testNullBaseDirsArgumentReturnsDefault() {
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, null);
		assertThat(underTest.getBaseDirs(), is(equalTo(BASE_DIRS)));
	}

	@Test
	public void testEmptyBaseDirsArgumentReturnsDefault() {
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, new String[] {});
		assertThat(underTest.getBaseDirs(), is(equalTo(BASE_DIRS)));
	}

	@Test
	public void testMultipleBaseDirsArgument() {
		String aDir = "/a/dir";
		String bDir = "/b/dir";
		String[] dirs = new String[] { aDir, bDir };
		LogFiles underTest = new LogFiles(FILENAME_PATTERN, dirs);
		assertThat(underTest.getBaseDirs(), is(equalTo(dirs)));
	}
}
