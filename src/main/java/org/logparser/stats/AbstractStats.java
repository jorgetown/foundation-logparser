package org.logparser.stats;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Skeletal implementation and common functionality for statistic summaries.
 * 
 * @author jorge.decastro
 * 
 */
public abstract class AbstractStats<E> implements Serializable {
	protected static final String DATE_FORMAT = "yyyyMMdd";
	protected ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};
	
	private static final long serialVersionUID = -5699879056725405682L;

	protected void addAll(final List<E> logEntries) {
		Preconditions.checkNotNull(logEntries);
		for (E entry : logEntries) {
			add(entry);
		}
	}

	protected abstract void add(final E entry);
}
