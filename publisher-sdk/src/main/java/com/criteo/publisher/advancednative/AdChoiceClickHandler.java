package com.criteo.publisher.advancednative;

import android.support.annotation.NonNull;
import com.criteo.publisher.adview.RedirectionListener;
import java.lang.ref.Reference;
import java.net.URI;

class AdChoiceClickHandler implements NativeViewClickHandler {

  @NonNull
  private final URI privacyUri;

  @NonNull
  private final Reference<CriteoNativeAdListener> listenerRef;

  @NonNull
  private final ClickHelper helper;

  AdChoiceClickHandler(
      @NonNull URI privacyUri,
      @NonNull Reference<CriteoNativeAdListener> listenerRef,
      @NonNull ClickHelper helper
  ) {
    this.privacyUri = privacyUri;
    this.listenerRef = listenerRef;
    this.helper = helper;
  }

  @Override
  public void onClick() {
    helper.redirectUserTo(privacyUri, new RedirectionListener() {
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
