package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import java.lang.ref.Reference;
import java.net.URI;

class ImpressionTask implements VisibilityListener {

  @NonNull
  private final Iterable<URI> impressionPixels;

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ImpressionHelper helper;

  ImpressionTask(
      @NonNull Iterable<URI> impressionPixels,
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ImpressionHelper helper) {
    this.impressionPixels = impressionPixels;
    this.listenerRef = listenerRef;
    this.helper = helper;
  }

  @Override
  public void onVisible() {
    helper.firePixels(impressionPixels);

    CriteoNativeAdListener listener = listenerRef.get();
    if (listener != null) {
      helper.notifyImpression(listener);
    }
  }
}
