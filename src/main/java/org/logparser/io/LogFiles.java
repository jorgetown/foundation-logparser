package org.logparser.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents one or more log files on the file system, to be collected for
 * processing.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class LogFiles {
	private static final Logger LOGGER = Logger.getLogger(LogFiles.class.getName());
	private final Pattern filenamePattern;
	private final String[] baseDir;

	@JsonCreator
	public LogFiles(@JsonProperty("filenamePattern") final String filenamePattern, @JsonProperty("baseDirs") final String[] baseDirs) {
		if (StringUtils.isBlank(filenamePattern)) {
			throw new IllegalArgumentException("'filenamePattern' argument is required.");
		}
		this.filenamePattern = Pattern.compile(filenamePattern);
		if (ArrayUtils.isEmpty(baseDirs)) {
			this.baseDir = new String[] { "." };
		} else {
			this.baseDir = baseDirs;
		}
	}

	public String[] getBaseDirs() {
		return baseDir;
	}

	public Pattern getFilenamePattern() {
		return filenamePattern;
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
					LOGGER.info(String.format("Log file to parse '%s'", file.getAbsolutePath()));
					listOfFiles.add(file);
				}
			}
		}
		LOGGER.info(String.format("Extracted log files matching pattern '%s' from base dir(s) '%s'", filenamePattern.pattern(), Arrays.toString(baseDirs)));
		return listOfFiles.toArray(new File[0]);
	}
}
