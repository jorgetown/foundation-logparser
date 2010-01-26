package org.logparser.filter;

import java.util.Calendar;

import org.logparser.Message;

/**
 * A {@link IMessageFilter} implementation that maintains state, acting as a
 * sampling service.
 * 
 * In this particular case, extracts {@link Message}s each time the interval
 * between any 2 {@link Message}s is longer than the value given by the 'diff'
 * argument.
 * 
 * @author jorge.decastro
 * 
 */
public class MessageSamplingByTime implements IMessageFilter<Message> {
	private final IMessageFilter<Message> filter;
	private final long diff;
	private Calendar previous;

	public MessageSamplingByTime(final IMessageFilter<Message> filter,
			final long timeInMillis) {
		this.filter = filter;
		this.diff = timeInMillis;
		this.previous = Calendar.getInstance();
	}

	public Message parse(final String text) {
		Message m = filter.parse(text);
		if (m == null){
			return m;
		}
		Message sampled = null;
		if (previous.getTime() == null
				|| (m.getDate().getTime() - previous.getTimeInMillis() > diff)) {
			sampled = m;
		}
		previous.setTime(m.getDate());
		return sampled;
	}
}
