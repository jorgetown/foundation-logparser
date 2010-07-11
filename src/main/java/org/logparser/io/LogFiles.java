package org.logparser.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Represents one or more log files on the file system to be collected for
 * parsing.
 * 
 * @author jorge.decastro
 * 
 */
public class LogFiles {
	private final String groupName;
	private final Pattern filenamePattern;
	private final String[] baseDir;

	public LogFiles(final String groupName, final String filenamePattern, final String[] baseDirs) {
		if (StringUtils.isBlank(groupName)) {
			throw new IllegalArgumentException("'groupName' argument is required.");
		}
		if (StringUtils.isBlank(filenamePattern)) {
			throw new IllegalArgumentException("'filenamePattern' argument is required.");
		}
		Preconditions.checkNotNull(baseDirs);
		this.groupName = groupName;
		this.filenamePattern = Pattern.compile(filenamePattern);
		this.baseDir = baseDirs;
	}

	public String getGroupName() {
		return groupName;
	}

	public File[] list() {
		return list(baseDir, filenamePattern);
	}

	private File[] list(final String[] baseDirs, final Pattern filenamePattern) {
		List<File> listOfFiles = new ArrayList<File>();
		for (String path : baseDirs) {
			File f = new File(path.trim());
			if (!f.exists()) {
				throw new IllegalArgumentException(String.format("Unable to find path to log file '%s'", path));
			}
			File[] contents = f.listFiles();
			for (File file : contents) {
				if (filenamePattern.matcher(file.getName()).matches()) {
					listOfFiles.add(file);
				}
			}
		}
		return listOfFiles.toArray(new File[0]);
	}
}
