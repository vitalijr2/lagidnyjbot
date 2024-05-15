package io.github.vitalijr2.lagidnyj.telegram;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
      botTools.when(
              () -> BotTools.doResponse(isA(HttpResponse.class), anyInt(), anyString(), any()))
          .thenCallRealMethod();

      // when
      assertDoesNotThrow(() -> BotTools.doResponse(httpResponse, 678, "Test status", "test body"));

      // then
      verify(logger).warn("Could not make HTTP {} response: {}", 678, "test exception");
    }
  }

}
