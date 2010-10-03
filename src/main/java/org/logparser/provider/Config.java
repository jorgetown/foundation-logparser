package org.logparser.provider;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents configuration mapped from the JSON file.
 * 
 * @author jorge.decastro
 */
public final class Config {
	private String friendlyName;
	private final FilterProvider filterProvider;
	private StatsProvider statsProvider;
	private ChartParams chartParams;
	private SamplerProvider samplerProvider;
	private final LogFilesProvider logFilesProvider;

	@JsonCreator
	public Config(@JsonProperty("filterProvider") final FilterProvider filterProvider, @JsonProperty("logFilesProvider") final LogFilesProvider logFilesProvider) {
		if (filterProvider == null) {
			throw new IllegalArgumentException("'filterProvider' property is required. Check configuration file.");
		}
		if (logFilesProvider == null) {
			throw new IllegalArgumentException("'logFilesProvider' property is required. Check configuration file.");
		}
		this.filterProvider = filterProvider;
		this.logFilesProvider = logFilesProvider;
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

	public StatsProvider getStatsProvider() {
		return statsProvider;
	}

	public void setStatsProvider(final StatsProvider statsProvider) {
		this.statsProvider = statsProvider;
	}

	public SamplerProvider getSamplerProvider() {
		return samplerProvider;
	}

	public void setSamplerProvider(final SamplerProvider samplerProvider) {
		this.samplerProvider = samplerProvider;
	}

	public ChartParams getChartParams() {
		return chartParams;
	}

	public void setChartParams(final ChartParams chartParams) {
		this.chartParams = chartParams;
	}

	public LogFilesProvider getLogFilesProvider() {
		return logFilesProvider;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
