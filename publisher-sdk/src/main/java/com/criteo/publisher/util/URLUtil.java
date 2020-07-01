package com.criteo.publisher.util;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Utility methods copied and slightly modified from the {@link android.webkit.URLUtil}.
 * <p>
 * As this is part of the Android SDK, running unit tests on it will only return <code>false</code>,
 * or worse throw an exception.
 * <p>
 * It was copied to allow the unit tests to use it.
 */
public class URLUtil {

  /**
   * @return True iff the url is valid.
   */
  public static boolean isValidUrl(String url) {
    if (url == null || url.length() == 0) {
      return false;
    }

    return isHttpUrl(url) || isHttpsUrl(url);
  }

  /**
   * @return True iff the url is an http: url.
   */
  private static boolean isHttpUrl(String url) {
    return (null != url) &&
        (url.length() > 6) &&
        url.substring(0, 7).equalsIgnoreCase("http://");
  }

  /**
   * @return True iff the url is an https: url.
   */
  private static boolean isHttpsUrl(String url) {
    return (null != url) &&
        (url.length() > 7) &&
        url.substring(0, 8).equalsIgnoreCase("https://");
  }

}
