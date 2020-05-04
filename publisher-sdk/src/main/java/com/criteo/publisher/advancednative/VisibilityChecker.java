package com.criteo.publisher.advancednative;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

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
