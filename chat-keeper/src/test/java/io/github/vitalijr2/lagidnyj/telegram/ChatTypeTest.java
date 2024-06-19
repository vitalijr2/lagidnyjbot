package io.github.vitalijr2.lagidnyj.telegram;

import static org.junit.jupiter.api.Assertions.*;

import io.github.vitalijr2.lagidnyj.telegram.TelegramBotTools.ChatType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("fast")
class ChatTypeTest {

  @DisplayName("Null or empty, or unknown value")
  @ParameterizedTest(name = "<{0}>")
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "qwerty"})
  void nullOrEmptyOrUnknownValue(String value) {
    assertNull(ChatType.fromString(value));
  }

}