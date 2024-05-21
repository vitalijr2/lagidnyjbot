package io.github.vitalijr2.lagidnyj.keeper;

import io.github.vitalijr2.lagidnyj.beans.User;
import org.jetbrains.annotations.Nullable;

public interface ChatKeeper {

  /**
   * Add user to a watching list.
   *
   * @param chatId chat identifier
   * @param user   Telegram user
   */
  void addUserToWatchList(@Nullable Long chatId, @Nullable User user);

}
