package io.github.vitalijr2.lagidnyj.telegram;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@Tag("fast")
class LagidnyjBotFastTest {

  private static Logger logger;

  @Mock
  private HttpRequest httpRequest;
  @Mock
  private HttpResponse httpResponse;

  private LagidnyjBot bot;

  @BeforeAll
  static void setUpClass() {
    logger = LoggerFactory.getLogger(LagidnyjBot.class);
  }

  @AfterEach
  void tearDown() {
    clearInvocations(logger);
  }

  @BeforeEach
  void setUp() {
    bot = new LagidnyjBot();
  }

  @DisplayName("HTTP method not allowed")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"GET", "HEAD", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH"})
  void methodNotAllowed(String methodName) throws IOException {
    try (var botTools = mockStatic(BotTools.class)) {
      // given
      when(httpRequest.getMethod()).thenReturn(methodName);
      when(httpRequest.getFirstHeader(anyString())).thenReturn(Optional.of("1.2.3.4"));

      // when
      assertDoesNotThrow(() -> bot.service(httpRequest, httpResponse));

      // then
      verify(logger).warn(eq("Method {} isn't implemented: {}"), eq(methodName), eq("1.2.3.4"));
      botTools.verify(() -> BotTools.badMethod(isA(HttpResponse.class), eq("POST")));
    }
  }

  @DisplayName("Bad request body")
  @Test
  void requestBody() throws IOException {
    try (var botTools = mockStatic(BotTools.class)) {
      // given
      var reader = new CharArrayReader("{\"a\":\"b\"}".toCharArray());

      bot = spy(bot);

      when(httpRequest.getMethod()).thenReturn("POST");
      when(httpRequest.getReader()).thenReturn(new BufferedReader(reader));
      doThrow(new JSONException("test exception")).when(bot).processRequestBody(isA(Reader.class));

      // when
      assertDoesNotThrow(() -> bot.service(httpRequest, httpResponse));

      // then
      verify(logger).warn(eq("Could not parse request body: {}"), eq("test exception"));
      botTools.verify(() -> BotTools.internalError(isA(HttpResponse.class)));
    }
  }

}