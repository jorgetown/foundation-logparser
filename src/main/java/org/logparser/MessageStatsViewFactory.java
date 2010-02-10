package org.logparser;

/**
 * Factory implementation for {@link MessageStatsView} instances.
 * 
 * @author jorge.decastro
 * 
 */
public class MessageStatsViewFactory implements IStatsViewFactory<Message> {

	public MessageStatsView newInstance() {
		return new MessageStatsView();
	}

}
