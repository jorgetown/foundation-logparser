package org.logparser;

/**
 * Responsible for the creation of {@link IStatsView} instances.
 * 
 * @author jorge.decastro
 * 
 */
public interface IStatsViewFactory<E> {
	public IStatsView<E> newInstance();
}
