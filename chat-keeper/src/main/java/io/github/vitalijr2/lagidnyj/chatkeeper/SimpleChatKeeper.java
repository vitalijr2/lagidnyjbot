package io.github.vitalijr2.lagidnyj.chatkeeper;

import static io.github.vitalijr2.lagidnyj.telegram.TelegramBotTools.sendMessage;
import static java.util.Objects.requireNonNull;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.Caching;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleChatKeeper implements AutoCloseable, ChatKeeper, Runnable {

  public static final long DEFAULT_KEEPING_DELAY = 186;
  private static final String CACHE_CONFIG_FILE = "/chat-keeper.xml";
  private static final String CONFIGURATION_IS_NOF_FOUND = "Cache configuration is nof found";
  private final Cache<String, AtomicInteger> chatKeeperCache;
  private final ScheduledExecutorService charKeeperExecutor;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final BlockingQueue<DelayedChatNotification> notificationQueue;

  public SimpleChatKeeper() {
    this(getCache(SimpleChatKeeper.class, CACHE_CONFIG_FILE), getQueue(), getScheduledExecutorService());
  }

  @VisibleForTesting
  SimpleChatKeeper(Cache<String, AtomicInteger> cache, BlockingQueue<DelayedChatNotification> queue,
      ScheduledExecutorService scheduledExecutorService) {
    chatKeeperCache = cache;
    notificationQueue = queue;
    charKeeperExecutor = scheduledExecutorService;

    charKeeperExecutor.scheduleWithFixedDelay(this, DEFAULT_KEEPING_DELAY, DEFAULT_KEEPING_DELAY, TimeUnit.SECONDS);
  }

  @VisibleForTesting
  static @NotNull Cache<String, AtomicInteger> getCache(Class clazz, String configFile) {
    try {
      var manager = Caching.getCachingProvider()
          .getCacheManager(clazz.getResource(configFile).toURI(), clazz.getClass().getClassLoader());

      return requireNonNull(manager.getCache("chat-keeper", String.class, AtomicInteger.class),
          CONFIGURATION_IS_NOF_FOUND);
    } catch (URISyntaxException exception) {
      throw new CacheException(CONFIGURATION_IS_NOF_FOUND, exception);
    }
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
      logger.debug("Added notification {} to the delay queue", notification);
    } else {
      logger.debug("Increment cached counter to {} by notification {}", counter.incrementAndGet(), notification);
    }
  }

  @Override
  public void close() throws InterruptedException {
    charKeeperExecutor.shutdown();
    if (!charKeeperExecutor.awaitTermination(DEFAULT_KEEPING_DELAY, TimeUnit.SECONDS)) {
      charKeeperExecutor.shutdownNow();
    }
  }

  @Override
  public Map<Long, List<DelayedChatNotification>> getDelayedNotifications() {
    var notificationSet = new HashSet<DelayedChatNotification>();

    logger.trace("Notification queue's size: {}", notificationQueue.size());
    logger.trace("Get {} available elements", notificationQueue.drainTo(notificationSet));

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
