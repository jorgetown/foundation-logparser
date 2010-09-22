package org.logparser.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
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
import org.logparser.config.ChartParams;
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
	private final ChartParams chartParams;
	private final DecimalFormat df;

	public GoogleChartView(final ChartParams chartParams) {
		Preconditions.checkNotNull(chartParams, "'chartParams' argument cannot be null.");
		this.chartParams = chartParams;
		this.baseUri = chartParams.getBaseUri();
		this.params = chartParams.getParams();
		df = new DecimalFormat("#.#");
	}

	private final Function<String, URL> makeUrl = new Function<String, URL>() {
		public URL apply(final String url) {
			try {
				return new URL(url);
			} catch (MalformedURLException mue) {
			}
			return null;
		}
	};

	public void write(final Map<String, String> urls) {
		write(urls, "png", "");
	}

	public void write(final Map<String, String> strings, final String format, final String prefix) {
		Map<String, URL> urls = Maps.transformValues(strings, makeUrl);
		for (Entry<String, URL> url : urls.entrySet()) {
			try {
				BufferedImage image = ImageIO.read(url.getValue());
				File outfile = new File(String.format("%s%s.%s", prefix, CharMatcher.anyOf("<>:\"\\/|?*").removeFrom(url.getKey()), format));
				ImageIO.write(image, format, outfile);
				LOGGER.info(String.format("Writing image chart to %s", outfile));
			} catch (IOException ioe) {
				LOGGER.info("IO error writing image chart", ioe);
			}
		}
	}

	private Map<String, String> setupChartParams(final TimeStats<LogEntry> timeStats, final TimeStats<LogEntry> alerts, final Function<Integer, String> functor, final String markerOverride) {
		
		Map<String, String> paramsCopy = new HashMap<String, String>(params);
		SummaryStatistics stats = new SummaryStatistics();
		List<String> means = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		paramsCopy.put("chm", markerOverride);
		int index = 0;
		for (Entry<Integer, StatisticalSummary> entries : timeStats.getTimeStats().entrySet()) {
			means.add(df.format(entries.getValue().getMean()));
			labels.add(functor.apply(entries.getKey()));
			stats.addValue(Double.valueOf(df.format(entries.getValue().getMean())));

			if (alerts != null && alerts.getTimeStats().containsKey(entries.getKey())) {
				String marker = paramsCopy.get("chm");
				paramsCopy.put("chm", String.format("%s|a,00E741,0,%s,18,-1", marker, index));
			}
			index++;
		}
		long upperbound = Math.round(stats.getMax() + stats.getMin());
		paramsCopy.put("chxl", String.format("0:|%s|2:|min|avg|max", Joiner.on("|").join(labels)));
		paramsCopy.put("chxp", String.format("2,%s,%s,%s", stats.getMin(), stats.getMean(), stats.getMax()));
		paramsCopy.put("chds", String.format("0,%s", upperbound));
		paramsCopy.put("chxr", String.format("1,0,%s|2,0,%s", upperbound, upperbound));
		paramsCopy.put("chd", String.format("t:%s", Joiner.on(",").join(means)));

		return paramsCopy;
	}

	public Map<String, String> createChartUrl(final String title, final TimeStats<LogEntry> timeStats, final Function<Integer, String> functor) {
		Map<String, String> params = setupChartParams(timeStats, null, functor, "D,FF0000,0,-1,1|N,FF0000,0,-1,9");
		Map<String, String> urls = new HashMap<String, String>();
		urls.put(title, makeUrlString(title, params));
		return urls;
	}

	public Map<String, String> createChartUrls(final DayStats<LogEntry> dayStats, final Map<String, TimeStats<LogEntry>> alerts, final Function<Integer, String> functor) {
		return createChartUrls(dayStats, alerts, functor, params.get("chm"));
	}
	
	public Map<String, String> createChartUrls(final DayStats<LogEntry> dayStats, final Function<Integer, String> functor) {
		return createChartUrls(dayStats, null, functor, "D,FF0000,0,-1,1|N,FF0000,0,-1,9");
	}

	private Map<String, String> createChartUrls(
			final DayStats<LogEntry> dayStats,
			final Map<String, TimeStats<LogEntry>> alerts,
			final Function<Integer, String> functor,
			final String markerOverride) {
		
		Map<String, String> urls = new HashMap<String, String>();

		String key = null;
		for (Entry<String, TimeStats<LogEntry>> entries : dayStats.getDayStats().entrySet()) {
			key = entries.getKey();
			Map<String, String> chartParams = setupChartParams(entries.getValue(), alerts != null ? alerts.get(key) : null, functor, markerOverride);
			urls.put(key, makeUrlString(key, chartParams));
		}

		return urls;
	}

	private String makeUrlString(final String title, final Map<String, String> keyValues) {
		StringBuilder url = new StringBuilder(baseUri);
		url.append("chtt=");
		url.append(chartParams.encodeString.apply(title));
		Map<String, String> encodedParams = chartParams.urlEncodeValues(keyValues);
		for (Entry<String, String> entry : encodedParams.entrySet()) {
			url.append("&");
			url.append(entry.getKey());
			url.append("=");
			url.append(entry.getValue());
		}
		LOGGER.info(String.format("%s", url.toString()));
		return url.toString();
	}
}
