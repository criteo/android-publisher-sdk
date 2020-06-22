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

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Represent the state of the android application.
 * <p>
 * This should not be confused with the global state of the device (see {@link DeviceUtil})
 * <p>
 * The main purpose of this class is to share common operations related to the application state, so
 * caller may use this abstraction instead of directly looking into android internals.
 * <p>
 * Moreover, this abstraction allow tests to stub those android specific parts.
 */
public class AndroidUtil {

  private final Context context;

  public AndroidUtil(@NonNull Context context) {
    this.context = context;
  }

  /**
   * Overall orientation of the screen.
   * <p>
   * May be one of {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE},
   * {@link android.content.res.Configuration#ORIENTATION_PORTRAIT}.
   */
  public int getOrientation() {
    return context.getResources().getConfiguration().orientation;
  }

  /**
   * Transform given distance in DP (density-independent pixel) into pixels.
   *
   * @param dp distance in DP
   * @return equivalent in pixels
   */
  public int dpToPixel(int dp) {
    return (int) Math.ceil(dp * context.getResources().getDisplayMetrics().density);
  }

}
