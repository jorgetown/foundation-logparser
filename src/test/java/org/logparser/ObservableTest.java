package org.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link Observable} implementation.
 * 
 * @author jorge.decastro
 * 
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ObservableTest {
	private Observable<LogEntry> underTest;
	private LogEntry event;
	@Mock
	IObserver<LogEntry> subscriber1;
	@Mock
	IObserver<LogEntry> subscriber2;

	@Before
	public void setup() {
		event = new LogEntry(new Date().getTime(), "edit.do", 1100.0D);
		underTest = new Observable<LogEntry>();
	}

	@After
	public void tearDown() {
		subscriber1 = null;
		subscriber2 = null;
		event = null;
		underTest = null;
	}

	@Test
	public void testSubjectAtInitializationHasNoSubscribers() {
		assertThat(underTest.getSubscribers(), is(notNullValue()));
		assertThat(underTest.getSubscribers().isEmpty(), is(true));
		assertThat(underTest.getSubscribers().size(), is(0));
	}

	@Test(expected = NullPointerException.class)
	public void testAttachNullSubscriberToSubject() {
		IObserver<LogEntry> observer = null;
		underTest.attach(observer);
		assertThat(underTest.getSubscribers(), is(notNullValue()));
		assertThat(underTest.getSubscribers().isEmpty(), is(true));
		assertThat(underTest.getSubscribers().size(), is(0));
	}

	@Test
	public void testAttachSingleSubscriberToSubject() {
		underTest.attach(subscriber1);
		assertThat(underTest.getSubscribers(), is(notNullValue()));
		assertThat(underTest.getSubscribers().size(), is(1));
		assertThat(underTest.getSubscribers(), hasItem(subscriber1));
	}

	@Test
	public void testAttachMultipleSubscribersToSubject() {
		underTest.attach(subscriber1, subscriber2);
		assertThat(underTest.getSubscribers().size(), is(2));
		assertThat(underTest.getSubscribers(), hasItem(subscriber1));
		assertThat(underTest.getSubscribers(), hasItem(subscriber2));
	}

	@Test(expected = NullPointerException.class)
	public void testAttachListOfSubscribersContainingNullSubscriber() {
		underTest.attach(subscriber1, null);
	}

	@Test
	public void testDetachSingleSubscriberRemovesItFromSubject() {
		underTest.attach(subscriber1);
		assertThat(underTest.getSubscribers().size(), is(1));
		assertThat(underTest.getSubscribers(), hasItem(subscriber1));
		underTest.detach(subscriber1);
		assertThat(underTest.getSubscribers().isEmpty(), is(true));
		assertThat(underTest.getSubscribers().size(), is(0));
	}

	@Test
	public void testDetachMultipleSubscribersRemovesThemFromSubject() {
		underTest.attach(subscriber1, subscriber2);
		assertThat(underTest.getSubscribers().size(), is(2));
		assertThat(underTest.getSubscribers(), hasItem(subscriber1));
		assertThat(underTest.getSubscribers(), hasItem(subscriber2));
		underTest.detach(subscriber1);
		assertThat(underTest.getSubscribers().size(), is(1));
		assertThat(underTest.getSubscribers(), hasItem(subscriber2));
	}

	@Test(expected = NullPointerException.class)
	public void testDetachListOfSubscribersContainingNullSubscriber() {
		underTest.attach(subscriber1, subscriber2);
		assertThat(underTest.getSubscribers().size(), is(2));
		assertThat(underTest.getSubscribers(), hasItem(subscriber1));
		assertThat(underTest.getSubscribers(), hasItem(subscriber2));
		underTest.detach(subscriber1, null);
	}

	@Test
	public void testNotifyObserversPropagatesNullEvent() {
		underTest.attach(subscriber1, subscriber2);
		underTest.notifyObservers(null);
		verify(subscriber1, times(1)).consume(null);
		verify(subscriber2, times(1)).consume(null);
	}

	@Test
	public void testNotifyObserversPropagatesNotNullEvent() {
		underTest.attach(subscriber1, subscriber2);
		underTest.notifyObservers(event);
		verify(subscriber1, times(1)).consume(event);
		verify(subscriber2, times(1)).consume(event);
	}
}
