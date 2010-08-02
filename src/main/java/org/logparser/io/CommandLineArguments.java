package org.logparser.io;

import com.beust.jcommander.Parameter;

/**
 * Represents available command line arguments.
 * 
 * @see <a href="http://beust.com/jcommander/">JCommander</a>
 * @author jorge.decastro
 * 
 */
public class CommandLineArguments {
	@Parameter(names = { "-usage", "-help", "-h" }, description = "Help -a summary of all the options that log-parser understands", required = false)
	public boolean help;

	@Parameter(names = { "-config", "-configfile", "-c" }, description = "Optional path to JSON configuration file; defaults to 'config.json' in the current directory", required = false)
	public String configFile = "config.json";

	@Parameter(names = { "-logname", "-log", "-l" }, description = "Name of desired log group to analyze", required = true)
	public String logName;
}
