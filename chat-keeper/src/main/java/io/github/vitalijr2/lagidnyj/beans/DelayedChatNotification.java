package io.github.vitalijr2.lagidnyj.beans;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Delayed notification.
 *
 * @param chatId         chat identifier
 * @param userId         user identifier
 * @param firstName      first name
 * @param lastName       last name, optional
 * @param username       username, optional
 * @param languageCode   language code, optional, see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF
 *                       language tag</a>
 * @param secondsOfDelay seconds of delay
 */
public record DelayedChatNotification(long chatId, long userId, @NotNull String firstName, @Nullable String lastName,
                                      @Nullable String username, @Nullable String languageCode,
                                      int secondsOfDelay) implements Delayed {

  /**
   * Default seconds of delay.
   */
  public static final int DEFAULT_DELAY = 92;

  /**
   * Delayed notification with default delay, see {@link #DEFAULT_DELAY}.
   *
   * @param chatId       chat identifier
   * @param userId       user identifier
   * @param firstName    first name
   * @param lastName     last name, optional
   * @param username     username, optional
   * @param languageCode language code, optional, see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF
   *                     language tag</a>
   */
  public DelayedChatNotification(long chatId, long userId, @NotNull String firstName, @Nullable String lastName,
      @Nullable String username, @Nullable String languageCode) {
    this(chatId, userId, firstName, lastName, username, languageCode, DEFAULT_DELAY);
  }

  @Override
  public int compareTo(@NotNull Delayed delayed) {
    return (int) (getDelay(TimeUnit.SECONDS) - delayed.getDelay(TimeUnit.SECONDS));
  }

  @Override
  public long getDelay(@NotNull TimeUnit timeUnit) {
    return timeUnit.convert(secondsOfDelay, TimeUnit.SECONDS);
  }

  /**
   * Lookup ID.
   * <p>
   * The identifier is used for caching of counter, equals chatId + userId.
   *
   * @return lookup ID
   */
  public String lookupId() {
    return chatId + ":" + userId;
  }

}
