package org.logparser.example;

import org.logparser.IStatsViewFactory;

/**
 * Factory implementation for {@link MessageStats} instances.
 * 
 * @author jorge.decastro
 * 
 */
public class MessageStatsFactory implements IStatsViewFactory<Message> {

	public MessageStats newInstance() {
		return new MessageStats();
	}

}
