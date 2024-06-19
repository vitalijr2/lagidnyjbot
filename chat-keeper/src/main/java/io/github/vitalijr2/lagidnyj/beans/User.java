package io.github.vitalijr2.lagidnyj.beans;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Telegram user.
 *
 * @param id           identifier
 * @param firstName    first name
 * @param lastName     last name, optional
 * @param username     username, optional
 * @param languageCode language code, optional
 */
public record User(long id, @NotNull String firstName, @Nullable String lastName, @Nullable String username,
                   @Nullable String languageCode) {

}
