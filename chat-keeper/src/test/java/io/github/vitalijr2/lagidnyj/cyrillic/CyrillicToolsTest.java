package io.github.vitalijr2.lagidnyj.cyrillic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.vitalijr2.lagidnyj.cyrillic.CyrillicTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("fast")
class CyrillicToolsTest {

  @DisplayName("Russian letters")
  @ParameterizedTest
  @CsvSource(value = {"Ё", "ё", "Ъ", "ъ", "Ы", "ы", "Э", "э"})
  void russianLetters(String letter) {
    // when and then
    assertTrue(CyrillicTools.hasRussianLetters(String.format("qwerty%sйцукен", letter)));
  }

  @DisplayName("Non-russian letter")
  @Test
  void nonRussianLetters() {
    // when and then
    assertFalse(CyrillicTools.hasRussianLetters(String.format("qwerty%sйцукен", "ї")));
  }

}