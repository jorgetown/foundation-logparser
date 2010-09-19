package org.logparser.config;

import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.logparser.ILogEntryFilter;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;
import org.logparser.time.TimeInterval;

import com.google.common.base.Strings;

/**
 * Provides ready to consume configuration settings to {@link ILogEntryFilter}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class FilterParams {
	public static final String DEFAULT_FILTER_PATTERN = ".*";
	private final String sampleEntry;
	private final Pattern timestampPattern;
	private final String timestampFormat;
	private final Pattern actionPattern;
	private final Pattern durationPattern;
	private final Pattern filterPattern;
	private final ITimeInterval timeInterval;

	// Ugh. Wanted a builder here but that it play nice with Jackson's JSON mapping;
	// Tolerable because it's handled by Jackson.
	@JsonCreator
	public FilterParams(@JsonProperty("sampleEntry") final String sampleEntry,
			@JsonProperty("timestampPattern") final String timestampPattern,
			@JsonProperty("timestampFormat") final String timestampFormat,
			@JsonProperty("actionPattern") final String actionPattern,
			@JsonProperty("durationPattern") final String durationPattern,
			@JsonProperty("filterPattern") final String filterPattern,
			@JsonProperty("timeInterval") @JsonDeserialize(as=TimeInterval.class) final ITimeInterval timeInterval) {

		if (Strings.isNullOrEmpty(timestampPattern)) {
			throw new IllegalArgumentException("'timestampPattern' property is required. Check configuration file.");
		}
		if (Strings.isNullOrEmpty(timestampFormat)) {
			throw new IllegalArgumentException("'timestampFormat' property is required. Check configuration file.");
		}
		if (Strings.isNullOrEmpty(actionPattern)) {
			throw new IllegalArgumentException("'actionPattern' property is required. Check configuration file.");
		}
		if (Strings.isNullOrEmpty(durationPattern)) {
			throw new IllegalArgumentException("'durationPattern' property is required. Check configuration file.");
		}
		this.timestampPattern = Pattern.compile(timestampPattern);
		this.actionPattern = Pattern.compile(actionPattern);
		this.durationPattern = Pattern.compile(durationPattern);
		this.filterPattern = Strings.isNullOrEmpty(filterPattern) ? Pattern.compile(DEFAULT_FILTER_PATTERN) : Pattern.compile(filterPattern);
		this.timestampFormat = timestampFormat;
		this.timeInterval = timeInterval != null ? timeInterval : new InfiniteTimeInterval();
		this.sampleEntry = Strings.nullToEmpty(sampleEntry);
	}

	public String getSampleEntry() {
		return sampleEntry;
	}

	public Pattern getTimestampPattern() {
		return timestampPattern;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public Pattern getActionPattern() {
		return actionPattern;
	}

	public Pattern getDurationPattern() {
		return durationPattern;
	}

	public Pattern getFilterPattern() {
		return filterPattern;
	}

	public ITimeInterval getTimeInterval() {
		return timeInterval;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
