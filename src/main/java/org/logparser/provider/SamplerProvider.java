package org.logparser.provider;

import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.logparser.ILogEntryFilter;
import org.logparser.ITimestampedEntry;
import org.logparser.sampling.SamplingByFrequency;
import org.logparser.sampling.SamplingByTime;

/**
 * Responsible for providing bespoke instances of {@link ILogEntryFilter}
 * implementations.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class SamplerProvider {
	private static final Logger LOGGER = Logger.getLogger(SamplerProvider.class);

	public enum SampleBy {
		TIME, FREQUENCY
	};

	public final SampleBy sampleBy;
	public final int value;
	public final TimeUnit timeUnit;

	@JsonCreator
	public SamplerProvider(
			@JsonProperty("sampleBy") final SampleBy sampleBy,
			@JsonProperty("value") final int value,
			@JsonProperty("timeUnit") final TimeUnit timeUnit) {

		if (sampleBy == null) {
			throw new IllegalArgumentException("'sampleBy' property of sampler provider is required.");
		}
		if (value < 0) {
			throw new IllegalArgumentException("'value' property of sampler provider must be a positive integer.");
		}
		this.sampleBy = sampleBy;
		this.value = value;
		this.timeUnit = timeUnit;
	}

	public SampleBy getSampleBy() {
		return sampleBy;
	}

	public int getValue() {
		return value;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public <E extends ITimestampedEntry> ILogEntryFilter<E> build(final ILogEntryFilter<E> filter) {
		ILogEntryFilter<E> sampler = null;
		switch (sampleBy) {
			case TIME:
				sampler = new SamplingByTime<E>(filter, value, timeUnit != null ? timeUnit : TimeUnit.MINUTES);
				break;
			case FREQUENCY:
				sampler = new SamplingByFrequency<E>(filter, value);
				break;
			default:
				sampler = filter;
		}
		return sampler;
	}
}
