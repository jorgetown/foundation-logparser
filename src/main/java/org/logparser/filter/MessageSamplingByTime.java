package org.logparser.filter;

import java.util.Calendar;

import net.jcip.annotations.Immutable;

import org.logparser.Message;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampling service.
 * 
 * In this particular case, extracts {@link Message}s each time the interval
 * between any 2 {@link Message}s is longer than the value given by the 'timeInMillis'
 * argument.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public class MessageSamplingByTime implements IMessageFilter<Message> {
	private final IMessageFilter<Message> filter;
	private final long timeInMillis;
	private Calendar previous;

	public MessageSamplingByTime(final IMessageFilter<Message> filter, final long timeInMillis) {
		this.filter = filter;
		this.timeInMillis = timeInMillis;
		this.previous = Calendar.getInstance();
	}

	public Message parse(final String text) {
		Message m = filter.parse(text);
		if (m == null) {
			return m;
		}
		Message sampled = null;
		if (previous.getTime() == null
				|| (m.getDate().getTime() - previous.getTimeInMillis() > timeInMillis)) {
			sampled = m;
		}
		previous.setTime(m.getDate());
		return sampled;
	}
	
	public IMessageFilter<Message> getFilter(){
		return filter;
	}
	
	public long getTimeInMillis(){
		return timeInMillis;
	}
}
