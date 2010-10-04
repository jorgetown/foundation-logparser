package org.logparser;

import java.io.File;

/**
 * Specifies the protocol required of log filter implementations.
 * 
 * @author jorge.decastro
 * 
 */
public interface ILogFilter {

	/**
	 * Read log entries from the given file.
	 * 
	 * @param file the log {@code file}.
	 */
	public void filter(File file);

	/**
	 * The total number of log entries read from the file.
	 * 
	 * @return {@code int} containing the total number of log entries read.
	 */
	public int size();
}
