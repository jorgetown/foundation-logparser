package org.logparser;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.junit.Ignore;

/**
 * Represents a simple log entry used for testing.
 * 
 * @author jorge.decastro
 * 
 */
@Ignore
public class TestMessage implements ITimestampedEntry {
	private final long time;
	private final String action;

	public TestMessage(final long milliseconds) {
		this(null, milliseconds);
	}

	public TestMessage(final String action, final long milliseconds) {
		this.action = action;
		this.time = milliseconds;
	}

	public String getAction() {
		return action;
	}

	public double getDuration() {
		return time;
	}

	public long getTimestamp() {
		return time;
	}

	public String getText() {
		return "";
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public String toJsonString() {
		return "";
	}

	public String toCsvString() {
		return "";
	}
}