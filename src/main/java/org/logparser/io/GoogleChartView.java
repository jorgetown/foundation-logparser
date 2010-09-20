package org.logparser.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

	public void write(final Map<String, String> strings, final String format, final  String prefix) {
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
	
	// TODO Oi. How did you get so big and clumsy?
	public Map<String, String> createChartUrls(final DayStats<LogEntry> dayStats, final Map<String, TimeStats<LogEntry>> alerts) {
		Map<String, String> urls = new HashMap<String, String>();
		Map<String, String> paramsCopy = new HashMap<String, String>(params);
		String marker = paramsCopy.remove("chm");
		String statsMarker = marker;

		for (Entry<String, TimeStats<LogEntry>> entries : dayStats.getDayStats().entrySet()) {

			StringBuilder url = new StringBuilder(baseUri);
			List<Double> means = new ArrayList<Double>();
			List<String> labels = new ArrayList<String>();
			url.append("chtt=");
			url.append(entries.getKey());
			
			SummaryStatistics stats = new SummaryStatistics();
			int index = 0;
			for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
				means.add(Double.valueOf(df.format(timeStats.getValue().getMean())));
				try {
					labels.add(URLEncoder.encode(dayStats.getFormattedLabel(timeStats.getKey()), "UTF-8"));
				} catch (UnsupportedEncodingException uee) {
				}
				stats.addValue(Double.valueOf(df.format(timeStats.getValue().getMean())));
				
				if (alerts.containsKey(entries.getKey())) {
					TimeStats<LogEntry> alertStats = alerts.get(entries.getKey());
					if (alertStats.getTimeStats().containsKey(timeStats.getKey())) {
						statsMarker = String.format("%s|a,00E741,0,%s,18,-1", statsMarker, index);
					}
				}
				index++;
			}
			url.append("&chm=");
			try {
				url.append(URLEncoder.encode(statsMarker.equals(marker)?marker:statsMarker, "UTF-8"));
			} catch (UnsupportedEncodingException uee) {
			}
			statsMarker = marker;
			
			Map<String, String> encodedParams = chartParams.urlEncodeValues(paramsCopy);
			for (Entry<String, String> entry : encodedParams.entrySet()) {
				url.append("&");
				url.append(entry.getKey());
				url.append("=");
				url.append(entry.getValue());
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
