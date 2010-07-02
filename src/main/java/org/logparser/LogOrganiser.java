package org.logparser;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for grouping log {@code E}ntries together.
 * 
 * @author jorge.decastro
 */
public class LogOrganiser<E extends ITimestampedEntry> {
	private final Map<String, IStatsView<E>> organisedByKey;

	public LogOrganiser() {
		this.organisedByKey = new HashMap<String, IStatsView<E>>();
	}

	public Map<String, IStatsView<E>> groupBy(final LogSnapshot<E> logSnapshot) {
		Preconditions.checkNotNull(logSnapshot);
		organisedByKey.clear();

		String key = null;
		for (E entry : logSnapshot.getFilteredEntries()) {
			key = entry.getAction();
			// new request? create a new stats wrapper for it
			if (!organisedByKey.containsKey(key)) {
				IStatsView<E> stats = new StatsSnapshot<E>();
				stats.add(entry);
				organisedByKey.put(key, stats);
			} else {
				IStatsView<E> existingEntriesList = organisedByKey.get(key);
				existingEntriesList.add(entry);
			}
		}

		return organisedByKey;
	}
}
