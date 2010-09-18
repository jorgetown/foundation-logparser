package org.logparser;

import java.util.Map;

import net.jcip.annotations.Immutable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

/**
 * Configuration parameters for the Google chart generator.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class ChartParams {
	private final String baseUri;
	private final Map<String, String> params;

	@JsonCreator
	public ChartParams(@JsonProperty("baseUri") final String baseUri, @JsonProperty("params") final Map<String, String> params) {
		Preconditions.checkNotNull(baseUri, "'baseUri' argument cannot be null.");
		Preconditions.checkNotNull(params, "'params' argument cannot be null.");
		this.baseUri = baseUri;
		this.params = params;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
