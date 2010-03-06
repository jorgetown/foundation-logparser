package org.logparser;

/**
 * Represents a simple log message used for testing.
 * 
 * @author jorge.decastro
 * 
 */
public class TestMessage implements IStatsCapable {
	private final long milliseconds;

	public TestMessage(final long milliseconds) {
		this.milliseconds = milliseconds;
	}

	public long getElapsedTime() {
		return milliseconds;
	}
}