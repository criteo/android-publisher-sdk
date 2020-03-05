package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import java.net.URI;

class ImpressionTask implements VisibilityListener {

  @NonNull
  private final Iterable<URI> impressionPixels;

  @NonNull
  private final ImpressionHelper helper;

  ImpressionTask(
      @NonNull Iterable<URI> impressionPixels,
      @NonNull ImpressionHelper helper) {
    this.impressionPixels = impressionPixels;
    this.helper = helper;
  }

  @Override
  public void onVisible() {
    helper.firePixels(impressionPixels);
  }
}
