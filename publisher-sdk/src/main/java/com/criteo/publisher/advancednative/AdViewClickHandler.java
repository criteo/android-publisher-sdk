package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.adview.RedirectionListener;
import java.lang.ref.Reference;
import java.net.URI;

class AdViewClickHandler implements NativeViewClickHandler {

  @NonNull
  private final URI uri;

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ClickHelper helper;

  AdViewClickHandler(
      @NonNull URI uri,
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ClickHelper helper
  ) {
    this.uri = uri;
    this.listenerRef = listenerRef;
    this.helper = helper;
  }

  @Override
  public void onClick() {
    helper.notifyUserClickAsync(listenerRef.get());

    helper.redirectUserTo(uri, new RedirectionListener() {
      @Override
      public void onUserRedirectedToAd() {
        helper.notifyUserIsLeavingApplicationAsync(listenerRef.get());
      }

      @Override
      public void onUserBackFromAd() {
        helper.notifyUserIsBackToApplicationAsync(listenerRef.get());
      }
    });
  }
}
