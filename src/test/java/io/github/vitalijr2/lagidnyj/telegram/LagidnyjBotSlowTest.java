package io.github.vitalijr2.lagidnyj.telegram;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
class LagidnyjBotSlowTest {

  @Mock
  private HttpRequest httpRequest;
  @Mock
  private HttpResponse httpResponse;
  @Mock
  private BufferedWriter writer;

  @Spy
  private LagidnyjBot bot;

  @DisplayName("Webhook")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "webhook.csv", delimiterString = "|", nullValues = "N/A", numLinesToSkip = 1)
  void webhook(String title, String requestBody, String messageResponseBody) throws IOException {
    // given
    var reader = new CharArrayReader(requestBody.toCharArray());

    when(httpRequest.getMethod()).thenReturn("POST");
    when(httpRequest.getReader()).thenReturn(new BufferedReader(reader));
    if (null != messageResponseBody) {
      when(httpResponse.getWriter()).thenReturn(writer);
      doReturn(messageResponseBody).when(bot).processMessage(isA(JSONObject.class));
    }

    // when
    assertDoesNotThrow(() -> bot.service(httpRequest, httpResponse));

    // then
    verify(httpResponse).setStatusCode(200, "OK");
    verify(httpResponse).appendHeader(eq("Server"), anyString());
    verify(httpResponse).setContentType("application/json;charset=utf-8");
    if (null != messageResponseBody) {
      verify(httpResponse).getWriter();
      verify(writer).write(anyString());
    }
    verifyNoMoreInteractions(httpResponse);
  }

  @DisplayName("HTTP method not allowed")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"GET", "HEAD", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH"})
  void methodNotAllowed(String methodName) throws IOException {
    // given
    var headers = new HashMap<String, List<String>>();

    when(httpRequest.getMethod()).thenReturn(methodName);
    when(httpRequest.getFirstHeader(anyString())).thenReturn(Optional.of("1.2.3.4"));
    when(httpResponse.getHeaders()).thenReturn(headers);
    when(httpResponse.getWriter()).thenReturn(writer);

    // when
    assertDoesNotThrow(() -> bot.service(httpRequest, httpResponse));

    // then
    verify(httpResponse).setStatusCode(405, "Method Not Allowed");
    verify(httpResponse).appendHeader(eq("Server"), anyString());
    verify(httpResponse).setContentType("text/html;charset=utf-8");
    verify(httpResponse).getWriter();
    verifyNoMoreInteractions(httpResponse);
    verify(writer).write(startsWith("<!doctype html>"));

    assertThat("", headers, hasEntry(equalTo("Allow"), contains("POST")));
  }

  @DisplayName("Bad request body")
  @Test
  void requestBody() throws IOException {
    // given
    var reader = new CharArrayReader("{\"a\":\"b\"}".toCharArray());

    when(httpRequest.getMethod()).thenReturn("POST");
    when(httpRequest.getReader()).thenReturn(new BufferedReader(reader));
    doThrow(new JSONException("test exception")).when(bot).processRequestBody(isA(Reader.class));

    // when
    assertDoesNotThrow(() -> bot.service(httpRequest, httpResponse));

    // then
    verify(httpResponse).setStatusCode(500, "Internal Server Error");
    verify(httpResponse).appendHeader(eq("Server"), anyString());
    verifyNoMoreInteractions(httpResponse);
  }

  @DisplayName("Process message with Russian letters")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "russian_letters.csv", delimiterString = "|", numLinesToSkip = 1)
  void russianLetters(String title, String message) {
    // given
    doNothing().when(bot).addUserToWatchList(isA(JSONObject.class));

    // when
    bot.processMessage(new JSONObject(message));

    // then
    verify(bot).addUserToWatchList(isA(JSONObject.class));
  }

  @DisplayName("Process message without Russian letters")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "non_russian_letters.csv", delimiterString = "|", numLinesToSkip = 1)
  void nonRussianLetters(String chatType, String message) {
    // given
    var update = new JSONObject(message);

    // when
    bot.processMessage(update);

    // then
    verify(bot, never()).addUserToWatchList(isA(JSONObject.class));
  }

  @DisplayName("Reply a help message in a private chat")
  @Test
  void helpMessageInPrivateChat() {
    // given
    var update = new JSONObject("{\"chat\":{\"id\":321,\"type\":\"private\"}}");

    // when
    var reply = bot.processMessage(update);

    // then
    verify(bot, never()).addUserToWatchList(isA(JSONObject.class));

    var jsonReply = new JSONObject(reply);
    assertEquals("{\"method\":\"sendMessage\",\"parse_mode\":\"MarkdownV2\",\"chat_id\":321}", jsonReply, false);
  }

  @DisplayName("Channels are ignored")
  @Test
  void channelsIgnored() {
    // given
    var update = new JSONObject("{\"text\":\"ёж\",\"chat\":{\"type\":\"channel\"}}");

    // when
    var reply = bot.processMessage(update);

    // then
    verify(bot, never()).addUserToWatchList(isA(JSONObject.class));
    assertNull(reply);
  }

}