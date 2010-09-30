package org.logparser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Responsible for parsing log entry strings and returning corresponding
 * {@link LogEntry}s.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class LogEntryFilter implements ILogEntryFilter<LogEntry> {
	public static final String DEFAULT_FILTER_PATTERN = ".*";
	private final Pattern timestampPattern;
	private final String timestampFormat;
	private final Pattern actionPattern;
	private final Pattern durationPattern;
	private final Pattern filterPattern;
	private final ITimeInterval timeInterval;
	private final ITimeInterval dateInterval;
	private final String sampleEntry;
	/**
	 * The date format to expect from the log entries to be filtered.
	 */
	private final ThreadLocal<DateFormat> dateFormatter;

	private LogEntryFilter(final Builder builder) {
		timestampPattern = builder.timestampPattern;
		timestampFormat = builder.timestampFormat;
		dateFormatter = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(timestampFormat);
			}
		};
		actionPattern = builder.actionPattern;
		durationPattern = builder.durationPattern;
		filterPattern = builder.filterPattern;
		timeInterval = builder.timeInterval;
		dateInterval = builder.dateInterval;
		sampleEntry = builder.sampleEntry;
	}

	public LogEntry parse(final String text) {
		Matcher m = timestampPattern.matcher(text);
		if (m.find()) {
			Date date = getDateFromString.apply(m.group(1));
			if (timeInterval.isBetweenInstants(date) && dateInterval.isBetweenInstants(date)) {
				m = actionPattern.matcher(text);
				if (m.find()) {
					String action = m.group(1);
					if (filterPattern.matcher(action).matches()) {
						m = durationPattern.matcher(text);
						if (m.find()) {
							String duration = m.group(1);
							return new LogEntry(date.getTime(), action, Double.valueOf(duration));
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns the {@link Date} corresponding to the given {@link String},
	 * formatted by this {@link LogEntryFilter#getTimestampFormat()}.
	 */
	public Function<String, Date> getDateFromString = new Function<String, Date>() {
		public Date apply(final String date) {
			try {
				return dateFormatter.get().parse(date);
			} catch (ParseException pe) {
				// If the date format is wrong, fail quickly
				throw new IllegalArgumentException(
						String.format("Check timestamp regex '%s' or timestamp format '%s'; unable to parse '%s'", getTimestampPattern().pattern(), getTimestampFormat(), date), pe);
			}
		}
	};

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

	public ITimeInterval getDateInterval() {
		return dateInterval;
	}
	
	public String getSampleEntry() {
		return sampleEntry;
	}

	public static class Builder {
		// required parameters
		private final Pattern timestampPattern;
		private final String timestampFormat;
		private final Pattern actionPattern;
		private final Pattern durationPattern;
		// optional parameters
		private Pattern filterPattern = Pattern.compile(DEFAULT_FILTER_PATTERN);
		private ITimeInterval timeInterval = new InfiniteTimeInterval();
		private ITimeInterval dateInterval = new InfiniteTimeInterval();
		private String sampleEntry = "";

		public Builder(final Pattern timestampPattern, final String timestampFormat, final Pattern actionPattern, final Pattern durationPattern) {
			this.timestampPattern = Preconditions.checkNotNull(timestampPattern, "'timestampPattern' argument cannot be null.");
			this.timestampFormat = Preconditions.checkNotNull(timestampFormat, "'timestampFormat' argument cannot be null.");
			this.actionPattern = Preconditions.checkNotNull(actionPattern, "'actionPattern' argument cannot be null.");
			this.durationPattern = Preconditions.checkNotNull(durationPattern, "'durationPattern' argument cannot be null.");
		}

		public Builder filterPattern(final Pattern filterPattern) {
			this.filterPattern = Preconditions.checkNotNull(filterPattern, "'filterPattern' argument cannot be null.");
			return this;
		}

		public Builder timeInterval(final ITimeInterval timeInterval) {
			this.timeInterval = Preconditions.checkNotNull(timeInterval, "'timeInterval' argument cannot be null.");
			return this;
		}

		public Builder dateInterval(final ITimeInterval dateInterval) {
			this.dateInterval = Preconditions.checkNotNull(dateInterval, "'dateInterval' argument cannot be null.");
			return this;
		}
		
		public Builder sampleEntry(final String sampleEntry) {
			this.sampleEntry = Strings.nullToEmpty(sampleEntry);
			return this;
		}

		public LogEntryFilter build() {
			return new LogEntryFilter(this);
		}
	}
}
