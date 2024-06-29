package io.github.vitalijr2.lagidnyj.telegram;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.github.vitalijr2.lagidnyj.beans.User;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointerException;

public class TelegramBotTools {

  private static final String EDITED_MESSAGE = "edited_message";
  private static final String MESSAGE = "message";
  private static final Pattern MARKDOWN_ESCAPE_PATTERN = Pattern.compile("([_*\\[\\]()~>#+-=|{}.!])");

  private TelegramBotTools() {
  }

  /**
   * Markdown V2 escaping.
   *
   * @param text raw text
   * @return Markdown safe text
   */
  public static String markdownEscaping(String text) {
    return MARKDOWN_ESCAPE_PATTERN.matcher(text).replaceAll("\\\\$1");
  }

  /**
   * Check if a Telegram message is sent via bot.
   *
   * @param update Telegram update
   * @return true if the message has the {@code via_bot} field.
   */
  public static boolean viaBot(JSONObject update) {
    return nonNull(update.optQuery("/message/via_bot"));
  }

  /**
   * Check if a Telegram update is an edited message.
   *
   * @param update Telegram update
   * @return true if the message has the {@code edited_message} field.
   */
  public static boolean isEditedMessage(JSONObject update) {
    return update.has(EDITED_MESSAGE);
  }

  /**
   * Check if a Telegram update is a message.
   *
   * @param update Telegram update
   * @return true if the update has the {@code message} field.
   */
  public static boolean isMessage(JSONObject update) {
    return update.has(MESSAGE);
  }

  /**
   * Get chat identifier.
   *
   * @param message Telegram message
   * @return chat identifier
   * @throws JSONPointerException if the message does not contain a chat object
   */
  public static long getChatId(JSONObject message) throws JSONPointerException {
    return ((Number) message.query("/chat/id")).longValue();
  }

  /**
   * Get type of chat.
   *
   * @param message Telegram message
   * @return type of chat
   * @throws JSONPointerException if the message does not contain a chat object
   */
  public static ChatType getChatType(JSONObject message) throws JSONPointerException {
    return ChatType.fromString((String) message.query("/chat/type"));
  }

  /**
   * Get an edited message.
   *
   * @param update Telegram update
   * @return message
   * @throws JSONException if an update does not contain an edited message
   */
  @NotNull
  public static JSONObject getEditedMessage(JSONObject update) throws JSONException {
    return update.getJSONObject(EDITED_MESSAGE);
  }

  /**
   * Get a "from" user.
   *
   * @param message Telegram message
   * @return user
   */
  @NotNull
  public static User getFrom(JSONObject message) throws JSONException {
    var from = message.getJSONObject("from");

    return new User(from.getNumber("id").longValue(), from.getString("first_name"), from.optString("last_name", null),
        from.optString("username", null), from.optString("language_code", null));
  }

  /**
   * Get a message or edited message.
   *
   * @param update Telegram update
   * @return message
   * @throws JSONException if an update does not contain a message
   */
  @NotNull
  public static JSONObject getMessage(JSONObject update) throws JSONException {
    return update.getJSONObject(MESSAGE);
  }

  /**
   * Take a {@code text} or {@code caption} fields from a message or an edited message.
   *
   * @param message Telegram message
   * @return text value
   */
  @NotNull
  public static Optional<String> getText(JSONObject message) {
    var text = message.optString("text", message.optString("caption", null));

    return (isNull(text)) ? Optional.empty() : Optional.of(text);
  }

  /**
   * Make a Telegram message.
   *
   * @param chatId chat identifier
   * @param text   Markdown text
   * @return JSON message
   * @see <a href="https://core.telegram.org/bots/api#markdownv2-style">Formatting options, MarkdownV2 style</a>
   */
  public static JSONObject sendMessage(long chatId, @NotNull String text) {
    var message = new JSONObject();

    message.put("chat_id", chatId);
    message.put("text", text);
    message.put("parse_mode", "MarkdownV2");

    return message;
  }

  public enum ChatType {
    Channel, Group, Private, Supergroup;

    private static final Map<String, ChatType> LOOKUP_MAP = Stream.of(values())
        .collect(Collectors.toMap((chatType) -> chatType.name().toLowerCase(), Function.identity()));

    public static ChatType fromString(String chatType) {
      if (null == chatType) {
        return null;
      }

      return LOOKUP_MAP.get(chatType.toLowerCase());
    }
  }

}
