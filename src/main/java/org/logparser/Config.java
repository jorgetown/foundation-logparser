package org.logparser;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.logparser.io.LogFiles;
import org.logparser.time.TimeInterval;

/**
 * Represents {@link ILogEntryFilter} and {@link ILogFilter} configuration.
 * 
 * @author jorge.decastro
 * 
 */
public class Config {
	public static final String DEFAULT_FILTER_PATTERN = ".*";
	public static final String DEFAULT_FILENAME_PATTERN = ".*.log$";
	public static final String DEFAULT_DECIMAL_FORMAT = "#####.#####";

	public enum GroupBy {
		DAY_OF_MONTH, DAY_OF_WEEK
	};

	private String friendlyName;
	private String sampleEntry;
	private String timestampPattern;
	private String timestampFormat;
	private String actionPattern;
	private String durationPattern;
	private String filterPattern;
	private String decimalFormat;
	private LogFiles logFiles;
	private TimeInterval timeInterval;
	private GroupBy groupBy;
	private SamplerConfig samplerConfig;

	public Config() {
		filterPattern = DEFAULT_FILTER_PATTERN;
		decimalFormat = DEFAULT_DECIMAL_FORMAT;
		groupBy = GroupBy.DAY_OF_MONTH;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(final String name) {
		this.friendlyName = name;
	}

	public String getSampleEntry() {
		return sampleEntry;
	}

	public void setSampleEntry(final String entry) {
		this.sampleEntry = entry;
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

	public TimeInterval getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(final TimeInterval timeInterval) {
		this.timeInterval = timeInterval;
	}

	public SamplerConfig getSampler() {
		return samplerConfig;
	}

	public void setSampler(final SamplerConfig samplerConfig) {
		this.samplerConfig = samplerConfig;
	}

	public GroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(final GroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public LogFiles getLogFiles() {
		return logFiles;
	}

	public void setLogFiles(final LogFiles logFiles) {
		this.logFiles = logFiles;
	}

	public String getDecimalFormat() {
		return decimalFormat;
	}

	public void setDecimalFormat(final String decimalFormat) {
		this.decimalFormat = decimalFormat;
	}

	public int groupByToCalendar() {
		switch (groupBy) {
		case DAY_OF_WEEK:
			return Calendar.DAY_OF_WEEK;
		default:
			return Calendar.DAY_OF_MONTH;
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
		if (logFiles == null) {
			throw new IllegalArgumentException("'logFiles' property is required. Check configuration file.");
		}
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	// TODO Replace tagged class w/ class hierarchy
	public static class SamplerConfig {
		public enum SampleBy {
			TIME, FREQUENCY
		};

		public SampleBy sampleBy;
		public int value;
		public TimeUnit timeUnit;

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}
	}
}
