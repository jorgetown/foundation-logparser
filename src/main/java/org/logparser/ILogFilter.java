package org.logparser;

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
	 * @param filepath the {@code filepath} to the log file.
	 */
	public void filter(String filepath);

	/**
	 * The total number of log entries read from the file.
	 * 
	 * @return {@code int} containing the total number of log entries read.
	 */
	public int size();
}
