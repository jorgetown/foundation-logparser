package org.logparser;

import org.junit.Ignore;

/**
 * Represents a simple log message used for testing.
 * 
 * @author jorge.decastro
 * 
 */
@Ignore
public class TestMessage implements IStatsCapable {
	private final long milliseconds;

	public TestMessage(final long milliseconds) {
		this.milliseconds = milliseconds;
	}

	public long getElapsedTime() {
		return milliseconds;
	}
}