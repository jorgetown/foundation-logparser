package org.logparser.filter;

import net.jcip.annotations.Immutable;

import org.logparser.Message;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampling service.
 * 
 * In this particular case, extracts messages at a rate given by the 'frequency'
 * argument.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class MessageSamplingByFrequency implements IMessageFilter<Message> {
	private final IMessageFilter<Message> filter;
	private final int frequency;
	private int count;

	public MessageSamplingByFrequency(final IMessageFilter<Message> filter, final int frequency) {
		this.filter = filter;
		this.frequency = frequency;
		this.count = 0;
	}

	public Message parse(final String text) {
		if (count >= frequency) {
			count = 0;
			return filter.parse(text);
		}
		count++;
		return null;
	}
	
	public IMessageFilter<Message> getFilter(){
		return filter;
	}
	
	public int getFrequency(){
		return frequency;
	}
}
