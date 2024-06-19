package io.github.vitalijr2.lagidnyj.telegram;

import static io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification.DEFAULT_DELAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.vitalijr2.lagidnyj.beans.DelayedChatNotification;
import io.github.vitalijr2.lagidnyj.chatkeeper.ChatKeeper;
import io.github.vitalijr2.lagidnyj.telegram.LagidnyjBot;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LagidnyjBotFastTest {

  private static Logger logger;

  @Mock
  private ChatKeeper chatKeeper;
  @Captor
  private ArgumentCaptor<DelayedChatNotification> delayedChatNotificationCaptor;

  @InjectMocks
  @Spy
  private LagidnyjBot bot;

  @BeforeAll
  static void setUpClass() {
    logger = LoggerFactory.getLogger(LagidnyjBot.class);
  }

  @AfterEach
  void tearDown() {
    clearInvocations(logger);
  }

  @DisplayName("Unexpected message type")
  @Test
  void unexpectedMessageType() {
    // given
    var update = new JSONObject("{\"fiels\":\"value\"}");

    // when
    bot.processMessage(update);

    // then
    verify(bot, never()).addUserToWatchList(isA(JSONObject.class));
    verify(logger).trace("{\"fiels\":\"value\"}");
    verify(logger).warn(eq("Could not parse message: {}"), anyString());
  }

  @DisplayName("Add user to a watching list, seconds of delay")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "add_user_to_watching_list.csv", delimiterString = "|", numLinesToSkip = 1)
  void addUserToWatchList(String title, String update, String expectedValue) {
    // given
    var values = Arrays.copyOf(expectedValue.split(" "), 6);

    var expectedUser = new DelayedChatNotification(Long.parseLong(values[0]), Long.parseLong(values[1]), values[2],
        values[3], values[4], values[5]);

    // when
    bot.addUserToWatchList(new JSONObject(update));

    // then
    verify(chatKeeper).addUserToWatchList(delayedChatNotificationCaptor.capture());
    verify(logger).trace(anyString(), eq(expectedUser));

    assertEquals(DEFAULT_DELAY, delayedChatNotificationCaptor.getValue().secondsOfDelay());
  }

}