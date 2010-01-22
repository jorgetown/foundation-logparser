package org.logparser.time;

import java.util.Date;

/**
 * Represents a definite length of time marked off by two instants in time.
 * 
 * @author jorge.decastro
 * 
 */
public interface ITimeInterval {
	/**
	 * Answers whether a given {@link Date} lies between two time instants.
	 * 
	 * @param date the {@link Date} being compared.
	 * @return true if {@link Date} lies after earliest time instant and before
	 *         latest time instant; false otherwise.
	 */
	public boolean isBetweenInstants(Date date);
}