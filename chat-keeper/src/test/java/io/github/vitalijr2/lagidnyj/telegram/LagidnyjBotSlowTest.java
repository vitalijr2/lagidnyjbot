package io.github.vitalijr2.lagidnyj.telegram;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.CharArrayReader;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
class LagidnyjBotSlowTest {

  @Spy
  private LagidnyjBot bot;

  @DisplayName("Webhook")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "webhook.csv", delimiterString = "|", nullValues = "N/A", numLinesToSkip = 1)
  void webhook(String title, String requestBody, String messageResponseBody) throws IOException {
    // given
    var payloadReader = new CharArrayReader(requestBody.toCharArray());

    if (null != messageResponseBody) {
      doReturn(messageResponseBody).when(bot).processMessage(isA(JSONObject.class));
    }

    // when
    var webhookAnswer = assertDoesNotThrow(() -> bot.processPayload(payloadReader));

    // then
    if (null != messageResponseBody) {
      assertThat(webhookAnswer, isPresentAnd(equalTo(messageResponseBody)));
    } else {
      assertThat(webhookAnswer, isEmpty());
    }
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