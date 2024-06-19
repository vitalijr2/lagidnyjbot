package io.github.vitalijr2.lagidnyj.telegram;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import io.github.vitalijr2.lagidnyj.cyrillic.CyrillicTools;
import io.github.vitalijr2.lagidnyj.chatkeeper.ChatKeeper;
import io.github.vitalijr2.telegram.webhookbot.TelegramWebhookBot;
import java.io.Reader;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LagidnyjBot implements TelegramWebhookBot {

  private static final String HELP_MESSAGE = "Більше інформації для чого цей бот та як ним користуватись в дописі про %s.";
  private static final String BUY_ME_A_COFFEE_LINK = "[лагідну українізацію](https://buymeacoffee.com/vitalij_r2/lagidna-ukrajinizacija)";
  private static final String HTTP_POST_METHOD = "POST";

  private final ChatKeeper chatKeeper;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public LagidnyjBot() {
    this(null);
  }

  LagidnyjBot(ChatKeeper chatKeeper) {
    this.chatKeeper = chatKeeper;
  }

  /**
   * If the Telegram update is a regular message or an inline query, pass it to the appropriate method:
   * {@link #processMessage(JSONObject)}.
   *
   * @param payloadReader request body
   * @return webhook answer if available
   */
  @VisibleForTesting
  @NotNull
  public Optional<String> processPayload(Reader payloadReader) {
    var update = new JSONObject(new JSONTokener(payloadReader));
    var result = Optional.<String>empty();

    if (TelegramBotTools.viaBot(update)) {
      logger.trace("Ignore message of another bot");
    } else if (TelegramBotTools.isMessage(update)) {
      result = Optional.ofNullable(processMessage(TelegramBotTools.getMessage(update)));
    } else if (TelegramBotTools.isEditedMessage(update)) {
      result = Optional.ofNullable(processMessage(TelegramBotTools.getEditedMessage(update)));
    }

    return result;
  }

  /**
   * Process <a href="https://core.telegram.org/bots/api#message">a message</a> or an edited message.
   * <p>
   * It takes {@code text} or {@code caption}, and then looks it for the Cyrillic letters <strong>ё</strong>,
   * <strong>ъ</strong>, <strong>ы</strong> and <strong>э</strong>.
   *
   * @param message Telegram message
   * @return warning for a user, restriction if some warnings have sent before or null
   */
  @VisibleForTesting
  @Nullable
  String processMessage(JSONObject message) {
    logger.trace(message.toString());
    String reply = null;
    try {
      switch (TelegramBotTools.getChatType(message)) {
        case Channel:
          // do nothing
          break;
        case Private:
          var helpMessage = TelegramBotTools.sendMessage(TelegramBotTools.getChatId(message),
              String.format(TelegramBotTools.markdownEscaping(HELP_MESSAGE), BUY_ME_A_COFFEE_LINK));

          logger.info("help message: {}", helpMessage);
          helpMessage.put("method", "sendMessage");
          reply = helpMessage.toString();
          break;
        default:
          TelegramBotTools.getText(message).map(CyrillicTools::hasRussianLetters).ifPresent(hasRussianLetters -> {
            if (hasRussianLetters) {
              addUserToWatchList(message);
            }
          });
      }
    } catch (JSONException exception) {
      logger.warn("Could not parse message: {}", exception.getMessage());
    }

    return reply;
  }

  /**
   * Add user to a watching list.
   *
   * @param update Telegram update
   */
  @VisibleForTesting
  @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
  void addUserToWatchList(JSONObject update) {
    var russianSpeaker = TelegramBotTools.getFrom(update);
    var notification = new DelayedChatNotification(TelegramBotTools.getChatId(update), russianSpeaker.id(), russianSpeaker.firstName(),
        russianSpeaker.lastName(), russianSpeaker.username(), russianSpeaker.languageCode());
    logger.trace("Add user to watch list: {}", notification);
    chatKeeper.addUserToWatchList(notification);
  }

}
