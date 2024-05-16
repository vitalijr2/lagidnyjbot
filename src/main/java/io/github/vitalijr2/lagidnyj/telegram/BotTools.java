package io.github.vitalijr2.lagidnyj.telegram;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BotTools {

  private static final String FULL_VERSION_STRING;
  private static final String HTTP_BAD_METHOD_RESPONSE;
  private static final Logger LOGGER = LoggerFactory.getLogger(BotTools.class);
  private static final String SERVER_HEADER = "Server";

  static {
    var body = "";
    var name = "unknown";
    var version = "unknown";

    try (InputStream versionPropsStream = BotTools.class.getResourceAsStream("/lagidnyj-bot.properties")) {
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
   * Make HTTP response.
   *
   * @param httpResponse  instance of HTTP response
   * @param statusCode    HTTP status
   * @param statusMessage HTTP status message
   * @param body          HTTP response body
   * @return prepared HTTP response
   */
  static HttpResponse doResponse(@NotNull HttpResponse httpResponse, int statusCode, @NotNull String statusMessage,
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
  static void badMethod(@NotNull HttpResponse httpResponse, String... allowedMethods) {
    doResponse(httpResponse, 405, "Method Not Allowed", HTTP_BAD_METHOD_RESPONSE).getHeaders()
        .put("Allow", List.of(allowedMethods));
  }

  /**
   * &quot;Internal Server Error&quot; HTTP response.
   *
   * @param httpResponse instance of HTTP response
   */
  static void internalError(@NotNull HttpResponse httpResponse) {
    doResponse(httpResponse, 500, "Internal Server Error", null);
  }

  /**
   * &quot;OK&quot; HTTP response without body.
   *
   * @param httpResponse instance of HTTP response
   */
  static void ok(HttpResponse httpResponse) {
    okWithBody(httpResponse, null);
  }

  /**
   * &quot;OK&quot; HTTP response with body.
   *
   * @param httpResponse instance of HTTP response
   * @param body         response body
   */
  static void okWithBody(HttpResponse httpResponse, String body) {
    doResponse(httpResponse, 200, "OK", body);
  }

  /**
   * Check if a Telegram message is sent via bot.
   *
   * @param update Telegram update
   * @return true if the message has the {@code via_bot} field.
   */
  static boolean viaBot(JSONObject update) {
    return nonNull(update.optQuery("/message/via_bot"));
  }

  /**
   * Check if a Telegram update is an edited message.
   *
   * @param update Telegram update
   * @return true if the message has the {@code edited_message} field.
   */
  static boolean isEditedMessage(JSONObject update) {
    return update.has("edited_message");
  }

  /**
   * Check if a Telegram update is a message.
   *
   * @param update Telegram update
   * @return true if the update has the {@code message} field.
   */
  static boolean isMessage(JSONObject update) {
    return update.has("message");
  }

  /**
   * Take a {@code text} or {@code caption} fields from a message or an edited message.
   *
   * @param update Telegram update
   * @return text value
   */
  static Optional<String> getText(JSONObject update) {
    JSONObject message = null;
    String text = null;

    if (isMessage(update)) {
      message = update.getJSONObject("message");
    } else if (isEditedMessage(update)) {
      message = update.getJSONObject("edited_message");
    }
    if (null != message) {
      text = message.optString("text", message.optString("caption"));
    }

    return (isNull(text) || text.isBlank()) ? Optional.empty() : Optional.of(text);
  }

}
