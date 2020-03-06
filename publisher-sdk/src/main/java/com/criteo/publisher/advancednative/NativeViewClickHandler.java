package com.criteo.publisher.advancednative;

import android.view.View;

interface NativeViewClickHandler {

  /**
   * Invoked callback in case a
   * {@linkplain ClickDetection#watch(View, NativeViewClickHandler) watched view} was clicked.
   */
  void onClick();
}
