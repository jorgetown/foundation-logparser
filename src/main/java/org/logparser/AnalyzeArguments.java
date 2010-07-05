package org.logparser;

import java.io.File;
import java.io.IOException;

import net.jcip.annotations.NotThreadSafe;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Extracts the command line arguments needed to run the log parser.
 * 
 * @author jorge.decastro
 */
@NotThreadSafe
public final class AnalyzeArguments {
	public static final String DEFAULT_CONFIG_FILE = "config.json";
	private static final String HELP_TEXT = "<optional path to JSON configuration file>";
	private String pathToConfig;

	public String getPathToConfig() {
		return pathToConfig;
	}

	public AnalyzeArguments() {
		this(new String[] {});
	}

	/**
	 * Gets all the necessary input arguments needed.
	 * 
	 * @param args the provided {@link String} arguments.
	 * 
	 */
	public AnalyzeArguments(final String[] args) {
		pathToConfig = DEFAULT_CONFIG_FILE;
		validate(args);
		parse(args);
	}

	/**
	 * Validates the command line arguments given.
	 * 
	 * @param args the given {@link String} array of arguments.
	 */
	public void validate(final String[] args) {
		// expect 1 optional argument
		if (args.length > 1) {
			throw new IllegalArgumentException(String.format("Error in # of arguments, should be\n%s", HELP_TEXT));
		}
	}

	/**
	 * Parse the arguments given and set class members.
	 * 
	 * @param args the given {@link String} array of arguments.
	 */
	public void parse(final String[] args) {
		if (args != null && args.length > 0) {
			pathToConfig = args[0];
			File f = new File(pathToConfig.trim());
			if (!f.exists()) {
				throw new IllegalArgumentException(String.format("Unable to find config file %s", pathToConfig));
			}
		}
	}
	
	public FilterConfig getFilterConfig() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			FilterConfig filterConfig = mapper.readValue(new File(pathToConfig), FilterConfig.class);
			filterConfig.validate();
			return filterConfig;
		} catch (JsonParseException jpe) {
			jpe.printStackTrace();
		} catch (JsonMappingException jme) {
			jme.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
}
