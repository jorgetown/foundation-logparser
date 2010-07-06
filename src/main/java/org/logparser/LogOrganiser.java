package org.logparser;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * Responsible for grouping log {@link ITimestampedEntry}s together, given a {@link LogSnapshot}.
 * 
 * @author jorge.decastro
 */
public class LogOrganiser<E extends ITimestampedEntry> {
	private final Map<String, IStatsView<E>> organisedByAction;

	public LogOrganiser() {
		this.organisedByAction = new HashMap<String, IStatsView<E>>();
	}

	public Map<String, IStatsView<E>> organize(final LogSnapshot<E> logSnapshot) {
		Preconditions.checkNotNull(logSnapshot);
		organisedByAction.clear();

		String key = null;
		for (E entry : logSnapshot.getFilteredEntries()) {
			key = entry.getAction();
			// new request? create a new stats wrapper for it
			if (!organisedByAction.containsKey(key)) {
				IStatsView<E> stats = new StatsSnapshot<E>();
				stats.add(entry);
				organisedByAction.put(key, stats);
			} else {
				IStatsView<E> existingEntriesList = organisedByAction.get(key);
				existingEntriesList.add(entry);
			}
		}

		return organisedByAction;
	}
}
