package org.logparser;

/**
 * Protocol for a strongly-typed observer in the eponymous design pattern.
 * 
 * @author jorge.decastro
 * 
 */
public interface IObserver<E> {
	public void consume(E event);
}
