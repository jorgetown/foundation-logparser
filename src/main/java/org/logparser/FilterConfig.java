package org.logparser;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.logparser.time.Instant;

/**
 * Represents {@link IMessageFilter} and {@link ILogFilter} configuration.
 * 
 * @author jorge.decastro
 * 
 */
public class FilterConfig {
	public static final String DEFAULT_FILTER_PATTERN = ".*";
	public static final String DEFAULT_FILENAME_PATTERN = ".*.log";

	public enum GroupBy {
		DAY_OF_MONTH, DAY_OF_WEEK, HOUR, MINUTE
	};

	private String friendlyName;
	private String sampleMessage;
	private String timestampPattern;
	private String timestampFormat;
	private String actionPattern;
	private String durationPattern;
	private String filterPattern;
	private String filenamePattern;
	private Instant before;
	private Instant after;
	private GroupBy groupBy;
	private String[] baseDirs;

	public FilterConfig() {
		filterPattern = DEFAULT_FILTER_PATTERN;
		filenamePattern = DEFAULT_FILENAME_PATTERN;
		groupBy = GroupBy.HOUR;
		before = null;
		after = null;
		baseDirs = new String[] { "." };
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(final String name) {
		this.friendlyName = name;
	}

	public String getSampleMessage() {
		return sampleMessage;
	}

	public void setSampleMessage(final String message) {
		this.sampleMessage = message;
	}

	public String getTimestampPattern() {
		return timestampPattern;
	}

	public void setTimestampPattern(final String timestampPattern) {
		this.timestampPattern = timestampPattern;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(final String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public String getActionPattern() {
		return actionPattern;
	}

	public void setActionPattern(final String actionPattern) {
		this.actionPattern = actionPattern;
	}

	public String getDurationPattern() {
		return durationPattern;
	}

	public void setDurationPattern(final String durationPattern) {
		this.durationPattern = durationPattern;
	}

	public String getFilterPattern() {
		return filterPattern;
	}

	public void setFilterPattern(final String filterPattern) {
		this.filterPattern = filterPattern;
	}

	public String getFilenamePattern() {
		return filenamePattern;
	}

	public void setFilenamePattern(final String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}

	public Instant getBefore() {
		return before;
	}

	public void setBefore(final String before) {
		if (StringUtils.isNotBlank(before)) {
			this.before = Instant.valueOf(before);
		}
	}

	public Instant getAfter() {
		return after;
	}

	public void setAfter(final String after) {
		if (StringUtils.isNotBlank(after)) {
			this.after = Instant.valueOf(after);
		}
	}

	public GroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(final GroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public String[] getBaseDirs() {
		return baseDirs;
	}

	public void setBaseDirs(final String[] baseDirs) {
		this.baseDirs = baseDirs;
	}

	public int groupByToCalendar() {
		switch (groupBy) {
		case DAY_OF_MONTH:
			return Calendar.DAY_OF_MONTH;
		case DAY_OF_WEEK:
			return Calendar.DAY_OF_WEEK;
		case MINUTE:
			return Calendar.MINUTE;
		default:
			return Calendar.HOUR_OF_DAY;
		}
	}

	public void validate() {
		if (StringUtils.isBlank(timestampPattern)) {
			throw new IllegalArgumentException("'timestampPattern' property is required. Check configuration file.");
		}
		if (StringUtils.isBlank(timestampFormat)) {
			throw new IllegalArgumentException("'timestampFormat' property is required. Check configuration file.");
		}
		if (StringUtils.isBlank(actionPattern)) {
			throw new IllegalArgumentException("'actionPattern' property is required. Check configuration file.");
		}
		if (StringUtils.isBlank(durationPattern)) {
			throw new IllegalArgumentException("'durationPattern' property is required. Check configuration file.");
		}
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
