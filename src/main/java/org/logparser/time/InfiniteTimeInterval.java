package org.logparser.time;

import java.util.Date;

import net.jcip.annotations.Immutable;

/**
 * Represents a time window of infinite length.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class InfiniteTimeInterval implements ITimeInterval {

	public boolean isBetweenInstants(Date date) {
		return true;
	}
}
