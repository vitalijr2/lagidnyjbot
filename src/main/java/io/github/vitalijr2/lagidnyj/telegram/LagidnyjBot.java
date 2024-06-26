package io.github.vitalijr2.lagidnyj.telegram;

import static io.github.vitalijr2.lagidnyj.telegram.BotTools.badMethod;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.getChatId;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.getChatType;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.getEditedMessage;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.getFrom;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.getMessage;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.getText;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.internalError;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.isEditedMessage;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.isMessage;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.markdownEscaping;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.ok;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.okWithBody;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.sendMessage;
import static io.github.vitalijr2.lagidnyj.telegram.BotTools.viaBot;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import io.github.vitalijr2.lagidnyj.cyrillic.CyrillicTools;
import io.github.vitalijr2.lagidnyj.keeper.ChatKeeper;
import java.io.IOException;
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

public class LagidnyjBot implements HttpFunction {

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
   * Get request body and send response back.
   *
   * @param httpRequest  Telegram update
   * @param httpResponse Telegram webhook answer
   * @see <a href="https://core.telegram.org/bots/api#update">Telegram Bot API: Update</a>
   * @see <a href="https://core.telegram.org/bots/api#making-requests-when-getting-updates">Telegram
   * Bot API: Making requests when getting updates</a>
   */
  @Override
  public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
    if (HTTP_POST_METHOD.equals(httpRequest.getMethod())) {
      try {
        processRequestBody(httpRequest.getReader()).ifPresentOrElse(body -> okWithBody(httpResponse, body),
            () -> ok(httpResponse));
      } catch (IOException | JSONException exception) {
        logger.warn("Could not parse request body: {}", exception.getMessage());
        internalError(httpResponse);
      }
    } else {
      logger.warn("Method {} isn't implemented: {}", httpRequest.getMethod(),
          httpRequest.getFirstHeader("X-Forwarded-For").orElse("address not known"));
      badMethod(httpResponse, HTTP_POST_METHOD);
    }
  }

  /**
   * If the Telegram update is a regular message or an inline query, pass it to the appropriate method:
   * {@link #processMessage(JSONObject)}.
   *
   * @param requestBodyReader request body
   * @return webhook answer if available
   */
  @VisibleForTesting
  @NotNull
  Optional<String> processRequestBody(Reader requestBodyReader) {
    var update = new JSONObject(new JSONTokener(requestBodyReader));
    var result = Optional.<String>empty();

    if (viaBot(update)) {
      logger.trace("Ignore message of another bot");
    } else if (isMessage(update)) {
      result = Optional.ofNullable(processMessage(getMessage(update)));
    } else if (isEditedMessage(update)) {
      result = Optional.ofNullable(processMessage(getEditedMessage(update)));
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
      switch (getChatType(message)) {
        case Channel:
          // do nothing
          break;
        case Private:
          var helpMessage = sendMessage(getChatId(message),
              String.format(markdownEscaping(HELP_MESSAGE), BUY_ME_A_COFFEE_LINK));

          logger.info("help message: {}", helpMessage);
          helpMessage.put("method", "sendMessage");
          reply = helpMessage.toString();
          break;
        default:
          getText(message).map(CyrillicTools::hasRussianLetters).ifPresent(hasRussianLetters -> {
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
    var russianSpeaker = getFrom(update);
    var notification = new DelayedChatNotification(getChatId(update), russianSpeaker.id(), russianSpeaker.firstName(),
        russianSpeaker.lastName(), russianSpeaker.username(), russianSpeaker.languageCode());
    logger.trace("Add user to watch list: {}", notification);
    chatKeeper.addUserToWatchList(notification);
  }

}
