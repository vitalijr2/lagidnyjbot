package io.github.vitalijr2.lagidnyj.chatkeeper;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface ChatKeeper {

  /**
   * Add user to a watching list.
   *
   * @param notification delayed notification
   */
  void addUserToWatchList(@NotNull DelayedChatNotification notification);

  /**
   * Get delayed notifications.
   *
   * @return map of chat identifier and delayed notification
   */
  Map<Long, List<DelayedChatNotification>> getDelayedNotifications();

}
