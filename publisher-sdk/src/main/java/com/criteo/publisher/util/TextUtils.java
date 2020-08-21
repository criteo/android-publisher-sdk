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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.Contract;

/**
 * This class contains some code from {@link android.text.TextUtils}. This is to enable unit testing
 * code that relies on it, on the JVM.
 */

public class TextUtils {

  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  @Contract("null -> true")
  public static boolean isEmpty(@Nullable CharSequence str) {
    return str == null || str.length() == 0;
  }

  /**
   * Return either non empty value or null value if empty
   *
   * @param str the {@link String} on which the method will run
   * @return {@code str} if it is not empty, {@code null} otherwise
   */
  @Nullable
  public static String getNotEmptyOrNullValue(@NonNull String str) {
    return !str.isEmpty() ? str : null;
  }
}
