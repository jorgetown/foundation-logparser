package org.logparser.config;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.logparser.LogEntryFilter;
import org.logparser.LogEntryFilterFactory;
import org.logparser.time.DateInterval;
import org.logparser.time.ITimeInterval;
import org.logparser.time.TimeInterval;

import com.google.common.base.Strings;

/**
 * Responsible for collecting the configuration parameters used by the
 * {@link LogEntryFilterFactory} to create {@link LogEntryFilter}s.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class FilterParams {
	private final String sampleEntry;
	private final String timestampPattern;
	private final String timestampFormat;
	private final String actionPattern;
	private final String durationPattern;
	private final String filterPattern;
	private final ITimeInterval timeInterval;
	private final ITimeInterval dateInterval;

	// Ugh. Wanted a builder here but that it play nice with Jackson's JSON auto-mapping; tolerable because it's handled by Jackson.
	@JsonCreator
	public FilterParams(
			@JsonProperty("sampleEntry") final String sampleEntry,
			@JsonProperty("timestampPattern") final String timestampPattern,
			@JsonProperty("timestampFormat") final String timestampFormat,
			@JsonProperty("actionPattern") final String actionPattern,
			@JsonProperty("durationPattern") final String durationPattern,
			@JsonProperty("filterPattern") final String filterPattern,
			@JsonProperty("timeInterval") @JsonDeserialize(as = TimeInterval.class) final ITimeInterval timeInterval,
			@JsonProperty("dateInterval") @JsonDeserialize(as = DateInterval.class) final ITimeInterval dateInterval) {

		if (Strings.isNullOrEmpty(timestampPattern)) {
			throw new IllegalArgumentException("'timestampPattern' property is required.");
		}
		if (Strings.isNullOrEmpty(timestampFormat)) {
			throw new IllegalArgumentException("'timestampFormat' property is required.");
		}
		if (Strings.isNullOrEmpty(actionPattern)) {
			throw new IllegalArgumentException("'actionPattern' property is required.");
		}
		if (Strings.isNullOrEmpty(durationPattern)) {
			throw new IllegalArgumentException("'durationPattern' property is required.");
		}
		this.timestampPattern = timestampPattern;
		this.actionPattern = actionPattern;
		this.durationPattern = durationPattern;
		this.filterPattern = filterPattern;
		this.timestampFormat = timestampFormat;
		this.timeInterval = timeInterval;
		this.dateInterval = dateInterval;
		this.sampleEntry = Strings.nullToEmpty(sampleEntry);
	}

	public String getSampleEntry() {
		return sampleEntry;
	}

	public String getTimestampPattern() {
		return timestampPattern;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public String getActionPattern() {
		return actionPattern;
	}

	public String getDurationPattern() {
		return durationPattern;
	}

	public String getFilterPattern() {
		return filterPattern;
	}

	public ITimeInterval getTimeInterval() {
		return timeInterval;
	}

	public ITimeInterval getDateInterval() {
		return dateInterval;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
