package io.github.vitalijr2.lagidnyj.telegram;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.cloud.functions.HttpResponse;
import io.github.vitalijr2.lagidnyj.beans.User;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotTools {

  private static final String APPLICATION_JSON = "application/json;charset=utf-8";
  private static final String EDITED_MESSAGE = "edited_message";
  private static final String FULL_VERSION_STRING;
  private static final String HTTP_BAD_METHOD_RESPONSE;
  private static final Logger LOGGER = LoggerFactory.getLogger(BotTools.class);
  private static final String MESSAGE = "message";
  private static final Pattern MARKDOWN_ESCAPE_PATTERN = Pattern.compile("([_*\\[\\]()~>#+-=|{}.!])");
  private static final String SERVER_HEADER = "Server";
  private static final String TEXT_HTML = "text/html;charset=utf-8";

  static {
    var body = "";
    var name = "unknown";
    var version = "unknown";

    try (InputStream versionPropsStream = BotTools.class.getResourceAsStream("/bot-tools.properties")) {
      var properties = new Properties();

      properties.load(versionPropsStream);
      body = properties.getProperty("http.bad-method");
      name = properties.getProperty("bot.name");
      version = properties.getProperty("bot.version");
    } catch (Exception exception) {
      LoggerFactory.getLogger(BotTools.class).error("Could not initialize the bot tools: {}", exception.getMessage());
      System.exit(1);
    }
    FULL_VERSION_STRING = name + " - " + version;
    HTTP_BAD_METHOD_RESPONSE = body;
  }

  private BotTools() {
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
   * Make HTTP response.
   *
   * @param httpResponse  instance of HTTP response
   * @param statusCode    HTTP status
   * @param statusMessage HTTP status message
   * @param body          HTTP response body
   * @return prepared HTTP response
   */
  public static HttpResponse doResponse(@NotNull HttpResponse httpResponse, int statusCode,
      @NotNull String statusMessage,
      @Nullable String body) {
    try {
      httpResponse.setStatusCode(statusCode, statusMessage);
      httpResponse.appendHeader(SERVER_HEADER, FULL_VERSION_STRING);
      if (null != body) {
        httpResponse.getWriter().write(body);
      }
    } catch (IOException exception) {
      LOGGER.warn("Could not make HTTP {} response: {}", statusCode, exception.getMessage());
    }

    return httpResponse;
  }

  /**
   * &quot;Method Not Allowed&quot; HTTP response.
   *
   * @param httpResponse   instance of HTTP response
   * @param allowedMethods list of allowed methods
   */
  public static void badMethod(@NotNull HttpResponse httpResponse, String... allowedMethods) {
    httpResponse.setContentType(TEXT_HTML);
    doResponse(httpResponse, 405, "Method Not Allowed", HTTP_BAD_METHOD_RESPONSE).getHeaders()
        .put("Allow", List.of(allowedMethods));
  }

  /**
   * &quot;Internal Server Error&quot; HTTP response.
   *
   * @param httpResponse instance of HTTP response
   */
  public static void internalError(@NotNull HttpResponse httpResponse) {
    doResponse(httpResponse, 500, "Internal Server Error", null);
  }

  /**
   * &quot;OK&quot; HTTP response without body.
   *
   * @param httpResponse instance of HTTP response
   */
  public static void ok(HttpResponse httpResponse) {
    okWithBody(httpResponse, null);
  }

  /**
   * &quot;OK&quot; HTTP response with body.
   *
   * @param httpResponse instance of HTTP response
   * @param body         response body
   */
  public static void okWithBody(HttpResponse httpResponse, String body) {
    httpResponse.setContentType(APPLICATION_JSON);
    doResponse(httpResponse, 200, "OK", body);
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
