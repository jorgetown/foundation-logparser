package org.logparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.NotThreadSafe;

import org.logparser.time.Instant;

/**
 * Represents the arguments needed to run the log parser.
 * 
 * @author jorge.decastro
 */
@NotThreadSafe
public final class AnalyzeArguments {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private static final String HELP_TEXT = "<path_filename> [optional regex pattern] [optional after HH:mm] [optional before HH:mm] \nExample: \n\tEXAMPLE_log_2009-12-15.log save.do|edit.do\n 17:25 18:10"
			+ "Processed log files are created in this directory.";
	/**
	 * The time format that the before/after {@link Instant} arguments should use.
	 */
	public static final Pattern TIME_FORMAT = Pattern.compile("(\\d{1,2})\\:((\\d{1,2}))");

	private String pathFile;
	private String path;
	private String file;
	// match everything by default
	private Pattern pattern = Pattern.compile(".*");
	private Instant after;
	private Instant before;

	public String getPathFile() {
		return pathFile;
	}

	public String getPath() {
		return path;
	}

	public String getFile() {
		return file;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public Instant getAfter() {
		return after;
	}

	public Instant getBefore() {
		return before;
	}

	public AnalyzeArguments() {
	}

	/**
	 * Gets all the necessary input arguments needed.
	 * 
	 * @param args the provided {@link String} arguments.
	 * 
	 */
	public AnalyzeArguments(final String[] args) {
		validate(args);
		parse(args);
	}

	/**
	 * Validates the command line arguments given.
	 * 
	 * @param args the given {@link String} array of arguments.
	 */
	public void validate(final String[] args) {
		// expect 1 mandatory (filepath) and 3 optional arguments
		if (args == null || args.length < 1 || args.length > 4) {
			throw new IllegalArgumentException(String.format("Error in # of arguments, should be\n%s", HELP_TEXT));
		}
		// force both time arguments to be present, if one of them is
		if (args.length > 2 && args.length < 4) {
			throw new IllegalArgumentException(String.format("Error in # of arguments, should be\n%s", HELP_TEXT));
		}
	}

	/**
	 * Parse the arguments given and set class members.
	 * 
	 * @param args the given {@link String} array of arguments.
	 */
	public void parse(final String[] args) {
		pathFile = args[0];
		path = resolvePath(pathFile);
		file = resolveFile(pathFile);

		if (args.length > 1) {
			pattern = Pattern.compile(args[1]);
		}

		if (args.length > 2) {
			after = extractTime(args[2]);
			before = extractTime(args[3]);
		}
	}

	/**
	 * The processed logs are written to the same directory as the source log
	 * file, this extracts that path. If the source log file was provided as
	 * "C:\temp\access.log", then just "C:\temp\" will be returned. If just a
	 * filename was provided e.g. "access.log", then an empty string will be
	 * returned.
	 * 
	 * @param pathFilename the provided path and filename of the log to analyze.
	 * @return just the path element, empty String if no directory specified.
	 */
	private String resolvePath(final String pathFilename) {
		int lastPathSeparatorIndex = pathFilename.lastIndexOf(FILE_SEPARATOR);
		return pathFilename.substring(0, lastPathSeparatorIndex + 1);
	}

	private String resolveFile(final String pathFilename) {
		int lastPathSeparatorIndex = pathFilename.lastIndexOf(FILE_SEPARATOR);
		return pathFilename.substring(lastPathSeparatorIndex + 1);
	}

	private Instant extractTime(final String timeString) {
		Matcher m = TIME_FORMAT.matcher(timeString);
		if (m.find()) {
			return new Instant(m.group(1), m.group(2));
		}
		throw new IllegalArgumentException(String.format("Unable to parse the time for String :%s\n"
						+ "Expected format is: %s", timeString, TIME_FORMAT.pattern()));
	}
}
