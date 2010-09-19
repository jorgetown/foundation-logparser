package org.logparser.config;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Map;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Configuration parameters for the Google chart generator.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class ChartParams {
	private static final Logger LOGGER = Logger.getLogger(ChartParams.class.getName());
	private static final String ENCODING_SCHEME = "UTF-8";
	private final String baseUri;
	private final Map<String, String> params;

	@JsonCreator
	public ChartParams(@JsonProperty("baseUri") final String baseUri, @JsonProperty("params") final Map<String, String> params) {
		if (Strings.isNullOrEmpty(baseUri)) {
			throw new IllegalArgumentException("'baseUri' argument is required.");
		}
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

	public Map<String, String> urlEncodeValues(final Map<String, String> toEncode) {
		return Maps.transformValues(toEncode, encodeString);
	}

	private final Function<String, String> encodeString = new Function<String, String>() {
		public String apply(final String toEncode) {
			try {
				return URLEncoder.encode(toEncode, ENCODING_SCHEME);
			} catch (UnsupportedEncodingException uee) {
				// don't need to handle because UTF-8 is a standard charset
				LOGGER.error(String.format("Error '%s' encoding '%s'", ENCODING_SCHEME, toEncode), uee);
			}
			return "";
		}
	};

	@Override
	public String toString() {
		return (new ReflectionToStringBuilder(this) {
			protected boolean accept(Field f) {
				return super.accept(f) && !f.getName().equals("encodeString");
			}
		}).toString();
	}
}
