package org.logparser.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link LogFiles}.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class LogFilesTest {
	private static final String GROUP_NAME = "A Group Name";
	private static final String FILENAME_PATTERN = "*.log$";
	private static final String[] BASE_DIRS = new String[] { "." };

	@Test(expected = IllegalArgumentException.class)
	public void testNullGroupNameArgument() {
		LogFiles underTest = new LogFiles(null, FILENAME_PATTERN, BASE_DIRS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyGroupNameArgument() {
		LogFiles underTest = new LogFiles("", FILENAME_PATTERN, BASE_DIRS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullFilenamePatternArgument() {
		LogFiles underTest = new LogFiles(GROUP_NAME, null, BASE_DIRS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyFilenamePatternArgument() {
		LogFiles underTest = new LogFiles(GROUP_NAME, "", BASE_DIRS);
	}

	@Test(expected = NullPointerException.class)
	public void testNullBaseDirsArgument() {
		LogFiles underTest = new LogFiles(GROUP_NAME, FILENAME_PATTERN, null);
	}
}
