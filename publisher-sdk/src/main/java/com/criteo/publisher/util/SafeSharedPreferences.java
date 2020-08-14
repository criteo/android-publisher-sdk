/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher.util;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Wrapper around {@link SharedPreferences} that provides type safety when accessing data saved under
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

  public long getLong(@NonNull String key, long defaultValue) {
    long value = defaultValue;

    try {
      value = sharedPreferences.getLong(key, defaultValue);
    } catch (ClassCastException e) {
      PreconditionsUtil.throwOrLog(
          new IllegalStateException("Expect a Long type when reading " + key, e)
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
          new IllegalStateException("Expect a boolean type when reading " + key, e)
      );
    }

    return value;
  }

}
