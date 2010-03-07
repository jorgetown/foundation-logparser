package org.logparser.io;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.logparser.IStatsView;
import org.logparser.LogSnapshot;
import org.logparser.Preconditions;

/**
 * Creates a chart image from a collection of log entries.
 * 
 * TODO System.setProperty("java.awt.headless","true");
 * 
 * TODO customize chart type etc
 * 
 * @author jorge.decastro
 */
public class ChartView<E> {
	public static final int DEFAULT_PIXELS_X = 1024;
	public static final int DEFAULT_PIXELS_Y = 768;
	protected final LogSnapshot<E> logSnapshot;
	protected final Map<String, IStatsView<E>> keyStats;
	protected final int x;
	protected final int y;
	protected String yAxisLegend;
	protected String xAxisLegend;

	public ChartView(final LogSnapshot<E> logSnapshot, final Map<String, IStatsView<E>> keyStats) {
		this(logSnapshot, keyStats, DEFAULT_PIXELS_X, DEFAULT_PIXELS_Y);
	}
	
	public ChartView(final LogSnapshot<E> logSnapshot, final Map<String, IStatsView<E>> keyStats, final int x, final int y) {
		Preconditions.checkNotNull(logSnapshot);
		Preconditions.checkNotNull(keyStats);
		this.logSnapshot = logSnapshot;
		this.keyStats = keyStats;
		this.x = x;
		this.y = y;
		this.xAxisLegend = String.format("%s entries", logSnapshot.getTotalEntries());
		this.yAxisLegend = "";
	}

	public void write(final String path, final String filename) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(filename);
		
		CategoryDataset dataset = populateDataset(keyStats);

		JFreeChart jFreeChart = ChartFactory.createBarChart(
				filename,
				getXAxisLegend(), 
				getYAxisLegend(), 
				dataset,
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);

		jFreeChart.getPlot().setForegroundAlpha(0.5f);
		jFreeChart.getPlot().setBackgroundAlpha(0.0f);

		String filepath = String.format("%s%s.png", path, filename);

		try {
			ChartUtilities.saveChartAsPNG(new File(filepath), jFreeChart, x, y);
		} catch (IOException ioe) {
			throw new RuntimeException(String.format("Failed to save chart: %s", filepath), ioe);
		}
	}

	public CategoryDataset populateDataset(Map<String, IStatsView<E>> keyStats) {
		return new DefaultCategoryDataset();
	}

	public String getXAxisLegend() {
		return this.xAxisLegend;
	}

	public String getYAxisLegend() {
		return this.yAxisLegend;
	}
}