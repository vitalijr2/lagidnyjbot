package io.github.vitalijr2.lagidnyj.chatkeeper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class SimpleChatKeeperSlowTest {

  @DisplayName("Create an instance")
  @Test
  void createInstance() {
    assertDoesNotThrow(() -> new SimpleChatKeeper());
  }

  @DisplayName("Cache configuration is not found")
  @Test
  void cacheConfigurationNotFound() {
    // when
    assertEquals("Cache configuration is nof found", assertThrows(NullPointerException.class,
        () -> SimpleChatKeeper.getCache(getClass(), "/wrong-configuration.xml")).getMessage());
  }

}