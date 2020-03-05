package com.criteo.publisher.advancednative;

import android.support.annotation.UiThread;

public abstract class CriteoNativeAdListener {

  /**
   * Callback invoked when a native view is detected as being displayed on user screen and ad
   * impression is triggered.
   * <p>
   * This callback is invoked on the UI thread, so it is safe to execute UI operations in the
   * implementation.
   */
  @UiThread
  void onAdImpression() {
  }
}
