package com.criteo.publisher.advancednative;

import android.view.View;

interface VisibilityListener {

  /**
   * Invoked callback in case a
   * {@linkplain VisibilityTracker#watch(View, VisibilityListener) watched view} was detected as
   * visible.
   */
  void onVisible();
}
