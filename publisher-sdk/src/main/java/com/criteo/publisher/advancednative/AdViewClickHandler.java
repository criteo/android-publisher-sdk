package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import java.lang.ref.Reference;

class AdViewClickHandler implements NativeViewClickHandler {

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ClickHelper helper;

  AdViewClickHandler(
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ClickHelper helper
  ) {
    this.listenerRef = listenerRef;
    this.helper = helper;
  }

  @Override
  public void onClick() {
    helper.notifyUserClickAsync(listenerRef.get());

    // TODO EE-920 redirect user to URI
    // TODO EE-920 notify listener that user is leaving the application
    // TODO EE-920 notify listener that user is back to the application
  }
}
