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
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import com.criteo.publisher.model.AdSize;

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

  @NonNull
  private final Context context;

  @NonNull
  private final DeviceUtil deviceUtil;

  public AndroidUtil(@NonNull Context context, @NonNull DeviceUtil deviceUtil) {
    this.context = context;
    this.deviceUtil = deviceUtil;
  }

  /**
   * Overall orientation of the screen.
   * <p>
   * May be one of {@link android.content.res.Configuration#ORIENTATION_LANDSCAPE}, {@link
   * android.content.res.Configuration#ORIENTATION_PORTRAIT}.
   */
  public int getOrientation() {
    // We're not using Configuration#orientation because it seems to not work in some cases.
    // For example, when starting an application with device in landscape, this property contains
    // ORIENTATION_PORTRAIT.
    AdSize currentScreenSize = deviceUtil.getCurrentScreenSize();
    return currentScreenSize.getWidth() < currentScreenSize.getHeight()
        ? Configuration.ORIENTATION_PORTRAIT
        : Configuration.ORIENTATION_LANDSCAPE;
  }

}
