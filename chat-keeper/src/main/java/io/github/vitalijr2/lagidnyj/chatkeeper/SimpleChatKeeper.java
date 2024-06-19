package io.github.vitalijr2.lagidnyj.chatkeeper;

import static io.github.vitalijr2.lagidnyj.telegram.TelegramBotTools.sendMessage;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleChatKeeper implements ChatKeeper, Runnable {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Executor notificationExecutor = Executors.newSingleThreadExecutor();
  private final BlockingQueue<DelayedChatNotification> notificationQueue = new DelayQueue<>();

  @Override
  public void addUserToWatchList(@NotNull DelayedChatNotification notification) {
    notificationQueue.offer(notification);
  }

  @Override
  public Map<Long, Collection<DelayedChatNotification>> getDelayedNotifications() {
    return Map.of();
  }

  @Override
  public void run() {
    var notificationSet = new HashSet<DelayedChatNotification>();

    logger.trace("Notification queue's size: {}", notificationQueue.size());
    notificationQueue.drainTo(notificationSet);
    logger.trace("Get {} available elements", notificationSet.size());

    notificationSet.stream().collect(Collectors.groupingBy(DelayedChatNotification::chatId))
        .forEach((chatId, russianSpeakers) -> {
          var message = "";

          sendMessage(chatId, message);
        });
  }

}
