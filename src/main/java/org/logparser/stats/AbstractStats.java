package org.logparser.stats;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.logparser.IObserver;

import com.google.common.base.Preconditions;

/**
 * Skeletal implementation and common functionality for statistic summaries.
 * 
 * @author jorge.decastro
 * 
 */
public abstract class AbstractStats<E> implements Serializable, IObserver<E> {
	private static final Logger LOGGER = Logger.getLogger(AbstractStats.class.getName());
	private static final long serialVersionUID = -5699879056725405682L;

	protected static final String DEFAULT_REPORT_DATE_FORMAT = "MM/dd";
	protected static final String DEFAULT_DECIMAL_FORMAT = "#.##";
	protected static final String DATE_FORMAT = "yyyyMMdd";
	protected ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DATE_FORMAT);
		}
	};
	
	protected void consumeAll(final List<E> logEntries) {
		Preconditions.checkNotNull(logEntries);
		for (E entry : logEntries) {
			consume(entry);
		}
	}
	
	protected String formatDate(final DateFormat to, final String date){
		return formatDate(dateFormatter.get(), to, date);
	}
	
	private String formatDate(final DateFormat from, final DateFormat to, final String date) {
		try {
			return to.format(from.parse(date));
		} catch (ParseException pe) {
			LOGGER.error(String.format("Error parsing date '%s' from format '%s' to format '%s'.", date, from, to), pe);
		}
		return "";
	}

	public abstract void consume(final E entry);
}
