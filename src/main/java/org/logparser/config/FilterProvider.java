package org.logparser.config;

import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.logparser.LogEntry;
import org.logparser.LogEntryFilter;
import org.logparser.time.DateInterval;
import org.logparser.time.ITimeInterval;
import org.logparser.time.TimeInterval;

import com.google.common.base.Strings;

/**
 * Responsible for providing bespoke instances of {@link LogEntryFilter}s.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class FilterProvider {
	private static final Logger LOGGER = Logger.getLogger(FilterProvider.class);
	private final String sampleEntry;
	private final String timestampPattern;
	private final String timestampFormat;
	private final String actionPattern;
	private final String durationPattern;
	private final String filterPattern;
	private final ITimeInterval timeInterval;
	private final ITimeInterval dateInterval;

	// Ugh. Builder pattern doesn't work with Jackson's JSON auto-mapping; tolerable because it's handled by Jackson.
	@JsonCreator
	public FilterProvider(
			@JsonProperty("sampleEntry") final String sampleEntry,
			@JsonProperty("timestampPattern") final String timestampPattern,
			@JsonProperty("timestampFormat") final String timestampFormat,
			@JsonProperty("actionPattern") final String actionPattern,
			@JsonProperty("durationPattern") final String durationPattern,
			@JsonProperty("filterPattern") final String filterPattern,
			@JsonProperty("timeInterval") @JsonDeserialize(as = TimeInterval.class) final ITimeInterval timeInterval,
			@JsonProperty("dateInterval") @JsonDeserialize(as = DateInterval.class) final ITimeInterval dateInterval) {

		if (Strings.isNullOrEmpty(timestampPattern)) {
			throw new IllegalArgumentException("'timestampPattern' property of filter provider is required.");
		}
		if (Strings.isNullOrEmpty(timestampFormat)) {
			throw new IllegalArgumentException("'timestampFormat' property of filter provider is required.");
		}
		if (Strings.isNullOrEmpty(actionPattern)) {
			throw new IllegalArgumentException("'actionPattern' property of filter provider is required.");
		}
		if (Strings.isNullOrEmpty(durationPattern)) {
			throw new IllegalArgumentException("'durationPattern' property of filter provider is required.");
		}
		this.timestampPattern = timestampPattern;
		this.actionPattern = actionPattern;
		this.durationPattern = durationPattern;
		this.filterPattern = filterPattern;
		this.timestampFormat = timestampFormat;
		this.timeInterval = timeInterval;
		this.dateInterval = dateInterval;
		this.sampleEntry = sampleEntry;
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

	public LogEntryFilter build() {
		LogEntryFilter.Builder filterBuilder = new LogEntryFilter.Builder(
				Pattern.compile(timestampPattern),
				timestampFormat,
				Pattern.compile(actionPattern),
				Pattern.compile(durationPattern));

		if (!Strings.isNullOrEmpty(sampleEntry)) {
			filterBuilder.sampleEntry(sampleEntry);
		}
		if (!Strings.isNullOrEmpty(filterPattern)) {
			filterBuilder.filterPattern(Pattern.compile(filterPattern));
		}
		if (timeInterval != null) {
			filterBuilder.timeInterval(timeInterval);
		}
		if (dateInterval != null) {
			filterBuilder.dateInterval(dateInterval);
		}

		LogEntryFilter logEntryFilter = filterBuilder.build();
		String sampleLogEntry = logEntryFilter.getSampleEntry();
		// sanity check: if a sample entry is given, it should be able to parse it
		if (!Strings.isNullOrEmpty(sampleLogEntry)) {
			LogEntry logEntry = logEntryFilter.parse(sampleLogEntry);
			if (logEntry != null) {
				LOGGER.info(String.format("Filter built and successfully parsed given sample entry: %s", logEntry.toString()));
			} else {
				LOGGER.warn("Filter built but unable to parse given sample entry!");
			}
		}
		return logEntryFilter;
	}
}
