package org.logparser;

import java.util.regex.Pattern;

import org.logparser.config.FilterParams;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Responsible for creating {@link LogEntryFilter}s.
 * 
 * @author jorge.decastro
 * 
 */
public class LogEntryFilterFactory {
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
		return filterBuilder.build();
	}
}
