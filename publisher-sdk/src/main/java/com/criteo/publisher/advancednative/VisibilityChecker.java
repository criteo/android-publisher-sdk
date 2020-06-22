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

package com.criteo.publisher.advancednative;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;

public class VisibilityChecker {

  /**
   * Detect the visibility on screen of the given view.
   * <p>
   * It is detected if at least 1px of the view is visible on screen.
   *
   * @param view view to check the visibility
   * @return true if visible, else false
   */
  boolean isVisible(@NonNull View view) {
    if (!view.isShown()) {
      return false;
    }

    boolean hasNoSize = view.getWidth() == 0 || view.getHeight() == 0;
    if (hasNoSize) {
      return false;
    }

    // FIXME EE-931 Handle when the given view is completely covered by other views.

    return view.getGlobalVisibleRect(new Rect());
  }
}
