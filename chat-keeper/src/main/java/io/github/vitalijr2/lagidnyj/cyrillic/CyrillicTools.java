package io.github.vitalijr2.lagidnyj.cyrillic;

import java.util.regex.Pattern;

public class CyrillicTools {

  private static final Pattern RUSSIAN_LETTERS = Pattern.compile("[ЁёЫыЪъЭэ]");

  private CyrillicTools() {
  }

  public static boolean hasRussianLetters(String text) {
    return RUSSIAN_LETTERS.matcher(text).find();
  }

}
