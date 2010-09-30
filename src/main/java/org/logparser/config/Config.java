package org.logparser.config;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.logparser.ILogFilter;

/**
 * Represents {@link ILogFilter} configuration obtained from the JSON file.
 * Contains configuration parameters for the participants.
 * 
 * @author jorge.decastro 
 * TODO split it, as it became the 'The Bloated One'
 */
public class Config {
	public static final String DEFAULT_DECIMAL_FORMAT = "#.#####";

	private String friendlyName;
	private FilterProvider filterProvider;
	private StatsParams statsParams;
	private ChartParams chartParams;
	private SamplerConfig samplerConfig;
	private String decimalFormat;
	private LogFilesProvider logFilesProvider;
	private boolean filteredEntriesStored;

	public Config() {
		decimalFormat = DEFAULT_DECIMAL_FORMAT;
		filteredEntriesStored = true;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(final String name) {
		this.friendlyName = name;
	}

	public FilterProvider getFilterProvider() {
		return filterProvider;
	}

	public void setFilterProvider(final FilterProvider filterProvider) {
		this.filterProvider = filterProvider;
	}

	public StatsParams getStatsParams() {
		return statsParams;
	}

	public void setStatsParams(final StatsParams statsParams) {
		this.statsParams = statsParams;
	}

	public SamplerConfig getSampler() {
		return samplerConfig;
	}

	public void setSampler(final SamplerConfig samplerConfig) {
		this.samplerConfig = samplerConfig;
	}

	public LogFilesProvider getLogFilesProvider() {
		return logFilesProvider;
	}

	public void setLogFilesProvider(final LogFilesProvider logFiles) {
		this.logFilesProvider = logFiles;
	}

	public String getDecimalFormat() {
		return decimalFormat;
	}

	public void setDecimalFormat(final String decimalFormat) {
		this.decimalFormat = decimalFormat;
	}

	public boolean isFilteredEntriesStored() {
		return filteredEntriesStored;
	}

	public void setFilteredEntriesStored(final boolean filteredEntriesStored) {
		this.filteredEntriesStored = filteredEntriesStored;
	}

	public ChartParams getChartParams() {
		return chartParams;
	}

	public void setChartParams(final ChartParams chartParams) {
		this.chartParams = chartParams;
	}

	public void validate() {
		if (filterProvider == null) {
			throw new IllegalArgumentException("'filterProvider' property is required. Check configuration file.");
		}
		if (logFilesProvider == null) {
			throw new IllegalArgumentException("'logFilesProvider' property is required. Check configuration file.");
		}
		// TODO move sampler onto its own provider class
		if (samplerConfig != null && samplerConfig.value < 0) {
			throw new IllegalArgumentException("'value' property of sampler must be a positive integer. Check configuration file.");
		}
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	// TODO clean-up & make consistent
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
