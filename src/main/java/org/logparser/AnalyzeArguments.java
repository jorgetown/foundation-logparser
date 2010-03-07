package org.logparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.NotThreadSafe;

import org.logparser.time.Instant;

/**
 * Extracts the command line arguments needed to run the log parser.
 * 
 * @author jorge.decastro
 */
@NotThreadSafe
public final class AnalyzeArguments {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private static final String HELP_TEXT = "<comma-separated path to logfiles> <log file regex pattern> <optional filter regex pattern> <optional after HH:mm> <optional before HH:mm> \nExample: \n\t/home/logs/ EXAMPLE_log(.*).log save.do|edit.do\n 17:25 18:10"
			+ "Processed log files are created in these directories.";
	/**
	 * The time format that the before/after {@link Instant} arguments should use.
	 */
	public static final Pattern TIME_FORMAT = Pattern.compile("(\\d{1,2})\\:((\\d{1,2}))");

	private String[] paths;
	private File[] files;
	// match everything by default
	private Pattern pattern = Pattern.compile(".*");
	private Instant after;
	private Instant before;

	public String[] getPaths() {
		return paths;
	}

	public File[] getFiles() {
		return files;
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
		// expect 2 mandatory -paths & files, and 3 optional arguments
		if (args == null || args.length < 2 || args.length > 5) {
			throw new IllegalArgumentException(String.format("Error in # of arguments, should be\n%s", HELP_TEXT));
		}
		// force both time arguments to be present, if one of them is
		if (args.length > 3 && args.length < 5) {
			throw new IllegalArgumentException(String.format("Error in # of arguments, should be\n%s", HELP_TEXT));
		}
	}

	/**
	 * Parse the arguments given and set class members.
	 * 
	 * @param args the given {@link String} array of arguments.
	 */
	public void parse(final String[] args) {
		paths = args[0].split(",");
		Pattern filePattern = Pattern.compile(args[1]);
		List<File> listOfFiles = new ArrayList<File>();
		for (String path : paths){
			File f = new File(path);
			if (!f.exists()){
				throw new IllegalArgumentException(String.format("Unable to find given path %s", path));
			}
			File[] contents = f.listFiles();
			for (File file : contents){
				if (filePattern.matcher(file.getName()).matches()){
					listOfFiles.add(file);
				}
			}
		}
		
		files = listOfFiles.toArray(new File[0]);

		if (args.length > 2) {
			pattern = Pattern.compile(args[2]);
		}

		if (args.length > 3) {
			after = extractTime(args[3]);
			before = extractTime(args[4]);
		}
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
