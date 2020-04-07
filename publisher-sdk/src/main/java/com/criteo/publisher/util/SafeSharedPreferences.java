package com.criteo.publisher.util;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Wrapper around {@link SharedPreferences} that provide type safety when accessing data saved under
 * keys in DefaultSharedPreferences storage (which we can't necessarily control). This is to prevent
 * the apps using our SDK from crashing in case of type mismatch.
 */
public class SafeSharedPreferences {

  private final SharedPreferences sharedPreferences;

  public SafeSharedPreferences(@NonNull SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  @Nullable
  public String getString(@NonNull String key, @Nullable String defaultValue) {
    String value = defaultValue;

    try {
      value = sharedPreferences.getString(key, defaultValue);
    } catch (ClassCastException e) {
      PreconditionsUtil.throwOrLog(
          new IllegalStateException("Expected a String type when reading: " + key, e)
      );
    }

    return value;
  }

  public int getInt(@NonNull String key, int defaultValue) {
    int value = defaultValue;

    try {
      value = sharedPreferences.getInt(key, defaultValue);
    } catch (ClassCastException e) {
      PreconditionsUtil.throwOrLog(
          new IllegalStateException("Expect an int type when reading " + key, e)
      );
    }

    return value;
  }

  public boolean getBoolean(@NonNull String key, boolean defaultValue) {
    boolean value = defaultValue;

    try {
      value = sharedPreferences.getBoolean(key, defaultValue);
    } catch (ClassCastException e) {
      PreconditionsUtil.throwOrLog(
          new IllegalStateException("Expect an boolean type when reading " + key, e)
      );
    }

    return value;
  }

}
