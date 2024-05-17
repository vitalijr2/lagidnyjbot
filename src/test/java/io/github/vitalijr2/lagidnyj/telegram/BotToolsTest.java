package io.github.vitalijr2.lagidnyj.telegram;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpResponse;
import io.github.vitalijr2.lagidnyj.telegram.BotTools.ChatType;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class BotToolsTest {

  private static Logger logger;

  @Mock
  private HttpResponse httpResponse;

  @BeforeAll
  static void setUpClass() {
    logger = LoggerFactory.getLogger(BotTools.class);
  }

  @AfterEach
  void tearDown() {
    clearInvocations(logger);
  }

  @DisplayName("Do response: IOException")
  @Test
  void doResponseException() throws IOException {
    try (var botTools = mockStatic(BotTools.class)) {
      // given
      when(httpResponse.getWriter()).thenThrow(new IOException("test exception"));
      botTools.when(() -> BotTools.doResponse(isA(HttpResponse.class), anyInt(), anyString(), any()))
          .thenCallRealMethod();

      // when
      assertDoesNotThrow(() -> BotTools.doResponse(httpResponse, 678, "Test status", "test body"));

      // then
      verify(logger).warn("Could not make HTTP {} response: {}", 678, "test exception");
    }
  }

  @DisplayName("Take text from a message")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "take_text.csv", delimiterString = "|", numLinesToSkip = 1)
  void takeTextFromMessage(String title, String message) {
    // when and then
    assertThat(BotTools.getText(new JSONObject(message)), isPresent());
  }

  @DisplayName("Take text from a message: no text")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "not_take_text.csv", delimiterString = "|", numLinesToSkip = 1)
  void notTakeTextFromMessage(String title, String message) {
    // when and then
    assertThat(BotTools.getText(new JSONObject(message)), isEmpty());
  }

  @DisplayName("Chat type")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "chat_type.csv", delimiterString = "|", numLinesToSkip = 1)
  void chatTypeFromMessage(String chatType, String message) {
    // given
    var expectedChatType = ChatType.fromString(chatType);

    // when and then
    assertEquals(expectedChatType, BotTools.getChatType(new JSONObject(message)));
  }

  @DisplayName("Not a chat")
  @Test
  void notChat() {
    // when and then
    assertNull(BotTools.getChatType(new JSONObject("{\"inline_query\":{\"query\":\"test query\"}}")));
  }

  @DisplayName("Send message")
  @Test
  void sendMessage() {
    // when
    var message = BotTools.sendMessage(12345, "test message");

    // then
    JSONAssert.assertEquals("{\"parse_mode\":\"MarkdownV2\",\"text\":\"test message\",\"chat_id\":12345}", message,
        true);
  }

}
