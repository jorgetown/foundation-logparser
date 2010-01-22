package org.logparser.io;

import org.logparser.Message;
import org.logparser.filter.IMessageFilter;

/**
 * Custom parser for log {@link Message}s.
 * 
 * @author jorge.decastro
 * 
 */
public class DefaultLogParser extends AbstractLogParser<Message> {

	public DefaultLogParser(final IMessageFilter<Message> messageFilter) {
		super(messageFilter);
	}
}
