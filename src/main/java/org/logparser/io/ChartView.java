package org.logparser.io;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.logparser.ITimestampedEntry;
import org.logparser.LogSnapshot;

import com.google.common.base.Preconditions;

/**
 * Creates a chart image from a collection of log entries.
 * 
 * TODO System.setProperty("java.awt.headless","true");
 * 
 * TODO customize chart type etc
 * 
 * @author jorge.decastro
 */
public class ChartView<E extends ITimestampedEntry> {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final int DEFAULT_PIXELS_X = 1024;
	public static final int DEFAULT_PIXELS_Y = 768;
	private final LogSnapshot<E> logSnapshot;
	private final Calendar cal;
	protected final int x;
	protected final int y;
	protected String yAxisLegend;
	protected String xAxisLegend;

	public ChartView(final LogSnapshot<E> logSnapshot) {
		this(logSnapshot, DEFAULT_PIXELS_X, DEFAULT_PIXELS_Y);
	}

	public ChartView(final LogSnapshot<E> logSnapshot, final int x, final int y) {
		Preconditions.checkNotNull(logSnapshot);
		this.logSnapshot = logSnapshot;
		this.x = x;
		this.y = y;
		this.xAxisLegend = String.format("%s entries", logSnapshot.getTotalEntries());
		this.yAxisLegend = "";
		this.cal = Calendar.getInstance();
	}

	public void write(final String path, final String filename) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(filename);

		CategoryDataset dataset = populateDataset(logSnapshot);

		JFreeChart jFreeChart = ChartFactory.createBarChart(
				filename,
				getXAxisLegend(), 
				getYAxisLegend(), 
				dataset,
				PlotOrientation.VERTICAL, 
				true, 
				false, 
				false);

		String filepath = String.format("%s%s%s.png", path, FILE_SEPARATOR, filename);

		try {
			ChartUtilities.saveChartAsPNG(new File(filepath), jFreeChart, x, y);
		} catch (IOException ioe) {
			throw new RuntimeException(String.format("Failed to save chart: %s", filepath), ioe);
		}
	}

	public CategoryDataset populateDataset(final LogSnapshot<E> logSnapshot) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		List<E> entries = logSnapshot.getFilteredEntries();

		for (E entry : entries) {
			cal.setTimeInMillis(entry.getTimestamp());
			dataset.addValue(entry.getDuration(), entry.getAction(), cal.getTime());
		}
		return dataset;
	}

	public String getXAxisLegend() {
		int size = logSnapshot.getFilteredEntries().size();
		if (size > 0) {
			cal.setTimeInMillis(logSnapshot.getFilteredEntries().get(0).getTimestamp());
			Date fromDate = cal.getTime();
			cal.setTimeInMillis(logSnapshot.getFilteredEntries().get(size - 1).getTimestamp());
			Date toDate = cal.getTime();
			return String.format("%s entries, %s to %s", logSnapshot.getTotalEntries(), fromDate, toDate);
		}
		return xAxisLegend;
	}

	public String getYAxisLegend() {
		return this.yAxisLegend;
	}
}