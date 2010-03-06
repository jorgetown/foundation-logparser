package org.logparser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for grouping messages of the same type together, according to
 * given criteria.
 * 
 * @author jorge.decastro
 */
public class LogOrganiser<E extends IStatsCapable> {
	private final Map<String, IStatsView<E>> organisedByKey;

	public LogOrganiser() {
		this.organisedByKey = new HashMap<String, IStatsView<E>>();
	}

	public Map<String, IStatsView<E>> groupBy(final LogSnapshot<E> logSnapshot, final String groupByKey) {
		Preconditions.checkNotNull(logSnapshot);
		Preconditions.checkNotNull(groupByKey);
		organisedByKey.clear();

		String key = null;
		for (E entry : logSnapshot.getFilteredEntries()) {
			try {
				/**
				 * hmm, not pretty I know. But it's a choice of either
				 * reflective access to a private field or passing the method
				 * *name* as the key e.g.:
				 * 
				 * <pre>
				 * Class aClass = entry.getClass();
				 * Method m = aClass.getMethod(&quot;getUrl&quot;);
				 * key = (String) m.invoke(entry);
				 * </pre>
				 */
				Field field = entry.getClass().getDeclaredField(groupByKey);
				field.setAccessible(true);
				key = (String) field.get(entry);
			} catch (Throwable t) {
				throw new IllegalArgumentException("Fatal error accessing log entry field by reflection", t);
			}

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
