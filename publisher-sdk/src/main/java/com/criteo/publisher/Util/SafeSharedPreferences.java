package com.criteo.publisher.Util;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.DependencyProvider;

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

  @Nullable
  public Integer getInt(@NonNull String key, @Nullable Integer defaultValue) {
    Integer value = defaultValue;

    try {
      value = sharedPreferences.getInt(key, defaultValue);
    } catch (ClassCastException e) {
      PreconditionsUtil.throwOrLog(
          new IllegalStateException("Expect an Integer type when reading " + key, e)
      );
    }

    return value;
  }
}
