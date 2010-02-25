package org.logparser;


/**
 * Factory implementation for {@link StatsSnapshot} instances.
 * 
 * @author jorge.decastro
 * @param <E>
 * 
 */
public class StatsViewFactory<E extends IStatsCapable> implements IStatsViewFactory<E> {

	public StatsSnapshot<E> newInstance() {
		return new StatsSnapshot<E>();
	}
}
