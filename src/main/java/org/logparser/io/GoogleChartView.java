package org.logparser.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.logparser.LogEntry;
import org.logparser.stats.DayStats;
import org.logparser.stats.TimeStats;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Generates charts using the Google Charts API.
 * 
 * @see <a href="http://code.google.com/apis/chart/">Google Chart Tools / Image Charts (aka Chart API)</a>
 * @author jorge.decastro
 */
public class GoogleChartView {
	private static final Logger LOGGER = Logger.getLogger(GoogleChartView.class.getName());
	private final String baseUri;
	private final Map<String, String> params;

	public GoogleChartView(final String baseUri, final Map<String, String> params) {
		Preconditions.checkNotNull(baseUri, "'baseUri' argument cannot be null.");
		Preconditions.checkNotNull(params, "'params' argument cannot be null.");
		this.baseUri = baseUri;
		this.params = params;
	}

	Function<String, URL> makeUrl = new Function<String, URL>() {
		public URL apply(final String url) {
			try {
				return new URL(url);
			} catch (MalformedURLException mue) {
			}
			return null;
		}
	};

	Function<String, String> encodeString = new Function<String, String>() {
		public String apply(final String str) {
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
			}
			return null;
		}
	};

	public void write(final Map<String, String> urls) {
		write(Maps.transformValues(urls, makeUrl), "png");
	}

	public void write(final Map<String, URL> urls, final String format) {
		for (Entry<String, URL> url : urls.entrySet()) {
			try {
				BufferedImage image = ImageIO.read(url.getValue());
				File outfile = new File(String.format("%s.%s", CharMatcher.anyOf("<>:\"\\/|?*").removeFrom(url.getKey()), format));
				ImageIO.write(image, format, outfile);
				LOGGER.info(String.format("Writing image chart to %s", outfile));
			} catch (IOException ioe) {
				LOGGER.info("IO error writing image chart", ioe);
			}
		}
	}

	public Map<String, String> createChartUrls(final DayStats<LogEntry> dayStats) {
		Map<String, String> urls = new HashMap<String, String>();

		Map<String, String> encodedParams = Maps.transformValues(params, encodeString);

		for (Entry<String, TimeStats<LogEntry>> entries : dayStats.getDayStats().entrySet()) {

			StringBuilder url = new StringBuilder(baseUri);
			List<Double> means = new ArrayList<Double>();
			List<Integer> labels = new ArrayList<Integer>();
			url.append("chtt=");
			url.append(entries.getKey());
			for (Entry<String, String> entry : encodedParams.entrySet()) {
				url.append("&");
				url.append(entry.getKey());
				url.append("=");
				url.append(entry.getValue());
			}
			SummaryStatistics stats = new SummaryStatistics();
			for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
				means.add(timeStats.getValue().getMean());
				labels.add(timeStats.getKey());
				stats.addValue(timeStats.getValue().getMean());
			}

			long upperbound = Math.round(stats.getMax() + stats.getMin());
			url.append("&chxl=0:|").append(Joiner.on("|").join(labels)).append("|2:|min|avg|max");
			url.append(String.format("&chxp=2,%s,%s,%s", stats.getMin(), stats.getMean(), stats.getMax()));
			url.append(String.format("&chds=0,%s&chxr=1,0,%s|2,0,%s", upperbound, upperbound, upperbound));
			url.append("&chd=t:").append(Joiner.on(",").join(means));

			LOGGER.info(String.format("%s", url.toString()));
			urls.put(entries.getKey(), url.toString());
		}

		return urls;
	}
}
