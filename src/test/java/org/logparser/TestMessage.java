package org.logparser;

import org.junit.Ignore;

/**
 * Represents a simple log message used for testing.
 * 
 * @author jorge.decastro
 * 
 */
@Ignore
public class TestMessage implements ITimestampedEntry {
	private final long time;

	public TestMessage(final long milliseconds) {
		this.time = milliseconds;
	}

	public String getAction() {
		return null;
	}

	public double getDuration() {
		return time;
	}

	public long getTimestamp() {
		return time;
	}
}