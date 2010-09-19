package org.logparser.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link ChartParams}.
 * 
 * @author jorge.decastro
 * 
 */
public class ChartParamsTest {
	private static final String BASE_URI = "http://chart.apis.google.com/chart?";
	private ChartParams underTest;

	@Test(expected = IllegalArgumentException.class)
	public void testNullBaseUriArgument() {
		Map<String, String> params = new HashMap<String, String>();
		underTest = new ChartParams(null, params);
		assertThat(underTest, is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyBaseUriArgument() {
		Map<String, String> params = new HashMap<String, String>();
		underTest = new ChartParams("", params);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testRequiredBaseUriArgument() {
		Map<String, String> params = new HashMap<String, String>();
		underTest = new ChartParams(BASE_URI, params);
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getBaseUri(), is(equalTo(BASE_URI)));
	}

	@Test(expected = NullPointerException.class)
	public void testNullParamsArgument() {
		underTest = new ChartParams(BASE_URI, null);
		assertThat(underTest, is(nullValue()));
	}

	@Test
	public void testRequiredParamsArgument() {
		Map<String, String> params = new HashMap<String, String>();
		underTest = new ChartParams(BASE_URI, params);
		assertThat(underTest, is(notNullValue()));
		assertThat(underTest.getParams(), is(equalTo(params)));
	}

	@Test
	public void testUrlEncodedParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", "select A, sum(B)");
		underTest = new ChartParams(BASE_URI, params);
		Map<String, String> encoded = underTest.urlEncodeValues(params);
		assertThat(encoded, is(notNullValue()));
		assertThat(encoded.isEmpty(), is(false));
		assertThat(encoded.size(), is(1));
		assertThat(encoded.values(), hasItem("select+A%2C+sum%28B%29"));
		assertThat(underTest.getParams(), is(not(equalTo(encoded))));
		assertThat(underTest.getParams(), is(not(sameInstance(encoded))));
	}
}
