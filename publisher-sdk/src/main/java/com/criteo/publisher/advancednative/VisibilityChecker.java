package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import android.view.View;

interface VisibilityChecker {

  /**
   * Detect the visibility on screen of the given view.
   *
   * It is detected if at least 1px of the view is visible on screen.
   *
   * @param view view to check the visibility
   * @return true if visible, else false
   */
  boolean isVisible(@NonNull View view);
}
