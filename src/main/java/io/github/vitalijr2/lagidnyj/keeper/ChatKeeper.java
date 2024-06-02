package io.github.vitalijr2.lagidnyj.keeper;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import org.jetbrains.annotations.NotNull;

public interface ChatKeeper {

  /**
   * Add user to a watching list.
   *
   * @param notification delayed notification
   */
  void addUserToWatchList(@NotNull DelayedChatNotification notification);

}
