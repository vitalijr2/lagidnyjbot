package io.github.vitalijr2.lagidnyj.chatkeeper;

import static io.github.vitalijr2.lagidnyj.chatkeeper.SimpleChatKeeper.DEFAULT_KEEPING_DELAY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class SimpleChatKeeperFastTest {

  @Mock
  private Cache<String, AtomicInteger> cache;
  @Mock
  private BlockingQueue<DelayedChatNotification> queue;
  @Mock
  private ScheduledExecutorService executorService;
  @Captor
  private ArgumentCaptor<Runnable> runnableCaptor;

  @InjectMocks
  private SimpleChatKeeper simpleChatKeeper;

  @BeforeEach
  void setUp() {
    clearInvocations(executorService);
  }

  @DisplayName("Create and initialize instance")
  @Test
  void initialize() {
    // when
    var chatKeeper = new SimpleChatKeeper(cache, queue, executorService);

    // then
    verify(executorService).scheduleWithFixedDelay(runnableCaptor.capture(), eq(DEFAULT_KEEPING_DELAY),
        eq(DEFAULT_KEEPING_DELAY), isA(TimeUnit.class));

    assertEquals(chatKeeper, runnableCaptor.getValue());
  }

  @DisplayName("Auto closable")
  @Test
  void close() throws InterruptedException {
    // given
    when(executorService.awaitTermination(anyLong(), isA(TimeUnit.class))).thenReturn(true);

    // when
    assertDoesNotThrow(() -> {
      try (SimpleChatKeeper chatKeeper = new SimpleChatKeeper(cache, queue, executorService)) {
        // do nothing
      }
    });

    // then
    verify(executorService).scheduleWithFixedDelay(isA(Runnable.class), anyLong(), anyLong(), isA(TimeUnit.class));
    verify(executorService).shutdown();
    verify(executorService).awaitTermination(anyLong(), isA(TimeUnit.class));
    verify(executorService, never()).shutdownNow();
    verifyNoMoreInteractions(executorService);
  }

  @DisplayName("Awaiting is failed")
  @Test
  void awaitingIsFailed() throws InterruptedException {
    // given
    when(executorService.awaitTermination(anyLong(), isA(TimeUnit.class))).thenReturn(false);

    // when
    assertDoesNotThrow(() -> {
      try (SimpleChatKeeper chatKeeper = new SimpleChatKeeper(cache, queue, executorService)) {
        // do nothing
      }
    });

    // then
    verify(executorService).scheduleWithFixedDelay(isA(Runnable.class), anyLong(), anyLong(), isA(TimeUnit.class));
    verify(executorService).shutdown();
    verify(executorService).awaitTermination(anyLong(), isA(TimeUnit.class));
    verify(executorService).shutdownNow();
    verifyNoMoreInteractions(executorService);
  }

  @DisplayName("Awaiting throws an exception")
  @Test
  void awaitingThrowsException() throws InterruptedException {
    // given
    when(executorService.awaitTermination(anyLong(), isA(TimeUnit.class))).thenThrow(
        new InterruptedException("test exception"));

    // when
    var exception = assertThrows(InterruptedException.class, () -> {
      try (SimpleChatKeeper chatKeeper = new SimpleChatKeeper(cache, queue, executorService)) {
        // do nothing
      }
    });

    // then
    verify(executorService).scheduleWithFixedDelay(isA(Runnable.class), anyLong(), anyLong(), isA(TimeUnit.class));
    verify(executorService).shutdown();
    verify(executorService).awaitTermination(anyLong(), isA(TimeUnit.class));
    verify(executorService, never()).shutdownNow();
    verifyNoMoreInteractions(executorService);

    assertEquals("test exception", exception.getMessage());
  }

  @DisplayName("A notification goes to a delayed queue")
  @Test
  void addNotification() {
    // given
    var notification = new DelayedChatNotification(123, 987, "John", "Smith", "johnsmith", "en");

    when(cache.get(anyString())).thenReturn(null);

    // when
    simpleChatKeeper.addUserToWatchList(notification);

    // then
    verify(cache).get("123:987");
    verify(cache, never()).put(anyString(), isA(AtomicInteger.class));
    verifyNoMoreInteractions(cache);
    verify(queue).offer(notification);
  }

  @DisplayName("A notification is incrementing a counter in a cache")
  @Test
  void incrementCounter() {
    // given
    var notification = new DelayedChatNotification(123, 987, "John", "Smith", "johnsmith", "en");
    var counter = new AtomicInteger(456);

    when(cache.get(anyString())).thenReturn(counter);

    // when
    simpleChatKeeper.addUserToWatchList(notification);

    // then
    verify(cache).get("123:987");
    verifyNoMoreInteractions(cache);
    verify(queue, never()).offer(notification);

    assertEquals(457, counter.intValue());
  }

  @DisplayName("Get delayed notifications")
  @Test
  void getDelayedNotifications() {
    // given
    var bill = new DelayedChatNotification(456, 678, "Bill", "Taylor", "billtaylor", "en");
    var jane = new DelayedChatNotification(123, 321, "Jane", "Smith", "janesmith", "en");
    var john = new DelayedChatNotification(123, 987, "John", "Smith", "johnsmith", "en");

    when(queue.drainTo(isA(Collection.class))).thenAnswer(invocationOnMock -> {
      var collection = invocationOnMock.getArgument(0, Collection.class);

      collection.add(john);
      collection.add(jane);
      collection.add(bill);

      return 3;
    });

    // when
    var delayedNotifications = simpleChatKeeper.getDelayedNotifications();

    // then
    assertAll("Delayed notifications", () -> assertThat(delayedNotifications, aMapWithSize(2)),
        () -> assertThat(delayedNotifications, hasEntry(equalTo(john.chatId()), containsInAnyOrder(john, jane))),
        () -> assertThat(delayedNotifications, hasEntry(equalTo(bill.chatId()), contains(bill))));
  }

}