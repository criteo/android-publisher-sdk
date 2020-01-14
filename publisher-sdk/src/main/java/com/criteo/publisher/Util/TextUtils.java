package com.criteo.publisher.Util;

import android.support.annotation.Nullable;

/**
 * This is a partial copy of {@link android.text.TextUtils}. This is a copy so it's usable in UTs.
 */
public class TextUtils {

  /**
   * Returns true if the string is null or 0-length.
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  public static boolean isEmpty(@Nullable CharSequence str) {
    return str == null || str.length() == 0;
  }

}
