package org.logparser;

/**
 * Protocol for a strongly-typed, "push"-based, Observer design pattern.
 * 
 * @author jorge.decastro
 * 
 */
public interface IObservable<T> {
	public void attach(IObserver<T>... observers);

	public void detach(IObserver<T>... observers);

	public void notifyObservers(T event);
}
