package org.logparser.example;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.logparser.IStatsView;
import org.logparser.LogSnapshot;
import org.logparser.io.ChartView;

/**
 * Produces chart images specifically for log {@link Message}s.
 * 
 * @author jorge.decastro
 *
 */
public class MessageChartView extends ChartView<Message> {

	public MessageChartView(final LogSnapshot<Message> logSnapshot, final Map<String, IStatsView<Message>> keyStats) {
		super(logSnapshot, keyStats);
	}

	@Override
	public CategoryDataset populateDataset(final Map<String, IStatsView<Message>> keyStats) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Set<Entry<String, IStatsView<Message>>> statsEntries = keyStats.entrySet();

		for (Entry<String, IStatsView<Message>> entries : statsEntries) {
			for (Message message : entries.getValue().getEntries()) {
				dataset.addValue(Integer.valueOf(message.getMilliseconds()), entries.getKey(), message.getDate());
			}
		}
		return dataset;
	}

	@Override
	public String getXAxisLegend() {
		int size = logSnapshot.getFilteredEntries().size();
		return String.format("%s entries, %s to %s", 
				logSnapshot.getTotalEntries(), 
				logSnapshot.getFilteredEntries().get(0).getDate(), 
				logSnapshot.getFilteredEntries().get(size - 1).getDate());
	}

	@Override
	public String getYAxisLegend() {
		return "Milliseconds";
	}
}
