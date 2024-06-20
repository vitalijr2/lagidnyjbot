package io.github.vitalijr2.lagidnyj.chatkeeper;

import static io.github.vitalijr2.lagidnyj.telegram.TelegramBotTools.sendMessage;
import static java.util.Objects.requireNonNull;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.cache.Cache;
import javax.cache.Caching;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleChatKeeper implements AutoCloseable, ChatKeeper, Runnable {

  public static final String CACHE_CONFIG_FILE = "chat-keeper.xml";
  public static final long DEFAULT_KEEPING_DELAY = 186;
  private final Cache<String, Integer> chatKeeperCache;
  private final ScheduledExecutorService charKeeperExecutor;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final BlockingQueue<DelayedChatNotification> notificationQueue;

  public SimpleChatKeeper() {
    this(getCache(CACHE_CONFIG_FILE), getQueue(), getScheduledExecutorService());
  }

  @VisibleForTesting
  SimpleChatKeeper(Cache<String, Integer> cache, BlockingQueue<DelayedChatNotification> queue,
      ScheduledExecutorService scheduledExecutorService) {
    chatKeeperCache = cache;
    notificationQueue = queue;
    charKeeperExecutor = scheduledExecutorService;

    charKeeperExecutor.scheduleWithFixedDelay(this, DEFAULT_KEEPING_DELAY, DEFAULT_KEEPING_DELAY, TimeUnit.SECONDS);
  }

  @VisibleForTesting
  static @NotNull Cache<String, Integer> getCache(String configFile) {
    var manager = Caching.getCachingProvider().getCacheManager();

    return requireNonNull(manager.getCache("chat-keeper", String.class, Integer.class),
        "Cache configuration is nof found");
  }

  private static @NotNull DelayQueue<DelayedChatNotification> getQueue() {
    return new DelayQueue<>();
  }

  private static @NotNull ScheduledExecutorService getScheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void addUserToWatchList(@NotNull DelayedChatNotification notification) {
    var counter = chatKeeperCache.get(notification.lookupId());

    if (counter == null) {
      notificationQueue.offer(notification);
    } else {
      chatKeeperCache.put(notification.lookupId(), ++counter);
    }
  }

  @Override
  public void close() throws Exception {
    charKeeperExecutor.shutdown();
    if (!charKeeperExecutor.awaitTermination(DEFAULT_KEEPING_DELAY, TimeUnit.SECONDS)) {
      charKeeperExecutor.shutdownNow();
    }
  }

  @Override
  public Map<Long, List<DelayedChatNotification>> getDelayedNotifications() {
    var notificationSet = new HashSet<DelayedChatNotification>();

    logger.trace("Notification queue's size: {}", notificationQueue.size());
    notificationQueue.drainTo(notificationSet);
    logger.trace("Get {} available elements", notificationSet.size());

    return notificationSet.stream().collect(Collectors.groupingBy(DelayedChatNotification::chatId));
  }

  @Override
  public void run() {
    sendNotifications();
  }

  @VisibleForTesting
  void sendNotifications() {
    getDelayedNotifications().forEach((chatId, russianSpeakers) -> {
      var message = "";

      sendMessage(chatId, message);
    });
  }

}
