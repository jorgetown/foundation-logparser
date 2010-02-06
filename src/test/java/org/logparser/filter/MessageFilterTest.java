package org.logparser.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.logparser.Message;
import org.logparser.time.ITimeInterval;
import org.logparser.time.InfiniteTimeInterval;

/**
 * Unit tests for the {@link MessageFilter}.
 * 
 * @author jorge.decastro
 */
public class MessageFilterTest {
	private MessageFilter entryFilter;
	private ITimeInterval timeInterval = new InfiniteTimeInterval();
	private static final String MESSAGE_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss";
	private static final DateFormat MESSAGE_DATE_FORMATTER = new SimpleDateFormat(MESSAGE_DATE_FORMAT);

	private static final String[] LOG_MESSAGES = {
			"10.118.101.132 - - [07/Jan/2010:10:03:46 +0000] \"GET /cms/scripts/N201039839/bundles/editor.js HTTP/1.1\" 200 557279 51",
			"10.118.101.132 - - [07/Jan/2010:10:06:07 +0000] \"GET /cms/streamImage/b0f15942-75f8-11de-84c7-00144feabdc0.img HTTP/1.1\" 200 7120 289",
			"10.117.101.80 - - [15/Dec/2009:00:00:15 +0000] \"GET /cms/methode-event/lock.do?loid=26.0.1108263263&event=unlock&eventId=37087422 HTTP/1.1\" 200 - 14",
			"10.118.101.132 - - [15/Dec/2009:17:00:00 +0000] \"POST /cms/statusCheck.do HTTP/1.1\" 200 1779 2073" };

	@Test
	public void testMessagesFilteredSuccessfully() {
		entryFilter = new MessageFilter(timeInterval);
		for (String s : LOG_MESSAGES) {
			Message entry = entryFilter.parse(s);
			assertNotNull(entry);
			assertEquals(s, entry.getMessage());
		}
	}

	@Test
	public void testMalformedText() {
		entryFilter = new MessageFilter(timeInterval);
		Message entry = entryFilter.parse("not enough data items");
		assertNull(entry);
	}

	@Test
	public void testMalformedDateTime() {
		entryFilter = new MessageFilter(timeInterval);
		Message entry = entryFilter.parse("unexpectedly formatted datetime Hours:Minutes:Seconds-today/month/year 5 6 7 8 9 10 11");
		assertNull(entry);
	}

	@Test
	public void testParseSuccess() {
		entryFilter = new MessageFilter(timeInterval);
		final String EXPECTED_DATE_TIME = "15/Dec/2009:01:22:33";
		final String ACCESS_DATE_TIME = String.format("[%s]", EXPECTED_DATE_TIME);

		final String EXPECTED_URL = "servlet";
		final String ACCESS_URL = String.format("/context/path/%s?a=1&b=2", EXPECTED_URL);

		final String EXPECTED_TIME_TAKEN = "99";

		final String ACCESS_ENTRY = String.format(
				"1.1.1.1 - - %s \"POST %s HTTP/1.1\" 200 1779 %s",
				ACCESS_DATE_TIME, ACCESS_URL, EXPECTED_TIME_TAKEN);

		Message Message = entryFilter.parse(ACCESS_ENTRY);

		assertEquals(ACCESS_ENTRY, Message.getMessage());
		assertEquals(EXPECTED_DATE_TIME, MESSAGE_DATE_FORMATTER.format(Message.getDate()));
		assertEquals(EXPECTED_URL, Message.getUrl());
		assertEquals(EXPECTED_TIME_TAKEN, Message.getMilliseconds());
	}
}