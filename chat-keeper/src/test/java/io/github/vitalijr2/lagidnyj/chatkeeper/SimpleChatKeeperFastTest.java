package io.github.vitalijr2.lagidnyj.chatkeeper;

import static io.github.vitalijr2.lagidnyj.chatkeeper.SimpleChatKeeper.DEFAULT_KEEPING_DELAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
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
  private Cache<String, Integer> cache;
  @Mock
  private BlockingQueue<DelayedChatNotification> queue;
  @Mock
  private ScheduledExecutorService executorService;
  @Captor
  private ArgumentCaptor<Runnable> runnableCaptor;

  @InjectMocks
  private SimpleChatKeeper simpleChatKeeper;

  @DisplayName("Create and initialize instance")
  @Test
  void initialize() {
    // given
    clearInvocations(executorService);

    // when
    var chatKeeper = new SimpleChatKeeper(cache, queue, executorService);

    // then
    verify(executorService).scheduleWithFixedDelay(runnableCaptor.capture(), eq(DEFAULT_KEEPING_DELAY),
        eq(DEFAULT_KEEPING_DELAY), isA(TimeUnit.class));

    assertEquals(chatKeeper, runnableCaptor.getValue());
  }

  @DisplayName("Add a notification to a delayed queue")
  @Test
  void addNotification() {
    // given
    var notification = new DelayedChatNotification(123, 987, "John", "Smith", "johnsmith", "en");

    when(cache.get(anyString())).thenReturn(null);

    // when
    simpleChatKeeper.addUserToWatchList(notification);

    // then
    verify(cache).get("123:987");
    verify(cache, never()).put(anyString(), anyInt());
    verifyNoMoreInteractions(cache);
    verify(queue).offer(notification);
  }

  @DisplayName("Increment a counter in a cache")
  @Test
  void incrementCounter() {
    // given
    var notification = new DelayedChatNotification(123, 987, "John", "Smith", "johnsmith", "en");

    when(cache.get(anyString())).thenReturn(456);

    // when
    simpleChatKeeper.addUserToWatchList(notification);

    // then
    verify(cache).get("123:987");
    verify(cache).put("123:987", 457);
    verifyNoMoreInteractions(cache);
    verify(queue, never()).offer(notification);
  }

}