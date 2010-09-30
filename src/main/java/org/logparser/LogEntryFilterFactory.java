package org.logparser;

import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.apache.log4j.Logger;
import org.logparser.config.FilterParams;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Responsible for creating {@link LogEntryFilter}s.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class LogEntryFilterFactory {
	private static final Logger LOGGER = Logger.getLogger(LogEntryFilterFactory.class);
	private final FilterParams filterParams;

	public LogEntryFilterFactory(final FilterParams filterParams) {
		this.filterParams = Preconditions.checkNotNull(filterParams, "'filterParams' argument cannot be null.");
	}

	public LogEntryFilter build() {
		LogEntryFilter.Builder filterBuilder = new LogEntryFilter.Builder(
				Pattern.compile(filterParams.getTimestampPattern()),
				filterParams.getTimestampFormat(), 
				Pattern.compile(filterParams.getActionPattern()), 
				Pattern.compile(filterParams.getDurationPattern()));

		if (!Strings.isNullOrEmpty(filterParams.getFilterPattern())) {
			filterBuilder.filterPattern(Pattern.compile(filterParams.getFilterPattern()));
		}
		if (filterParams.getTimeInterval() != null) {
			filterBuilder.timeInterval(filterParams.getTimeInterval());
		}
		if (filterParams.getDateInterval() != null) {
			filterBuilder.dateInterval(filterParams.getDateInterval());
		}
		if (!Strings.isNullOrEmpty(filterParams.getSampleEntry())) {
			filterBuilder.sampleEntry(filterParams.getSampleEntry());
		}

		LogEntryFilter logEntryFilter = filterBuilder.build();
		String sampleLogEntry = logEntryFilter.getSampleEntry();
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
