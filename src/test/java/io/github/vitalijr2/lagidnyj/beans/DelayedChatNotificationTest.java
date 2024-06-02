package io.github.vitalijr2.lagidnyj.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("fast")
class DelayedChatNotificationTest {

  @DisplayName("Compare")
  @ParameterizedTest
  @CsvSource({"123,321,-198", "321,123,198", "123,123,0"})
  void compareTo(int firstSecondsOfDelay, int secondSecondsOfDelay, int expected) {
    // given
    var first = new DelayedChatNotification(123, 456, "John", "Smith", "johnsmith", "en", firstSecondsOfDelay);
    var second = new DelayedChatNotification(123, 456, "John", "Smith", "johnsmith", "en", secondSecondsOfDelay);

    // when
    assertEquals(expected, first.compareTo(second));
  }

  @DisplayName("Get delay")
  @ParameterizedTest
  @CsvSource({"NANOSECONDS,123,123000000000", "MICROSECONDS,123,123000000", "MILLISECONDS,123,123000",
      "SECONDS,123,123", "MINUTES,123,2", "HOURS,12300,3", "DAYS,1230000,14"})
  void getDelay(TimeUnit timeUnit, int secondsOfDelay, long expectedDelay) {
    // given
    var delayed = new DelayedChatNotification(123, 456, "John", "Smith", "johnsmith", "en", secondsOfDelay);

    // when and then
    assertEquals(expectedDelay, delayed.getDelay(timeUnit));
  }

}
