package org.logparser;

/**
 * Specifies the protocol required of log filter implementations.
 * 
 * @author jorge.decastro
 * 
 */
public interface ILogFilter {

	public void filter(String filepath);

	public int size();
}
