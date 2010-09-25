package org.logparser.io;

import static org.logparser.Constants.DEFAULT_OUTPUT_DIR;

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

import com.google.common.base.Strings;

/**
 * Represents one or more log files on the file system, to be collected for
 * processing.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class LogFiles {
	private static final Logger LOGGER = Logger.getLogger(LogFiles.class.getName());
	
	private final Pattern filenamePattern;
	private final String[] inputDirs;
	private final String outputDir;

	public LogFiles(final String filenamePattern, final String[] inputDirs) {
		this(filenamePattern, inputDirs, DEFAULT_OUTPUT_DIR);
	}

	@JsonCreator
	public LogFiles(@JsonProperty("filenamePattern") final String filenamePattern, @JsonProperty("inputDirs") final String[] inputDirs, @JsonProperty("outputDir") final String outputDir) {
		if (StringUtils.isBlank(filenamePattern)) {
			throw new IllegalArgumentException("'filenamePattern' argument is required.");
		}
		this.filenamePattern = Pattern.compile(filenamePattern);
		this.inputDirs = ArrayUtils.isEmpty(inputDirs) ? new String[] { DEFAULT_OUTPUT_DIR } : inputDirs;
		this.outputDir = Strings.isNullOrEmpty(outputDir) ? DEFAULT_OUTPUT_DIR : outputDir;
		if (!this.outputDir.equals(DEFAULT_OUTPUT_DIR)) {
			File f = new File(outputDir);
			boolean createdDirSuccessfully = false;
			if (!(f.exists() && f.isDirectory())) {
				createdDirSuccessfully = f.mkdirs();
				if (!createdDirSuccessfully) {
					throw new IllegalArgumentException(String.format("Unable to make 'outputDir' dir at '%s'.", f.getAbsolutePath()));
				}
			}
		}
	}

	public String[] getInputDirs() {
		return inputDirs;
	}

	public Pattern getFilenamePattern() {
		return filenamePattern;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public File[] list() {
		return list(inputDirs, filenamePattern);
	}

	private File[] list(final String[] inputDirs, final Pattern filenamePattern) {
		List<File> listOfFiles = new ArrayList<File>();
		for (String path : inputDirs) {
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
		LOGGER.info(String.format("Extracted log files matching pattern '%s' from input dir(s) '%s'", filenamePattern.pattern(), Arrays.toString(inputDirs)));
		return listOfFiles.toArray(new File[0]);
	}
}