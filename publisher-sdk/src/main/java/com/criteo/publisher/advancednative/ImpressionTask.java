package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import java.lang.ref.Reference;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

class ImpressionTask implements VisibilityListener {

  @NonNull
  private final Iterable<URI> impressionPixels;

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ImpressionHelper helper;

  @NonNull
  private final AtomicBoolean isAlreadyTriggered;

  ImpressionTask(
      @NonNull Iterable<URI> impressionPixels,
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ImpressionHelper helper) {
    this.impressionPixels = impressionPixels;
    this.listenerRef = listenerRef;
    this.helper = helper;
    this.isAlreadyTriggered = new AtomicBoolean(false);
  }

  @Override
  public void onVisible() {
    if (!isAlreadyTriggered.compareAndSet(false, true)) {
      return;
    }

    helper.firePixels(impressionPixels);

    CriteoNativeAdListener listener = listenerRef.get();
    if (listener != null) {
      helper.notifyImpression(listener);
    }
  }
}
